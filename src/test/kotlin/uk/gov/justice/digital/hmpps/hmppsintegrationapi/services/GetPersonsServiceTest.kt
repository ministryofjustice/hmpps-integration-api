package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchDateMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchPncMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPaginatedPrisoners
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSSort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService.Companion.attributeSearchRequest

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonsService::class],
)
internal class GetPersonsServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val deliusGateway: NDeliusGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val getPersonsService: GetPersonsService,
) : DescribeSpec({
    val firstName = personInProbationAndNomisPersona.firstName
    val lastName = personInProbationAndNomisPersona.lastName
    val pncNumber = "2003/13116M"
    val dateOfBirth = personInProbationAndNomisPersona.dateOfBirth.toString()
    val prisoners = listOf(POSPrisoner(firstName = firstName, lastName = lastName, middleNames = "Gary", youthOffender = false))
    val paginatedPrisoners =
      POSPaginatedPrisoners(
        content = prisoners,
        totalElements = 1,
        totalPages = 1,
        first = true,
        last = true,
        size = 10,
        number = 0,
        sort =
          POSSort(
            empty = false,
            sorted = false,
            unsorted = false,
          ),
        numberOfElements = 1,
        pageable =
          POSPageable(
            offset = 0,
            sort =
              POSSort(
                empty = false,
                sorted = false,
                unsorted = false,
              ),
            pageSize = 10,
            pageNumber = 1,
            paged = true,
            unpaged = false,
          ),
        empty = false,
      )
    val prisonAttributeSearchResponse =
      Response<POSPaginatedPrisoners?>(
        data = paginatedPrisoners,
      )

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(deliusGateway)
      Mockito.reset(featureFlag)

      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(data = emptyList()))
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(data = emptyList()))
    }

    it("search returns prisoner") {
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(prisonAttributeSearchResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters(prisons = listOf("MDI")))
      response.data.size.shouldBe(1)
      verify(prisonerOffenderSearchGateway, times(1)).attributeSearch(any())
    }

    it("search with prisons filter (no probation search) returns an error") {
      val error = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.TEST)
      val paginatedResponse = Response<POSPaginatedPrisoners?>(errors = listOf(error), data = null)
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(paginatedResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters(prisons = listOf("MDI")))
      response.data.shouldBeEmpty()
      response.errors.shouldBe(listOf(error))
    }

    it("search without prisons filter continues to call prison search (success) if probation search fails") {
      val error = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.TEST)
      val responseFromProbationOffenderSearch = Response(errors = listOf(error), data = emptyList<Person>())
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(prisonAttributeSearchResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters())
      response.data.size.shouldBe(1)
      response.errors.shouldBe(listOf(error))
    }

    it("search without prisons filter continues to call prison search (failure) if probation search fails") {
      val error = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.TEST)
      val paginatedResponse = Response<POSPaginatedPrisoners?>(errors = listOf(error), data = null)
      val responseFromProbationOffenderSearch = Response(errors = listOf(error), data = emptyList<Person>())
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(paginatedResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters())
      response.data.shouldBeEmpty()
      response.errors.shouldBe(listOf(error, error))
    }

    it("does not do probation search for supervisionStatus == PRISON") {
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(prisonAttributeSearchResponse)

      val filters = ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PRISONS.name))

      getPersonsService.personAttributeSearch(firstName, lastName, null, dateOfBirth, true, filters)

      verifyNoInteractions(deliusGateway)
      verify(prisonerOffenderSearchGateway, times(1)).attributeSearch(any())
    }

    it("search returns an empty list when no person(s) are found") {
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(emptyList()))
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(
        Response<POSPaginatedPrisoners?>(
          data = paginatedPrisoners.copy(content = emptyList()),
        ),
      )
      val response = getPersonsService.personAttributeSearch(firstName, lastName, null, dateOfBirth)
      response.data.shouldBe(emptyList())
    }

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
