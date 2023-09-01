package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Booking

@Service
class GetSentencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(pncId: String): Response<List<Sentence>> {
    val personResponse = getPersonService.execute(pncId = pncId)
    val nomisNumber = personResponse.data["prisonerOffenderSearch"]?.identifiers?.nomisNumber
    val deliusCrn = personResponse.data["probationOffenderSearch"]?.identifiers?.deliusCrn
    var bookingIdsResponse: Response<List<Booking>> = Response(data = emptyList())
    var nDeliusSentencesResponse: Response<List<Sentence>> = Response(data = emptyList())

    if (nomisNumber != null) {
      bookingIdsResponse = nomisGateway.getBookingIdsForPerson(nomisNumber)
    }

    if (deliusCrn != null) {
      nDeliusSentencesResponse = nDeliusGateway.getSentencesForPerson(deliusCrn)
    }

    val nomisSentenceResponses = bookingIdsResponse.data.map { nomisGateway.getSentencesForBooking(it.bookingId) }

    return Response(
      data = nomisSentenceResponses.map { it.data }.flatten() + nDeliusSentencesResponse.data,
      errors = bookingIdsResponse.errors + nomisSentenceResponses.map { it.errors }.flatten() + nDeliusSentencesResponse.errors,
    )
  }
}
