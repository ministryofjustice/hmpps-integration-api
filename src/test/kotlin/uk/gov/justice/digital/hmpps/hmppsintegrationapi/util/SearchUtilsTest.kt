package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchDateMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchPncMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.SearchUtils.attributeSearchRequest

internal class SearchUtilsTest :
  DescribeSpec({

    it("creates an attribute search request with PNC when alias search is NOT required") {
      val actual = attributeSearchRequest(pncNumber = "1994/5850111W", lastName = "SMITH", searchWithinAliases = false)
      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchPncMatcher("1994/5850111W"))), // THE PNC matcher
              POSAttributeSearchQuery(
                joinType = "OR",
                subQueries =
                  listOf(
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "lastName", condition = "IS", searchTerm = "SMITH"),
                        ),
                    ),
                  ),
              ),
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }

    it("creates an attribute search request with PNC when alias search IS required") {
      val actual = attributeSearchRequest(pncNumber = "1994/5850111W", lastName = "SMITH", searchWithinAliases = true)
      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchPncMatcher("1994/5850111W"))), // THE PNC matcher
              POSAttributeSearchQuery(
                joinType = "OR",
                subQueries =
                  listOf(
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.lastName", condition = "IS", searchTerm = "SMITH"),
                        ), // THE ALIAS SURNAME
                    ),
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "lastName", condition = "IS", searchTerm = "SMITH"),
                        ), // THE SURNAME
                    ),
                  ),
              ),
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }

    it("creates an attribute search request without PNC when alias search is NOT required") {
      val actual = attributeSearchRequest(firstName = "JOHN", lastName = "SMITH", dateOfBirth = "1974-02-02", searchWithinAliases = false)
      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(
                joinType = "OR",
                subQueries =
                  listOf(
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                  ),
              ),
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }

    it("creates an attribute search request without PNC when alias search IS required") {
      val actual = attributeSearchRequest(firstName = "JOHN", lastName = "SMITH", dateOfBirth = "1974-02-02", searchWithinAliases = true)
      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(
                joinType = "OR",
                subQueries =
                  listOf(
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "aliases.dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                  ),
              ),
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }

    it("creates an attribute search request with prison filters, PNC where alias search IS required") {
      val actual =
        attributeSearchRequest(
          pncNumber = "1994/5850111W",
          firstName = "JOHN",
          lastName = "SMITH",
          dateOfBirth = "1974-02-02",
          searchWithinAliases = true,
          consumerFilters = ConsumerFilters(prisons = listOf("HEI", "MDI")),
        )
      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchPncMatcher("1994/5850111W"))), // THE PNC matcher
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchMatcher(type = "String", attribute = "prisonId", condition = "IN", searchTerm = "HEI,MDI"))), // THE PRISON FILTER
              POSAttributeSearchQuery(
                joinType = "OR",
                subQueries =
                  listOf(
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "aliases.dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                  ),
              ),
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }

    it("creates an attribute search request with X for prison filters when prison filters are an empty list to force no results") {
      val actual =
        attributeSearchRequest(
          pncNumber = "1994/5850111W",
          firstName = "JOHN",
          lastName = "SMITH",
          dateOfBirth = "1974-02-02",
          searchWithinAliases = true,
          consumerFilters = ConsumerFilters(prisons = emptyList()),
        )
      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchPncMatcher("1994/5850111W"))), // THE PNC matcher
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchMatcher(type = "String", attribute = "prisonId", condition = "IN", searchTerm = "X"))), // Empty prisons filter
              POSAttributeSearchQuery(
                joinType = "OR",
                subQueries =
                  listOf(
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "aliases.lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "aliases.dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                    POSAttributeSearchQuery(
                      joinType = "AND",
                      matchers =
                        listOfNotNull(
                          POSAttributeSearchMatcher(type = "String", attribute = "firstName", condition = "IS", searchTerm = "JOHN"),
                          POSAttributeSearchMatcher(type = "String", attribute = "lastName", condition = "IS", searchTerm = "SMITH"),
                          POSAttributeSearchDateMatcher(attribute = "dateOfBirth", date = "1974-02-02"),
                        ),
                    ),
                  ),
              ),
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }

    it("creates an attribute search request for PNC and prison only") {
      val actual =
        attributeSearchRequest(
          pncNumber = "1994/5850111W",
          searchWithinAliases = false,
          consumerFilters = ConsumerFilters(prisons = listOf("WWI")),
        )

      val expectedRequest =
        POSAttributeSearchRequest(
          joinType = "AND",
          queries =
            listOf(
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchPncMatcher("1994/5850111W"))), // THE PNC matcher
              POSAttributeSearchQuery(joinType = "AND", matchers = listOf(POSAttributeSearchMatcher(type = "String", attribute = "prisonId", condition = "IN", searchTerm = "WWI"))), // Empty prisons filter
            ),
        )
      assertThat(actual).isEqualTo(expectedRequest)
    }
  })
