package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchDateMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchPncMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest

@Service
class GetPersonsService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val deliusGateway: NDeliusGateway,
) {
  fun execute(
    firstName: String?,
    lastName: String?,
    pncNumber: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<Person>> {
    val responseFromProbationOffenderSearch =
      deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)

    if (responseFromProbationOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromProbationOffenderSearch.errors)
    }

    if (pncNumber.isNullOrBlank()) {
      val responseFromPrisonerOffenderSearch =
        prisonerOffenderSearchGateway.getPersons(
          firstName,
          lastName,
          dateOfBirth,
          searchWithinAliases,
        )
      return Response(data = responseFromPrisonerOffenderSearch.data.map { it.toPerson() } + responseFromProbationOffenderSearch.data)
    }
    return Response(data = responseFromProbationOffenderSearch.data)
  }

  /**
   * Builds a POSAttributeSearchRequest for the given firstName, lastName, pncNumber, dateOfBirth
   * If searchWithinAliases is set then the request will ALSO perform a search on aliases for firstName, lastName and dateOfBirth
   * (in addition to searching the firstName, lastName and dateOfBirth on the main record)
   */
  fun attributeSearchRequest(
    firstName: String? = null,
    lastName: String? = null,
    pncNumber: String? = null,
    dateOfBirth: String? = null,
    searchWithinAliases: Boolean = false,
  ): POSAttributeSearchRequest =
    POSAttributeSearchRequest(
      joinType = "AND",
      queries =
        listOfNotNull(
          pncNumber?.let {
            POSAttributeSearchQuery(
              joinType = "AND",
              matchers = listOfNotNull(POSAttributeSearchPncMatcher(pncNumber = it)),
            )
          },
          POSAttributeSearchQuery(
            joinType = "OR",
            subQueries =
              listOfNotNull(
                searchWithinAliases.takeIf { it == true }?.let {
                  POSAttributeSearchQuery(
                    joinType = "AND",
                    matchers =
                      listOfNotNull(
                        firstName?.let {
                          POSAttributeSearchMatcher(
                            type = "String",
                            attribute = "aliases.firstName",
                            condition = "IS",
                            searchTerm = it,
                          )
                        },
                        lastName?.let {
                          POSAttributeSearchMatcher(
                            type = "String",
                            attribute = "aliases.lastName",
                            condition = "IS",
                            searchTerm = it,
                          )
                        },
                        dateOfBirth?.let {
                          POSAttributeSearchDateMatcher(
                            attribute = "aliases.dateOfBirth",
                            date = it,
                          )
                        },
                      ),
                  )
                },
                POSAttributeSearchQuery(
                  joinType = "AND",
                  matchers =
                    listOfNotNull(
                      firstName?.let {
                        POSAttributeSearchMatcher(
                          type = "String",
                          attribute = "firstName",
                          condition = "IS",
                          searchTerm = it,
                        )
                      },
                      lastName?.let {
                        POSAttributeSearchMatcher(
                          type = "String",
                          attribute = "lastName",
                          condition = "IS",
                          searchTerm = it,
                        )
                      },
                      dateOfBirth?.let {
                        POSAttributeSearchDateMatcher(
                          attribute = "dateOfBirth",
                          date = it,
                        )
                      },
                    ),
                ),
              ),
          ),
        ),
    )
}
