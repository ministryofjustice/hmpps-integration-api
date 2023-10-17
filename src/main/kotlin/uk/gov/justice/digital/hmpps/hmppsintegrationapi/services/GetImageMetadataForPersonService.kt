package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetImageMetadataForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<List<ImageMetadata>> {
    val responseFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(hmppsId= hmppsId)

    if (responseFromProbationOffenderSearch.data==null) {
      return Response(
        data = emptyList(),
        errors =responseFromProbationOffenderSearch.errors
      )
    }

    return nomisGateway.getImageMetadataForPerson(responseFromProbationOffenderSearch.data.identifiers.nomisNumber!!)
  }
}
