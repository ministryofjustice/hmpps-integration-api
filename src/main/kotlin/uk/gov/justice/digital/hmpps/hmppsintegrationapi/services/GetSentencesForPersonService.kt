package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisBooking

@Service
class GetSentencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(hmppsId: String): Response<List<Sentence>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    var bookingIdsResponse: Response<List<NomisBooking>> = Response(data = emptyList())
    var nDeliusSentencesResponse: Response<List<Sentence>> = Response(data = emptyList())

    if (nomisNumber != null) {
      bookingIdsResponse = nomisGateway.getBookingIdsForPerson(nomisNumber)
    }
    // only execute if bookingIdsResponse is successful?
    val nomisSentenceResponse = Response.merge(bookingIdsResponse.data.map { nomisGateway.getSentencesForBooking(it.bookingId) })

    if (deliusCrn != null) {
      nDeliusSentencesResponse = nDeliusGateway.getSentencesForPerson(deliusCrn)
    }

    return Response(
      data = nomisSentenceResponse.data + nDeliusSentencesResponse.data,
      errors = bookingIdsResponse.errors + nomisSentenceResponse.errors + nDeliusSentencesResponse.errors,
    )
  }
}

/**
Person service error → Return person service error

No Nomis number + no Delius crn -> Return entity not found response

No Nomis number + Delius crn, delius success → return Delius
No Nomis number + Delius crn, delius any error → return Delius error

Nomis number + No Delius crn, Nomis success -> Return Nomis
Nomis number + No Delius crn, Nomis any error -> Return Nomis error

Nomis number + Delius crn, Nomis success, Delius success → Merge responses
Nomis number + Delius crn, Nomis success, Delius 404 → Return Nomis
Nomis number + Delius crn, Nomis 404 on booking ids, Delius success → Return Delius
Nomis number + Delius crn, Nomis 404 on sentences, Delius success → Return Delius
Nomis number + Delius crn, Nomis non 404 error on booking ids-> Return Nomis error
Nomis number + Delius crn, Nomis non 404 error on sentences -> Return Nomis error
Nomis number + Delius crn, Delius non 404 error -> Return Delius error
Nomis number + Delius crn, Nomis 404 on booking ids, Delius any error -> Return Delius error
Nomis number + Delius crn, Nomis 404 on sentences, Delius any error -> Return Delius error
Nomis number + Delius crn, Nomis any error on booking ids, Delius 404 -> Return Nomis error
Nomis number + Delius crn, Nomis any error on sentences, Delius 404 -> Return Nomis error
 **/
