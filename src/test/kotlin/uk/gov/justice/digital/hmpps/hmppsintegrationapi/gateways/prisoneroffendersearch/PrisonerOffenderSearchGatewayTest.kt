package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisoneroffendersearch

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerOffenderSearchGateway::class],
)
class PrisonerOffenderSearchGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) : DescribeSpec(
    {
      val objectMapper = jacksonObjectMapper()
      val nomsNumber = "mockNomsNumber"
      val postPath = "/global-search?size=9999"
      val getPath = "/prisoner/$nomsNumber"
      val prisonerOffenderSearchApiMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)

      val firstName = "JAMES"
      val lastName = "HOWLETT"
      val dateOfBirth = "1975-02-28"

      beforeEach {
        prisonerOffenderSearchApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)

        whenever(hmppsAuthGateway.getClientToken("Prisoner Offender Search")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        prisonerOffenderSearchApiMockServer.stop()
      }

      describe("#getPersons") {
        beforeEach {
          prisonerOffenderSearchApiMockServer.stubForPost(
            postPath,
            """
            {
              "firstName": "$firstName",
              "lastName": "$lastName",
              "dateOfBirth": "$dateOfBirth",
              "includeAliases": false
            }
            """.removeWhitespaceAndNewlines(),
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/GetPrisonersResponse.json",
            ).readText(),
          )
        }

        it("authenticates using HMPPS Auth with credentials") {
          prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)

          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
        }

        it("returns person(s) when searching on first name, last name and date of birth, in a descending order according to date of birth") {
          val response = prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)
          response.data.count().shouldBe(4)
          response.data.forEach {
            it.firstName.shouldBe(firstName)
            it.lastName.shouldBe(lastName)
          }
          response.data[0]
            .prisonerNumber
            .shouldBe("A5043DY")
          response.data[1]
            .prisonerNumber
            .shouldBe("A5083DY")
          response.data[2]
            .prisonerNumber
            .shouldBe("G9347GV")
          response.data[3]
            .prisonerNumber
            .shouldBe("A7796DY")

          response.data[0].pncNumber.shouldBeNull()
          response.data[1].pncNumber.shouldBe("03/11985X")
          response.data[2].pncNumber.shouldBe("95/289622B")
          response.data[3].pncNumber.shouldBeNull()
        }

        it("returns person(s) when searching on first name only") {
          prisonerOffenderSearchApiMockServer.stubForPost(
            postPath,
            """
            {
              "firstName": "$firstName",
              "includeAliases": false
            }
            """.removeWhitespaceAndNewlines(),
            """
            {
              "content": [
                {
                  "firstName": "$firstName",
                  "lastName": "$lastName"
                }
              ],
              "pageable": {
                  "sort": {
                    "empty": true,
                    "unsorted": true,
                    "sorted": false
                  },
                  "offset": 0,
                  "pageSize": 10,
                  "pageNumber": 0,
                  "paged": true,
                  "unpaged": false
                },
                "totalPages": 2,
                "last": false,
                "totalElements": 4,
                "size": 10,
                "number": 0,
                "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
                },
                "first": true,
                "numberOfElements": 10,
                "empty": false
              }
            }
            """.trimIndent(),
          )

          val response = prisonerOffenderSearchGateway.getPersons(firstName, null, null)
          response.data.count().shouldBe(1)
          response.data
            .first()
            .firstName
            .shouldBe(firstName)
          response.data
            .first()
            .lastName
            .shouldBe(lastName)
        }

        it("returns person(s) when searching on last name only") {
          prisonerOffenderSearchApiMockServer.stubForPost(
            postPath,
            """
            {
              "lastName": "$lastName",
              "includeAliases": false
            }
            """.removeWhitespaceAndNewlines(),
            """
            {
              "content": [
                {
                  "firstName": "$firstName",
                  "lastName": "$lastName"
                }
              ],
              "pageable": {
                "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
                },
                "offset": 0,
                "pageSize": 10,
                "pageNumber": 0,
                "paged": true,
                "unpaged": false
              },
              "totalPages": 2,
              "last": false,
              "totalElements": 4,
              "size": 10,
              "number": 0,
              "sort": {
                "empty": true,
                "unsorted": true,
                "sorted": false
              },
              "first": true,
              "numberOfElements": 10,
              "empty": false
            }
            """.trimIndent(),
          )

          val response = prisonerOffenderSearchGateway.getPersons(null, lastName, null)
          response.data.count().shouldBe(1)
          response.data
            .first()
            .firstName
            .shouldBe(firstName)
          response.data
            .first()
            .lastName
            .shouldBe(lastName)
        }

        it("returns person(s) when searching on date of birth only") {
          prisonerOffenderSearchApiMockServer.stubForPost(
            postPath,
            """
            {
              "includeAliases": false,
              "dateOfBirth": "$dateOfBirth"
            }
            """.removeWhitespaceAndNewlines(),
            """
            {
              "content": [
                {
                  "firstName": "$firstName",
                  "lastName": "$lastName",
                  "dateOfBirth": "$dateOfBirth"
                }
              ],
              "pageable": {
                "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
                },
                "offset": 0,
                "pageSize": 10,
                "pageNumber": 0,
                "paged": true,
                "unpaged": false
              },
              "totalPages": 2,
              "last": false,
              "totalElements": 4,
              "size": 10,
              "number": 0,
              "sort": {
                "empty": true,
                "unsorted": true,
                "sorted": false
              },
              "first": true,
              "numberOfElements": 10,
              "empty": false
            }
            """.trimIndent(),
          )

          val response = prisonerOffenderSearchGateway.getPersons(null, null, dateOfBirth)
          response.data.count().shouldBe(1)
          response.data
            .first()
            .firstName
            .shouldBe(firstName)
          response.data
            .first()
            .lastName
            .shouldBe(lastName)
          response.data
            .first()
            .dateOfBirth
            .shouldBe(LocalDate.parse(dateOfBirth))
        }

        it("returns person(s) when searching within aliases") {
          prisonerOffenderSearchApiMockServer.stubForPost(
            postPath,
            """
            {
              "firstName": "$firstName",
              "includeAliases": true
            }
            """.removeWhitespaceAndNewlines(),
            """
            {
              "content": [
                {
                  "firstName": "Richard",
                  "lastName": "Roger",
                  "aliases": [
                    {
                      "firstName": "$firstName",
                      "lastName": "$lastName"
                    }
                  ]
                }
              ],
               "pageable": {
                  "sort": {
                    "empty": true,
                    "unsorted": true,
                    "sorted": false
                  },
                  "offset": 0,
                  "pageSize": 10,
                  "pageNumber": 0,
                  "paged": true,
                  "unpaged": false
                },
                "totalPages": 2,
                "last": false,
                "totalElements": 4,
                "size": 10,
                "number": 0,
                "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
                },
                "first": true,
                "numberOfElements": 10,
                "empty": false
              }
            }
            """.trimIndent(),
          )

          val response = prisonerOffenderSearchGateway.getPersons(firstName, null, null, true)
          response.data.count().shouldBe(1)
          response.data
            .first()
            .aliases
            .first()
            .firstName
            .shouldBe(firstName)
          response.data
            .first()
            .aliases
            .first()
            .lastName
            .shouldBe(lastName)
        }

        it("returns an empty list of Person if no matching person") {
          val firstNameThatDoesNotExist = "ZYX321"
          val lastNameThatDoesNotExist = "GHJ345"

          prisonerOffenderSearchApiMockServer.stubForPost(
            postPath,
            """
            {
              "firstName": "$firstNameThatDoesNotExist",
              "lastName": "$lastNameThatDoesNotExist",
              "includeAliases": false
            }
            """.removeWhitespaceAndNewlines(),
            """
            {
              "content": [],
              "pageable": {
                "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
                },
                "offset": 0,
                "pageSize": 10,
                "pageNumber": 0,
                "paged": true,
                "unpaged": false
              },
              "totalPages": 2,
              "last": false,
              "totalElements": 4,
              "size": 10,
              "number": 0,
              "sort": {
                "empty": true,
                "unsorted": true,
                "sorted": false
              },
              "first": true,
              "numberOfElements": 10,
              "empty": false
            }
            """,
          )

          val response = prisonerOffenderSearchGateway.getPersons(firstNameThatDoesNotExist, lastNameThatDoesNotExist, null)
          response.data.shouldBeEmpty()
        }
      }

      describe("#getPrisonOffender") {
        beforeEach {
          prisonerOffenderSearchApiMockServer.stubForGet(
            getPath,
            """
            {
              "prisonerNumber": "A7796DY",
              "bookingId": "599877",
              "firstName": "$firstName",
              "middleNames": "MARTIN",
              "lastName": "$lastName",
              "maritalStatus": "Widowed"
            }
            """.trimIndent(),
          )
        }

        it("authenticates using HMPPS Auth with credentials") {
          prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)
          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Prisoner Offender Search")
        }

        it("returns reasonable adjustment for a person with the matching ID") {
          val response = prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)
          response.data?.prisonerNumber.shouldBe("A7796DY")
          response.data?.bookingId.shouldBe("599877")
          response.data?.firstName.shouldBe(firstName)
          response.data?.middleNames.shouldBe("MARTIN")
          response.data?.lastName.shouldBe(lastName)
          response.data?.maritalStatus.shouldBe("Widowed")
        }

        it("returns an error when 404 NOT FOUND is returned") {
          prisonerOffenderSearchApiMockServer.stubForGet(
            getPath,
            """
            {
              "developerMessage": "cannot find person"
            }
            """,
            HttpStatus.NOT_FOUND,
          )

          val response = prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)
          response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
        }
      }

      describe("#attributeSearch") {
        val prisonId = "MKI"
        val cellLocation = "A-1-001"
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

        it("authenticates using HMPPS Auth with credentials") {
          prisonerOffenderSearchGateway.attributeSearch(request)
          verify(hmppsAuthGateway, times(1)).getClientToken("Prisoner Offender Search")
        }

        it("returns a prisoner by attributes") {
          prisonerOffenderSearchApiMockServer.stubForPost(
            "/attribute-search",
            objectMapper.writeValueAsString(request),
            """
            {
              "content": [
                {
                  "prisonId": "$prisonId",
                  "firstName": "$firstName",
                  "lastName": "$lastName",
                  "cellLocation": "$cellLocation"
                }
              ],
              "pageable": {
                "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
                },
                "offset": 0,
                "pageSize": 10,
                "pageNumber": 0,
                "paged": true,
                "unpaged": false
              },
              "totalPages": 1,
              "last": false,
              "totalElements": 1,
              "size": 10,
              "number": 0,
              "sort": {
                "empty": true,
                "unsorted": true,
                "sorted": false
              },
              "first": true,
              "numberOfElements": 1,
              "empty": false
            }
            """.trimIndent(),
          )

          val response = prisonerOffenderSearchGateway.attributeSearch(request)
          response.data.shouldNotBeNull()
          response.data.content.size
            .shouldBe(1)
          response.data.content[0]
            .prisonId
            .shouldBe(prisonId)
          response.data.content[0]
            .cellLocation
            .shouldBe(cellLocation)
        }

        it("throws an exception when 400 BAD REQUEST is returned") {
          prisonerOffenderSearchApiMockServer.stubForPost(
            "/attribute-search",
            objectMapper.writeValueAsString(request),
            """
            {
              "status": 400,
              "developerMessage": "Bad request",
              "errorCode": 20012,
              "userMessage": "Bad request",
              "moreInfo": "Bad request"
            }
            """.trimIndent(),
            HttpStatus.BAD_REQUEST,
          )

          shouldThrow<WebClientResponseException> { prisonerOffenderSearchGateway.attributeSearch(request) }
        }
      }
    },
  )
