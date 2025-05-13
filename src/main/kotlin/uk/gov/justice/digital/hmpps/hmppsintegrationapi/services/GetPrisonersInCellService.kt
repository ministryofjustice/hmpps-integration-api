package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest

@Service
class GetPrisonersInCellService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(
    prisonId: String,
    cellLocation: String,
  ): Response<List<PersonInPrison>?> {
    val request =
      POSAttributeSearchRequest(
        joinType = "AND",
        queries =
          listOf(
            POSAttributeSearchQuery(
              joinType = "AND",
              matchers =
                listOf(
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "prisonId",
                    condition = "IS",
                    searchTerm = prisonId,
                  ),
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "cellLocation",
                    condition = "IS",
                    searchTerm = cellLocation,
                  ),
                ),
            ),
          ),
      )

    val searchResponse = prisonerOffenderSearchGateway.attributeSearch(request)
    if (searchResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = searchResponse.errors)
    }

    return Response(data = searchResponse.data?.toPOSPrisoners()?.map { it.toPersonInPrison() }, errors = searchResponse.errors)
  }
}
