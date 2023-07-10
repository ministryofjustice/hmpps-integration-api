package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@Service
class GetSentencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
) {
  fun execute(pncId: String): Response<List<Sentence>>? {
//    val responseFromNomisGateway = nomisGateway.getSentences(pncId = pncId)

    return null
    //return Response<Sentence>?
//    return nomisGateway.getSentencesForPerson(responseFromNomisGateway.data)
  }
}
