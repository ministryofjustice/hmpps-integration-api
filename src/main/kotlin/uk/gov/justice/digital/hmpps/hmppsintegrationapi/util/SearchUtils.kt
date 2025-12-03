package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchDateMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchPncMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

object SearchUtils {
  /**
   * Builds a POSAttributeSearchRequest for the given firstName, lastName, pncNumber, dateOfBirth and prison filters
   * If searchWithinAliases is set then the request will ALSO perform a search on aliases for firstName, lastName and dateOfBirth
   * (in addition to searching the firstName, lastName and dateOfBirth on the main record)
   */
  fun attributeSearchRequest(
    firstName: String? = null,
    lastName: String? = null,
    pncNumber: String? = null,
    dateOfBirth: String? = null,
    searchWithinAliases: Boolean = false,
    consumerFilters: ConsumerFilters? = null,
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
          consumerFilters?.hasPrisonFilter().takeIf { it == true }?.let {
            POSAttributeSearchQuery(
              joinType = "AND",
              matchers =
                listOfNotNull(
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "prisonId",
                    condition = "IN",
                    // If there is an empty prisons list then set the search criteria to X to force no search results
                    searchTerm = consumerFilters?.prisons?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",") ?: "X",
                  ),
                ),
            )
          },
          takeIf { firstName != null || lastName != null || dateOfBirth != null }?.let {
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
            )
          },
        ),
    )
}
