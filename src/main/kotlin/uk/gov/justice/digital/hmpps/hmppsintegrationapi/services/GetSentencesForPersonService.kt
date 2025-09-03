package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetSentencesForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<Sentence>> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = emptyList(), errors = personResponse.errors)
    }

    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    if (nomisNumber == null && deliusCrn == null) {
      return Response(
        data = emptyList(),
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )
    }

    var nomisSentenceResponse = Response<List<Sentence>>(data = emptyList())
    if (nomisNumber != null) {
      val bookingIdsResponse = prisonApiGateway.getBookingIdsForPerson(nomisNumber)
      if (bookingIdsResponse.errors.isNotEmpty()) {
        if (!bookingIdsResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
          return Response(data = emptyList(), errors = bookingIdsResponse.errors)
        }
      } else {
        nomisSentenceResponse = Response.merge(bookingIdsResponse.data.map { prisonApiGateway.getSentencesForBooking(it.bookingId) })
        if (nomisSentenceResponse.errors.isNotEmpty() && !nomisSentenceResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
          return Response(data = emptyList(), errors = nomisSentenceResponse.errors)
        }
      }
    }

    var nDeliusSentencesResponse: Response<List<Sentence>> = Response(data = emptyList())
    if (deliusCrn != null) {
      nDeliusSentencesResponse = nDeliusGateway.getSentencesForPerson(deliusCrn)
      if (nDeliusSentencesResponse.errors.isNotEmpty()) {
        if (!nDeliusSentencesResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
          return Response(data = emptyList(), errors = nDeliusSentencesResponse.errors)
        } else {
          if (nomisSentenceResponse.errors.isEmpty()) {
            return nomisSentenceResponse
          } else {
            return nDeliusSentencesResponse
          }
        }
      }
    }

    return Response.merge(listOfNotNull(Response(data = nomisSentenceResponse.data), nDeliusSentencesResponse))
  }
}
