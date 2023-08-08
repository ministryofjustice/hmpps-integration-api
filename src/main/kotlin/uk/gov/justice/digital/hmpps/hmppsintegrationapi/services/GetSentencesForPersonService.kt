package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence

@Service
class GetSentencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(pncId: String): Response<List<Sentence>> {
    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)
    val responseFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(pncId = pncId)

    if (responseFromPrisonerOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromPrisonerOffenderSearch.errors)
    }

    val bookingIdsResponse = nomisGateway.getBookingIdsForPerson(responseFromPrisonerOffenderSearch.data.first().identifiers.nomisNumber!!)

    if (bookingIdsResponse.errors.isNotEmpty()) {
      return Response(emptyList(), bookingIdsResponse.errors)
    }

    val nDeliusSentences = nDeliusGateway.getSentencesForPerson(responseFromProbationOffenderSearch.data?.identifiers?.deliusCrn!!)

    val sentencesResponses = bookingIdsResponse.data.map { nomisGateway.getSentencesForBooking(it.bookingId) }
    val sentencesData = sentencesResponses.map { it.data }.flatten()
    val sentencesErrors = sentencesResponses.map { it.errors }.flatten()

    if (sentencesErrors.isNotEmpty()) {
      return Response(emptyList(), sentencesErrors)
    }

    return Response(sentencesData)
  }
}
