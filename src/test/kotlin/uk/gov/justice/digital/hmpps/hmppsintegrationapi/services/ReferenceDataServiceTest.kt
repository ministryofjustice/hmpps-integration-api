package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReferenceData
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ReferenceDataService::class],
)
class ReferenceDataServiceTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val referenceDataService: ReferenceDataService,
  private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule(),
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val ndeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)

      beforeEach {
        // Delius endpoint
        ndeliusApiMockServer.start()
        ndeliusApiMockServer.stubForGet(
          "/reference-data",
          """
            {
              "probationReferenceData": {
                "GENDER": [
                  {
                   "code": "M",
                   "description": "Male"
                  },
                  {
                   "code": "F",
                   "description": "Female"
                  }
                ],
                "ADDRESS_TYPE": [
                  {
                   "code": "A1",
                   "description": "Delius address status 1"
                  },
                  {
                   "code": "A2",
                   "description": "Delius address status 2"
                  }
                ]
              }
            }
          """,
        )

        // Nomis endpoints
        //  PHONE_USAGE("PHONE_TYPE"), ALERT("ALERT_TYPE"), ETHNICITY("ETHNICITY"), SEX("GENDER")
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/PHONE_USAGE",
          """
          [
            {"domain":"PHONE_USAGE", "code":"a", "description":"desc_a"},
            {"domain":"PHONE_USAGE", "code":"b", "description":"desc_b"},
            {"domain":"PHONE_USAGE", "code":"c", "description":"desc_c"}
          ]
        """,
        )

        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/ALERT",
          """
          [
            {"domain":"ALERT", "code":"a", "description":"desc_a"},
            {"domain":"ALERT", "code":"b", "description":"desc_b"},
            {"domain":"ALERT", "code":"c", "description":"desc_c"}
          ]
        """,
        )

        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/ETHNICITY",
          """
          [
            {"domain":"ETHNICITY", "code":"a", "description":"desc_a"},
            {"domain":"ETHNICITY", "code":"b", "description":"desc_b"},
            {"domain":"ETHNICITY", "code":"c", "description":"desc_c"}
          ]
        """,
        )

        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/SEX",
          """
          [
            {"domain":"SEX", "code":"a", "description":"desc_a"},
            {"domain":"SEX", "code":"b", "description":"desc_b"},
            {"domain":"SEX", "code":"c", "description":"desc_c"}
          ]
        """,
        )

        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/ADDRESS_TYPE",
          """
          [
            {"domain":"ADDRESS_TYPE", "code":"a", "description":"prison ADDRESS_TYPE_a"},
            {"domain":"ADDRESS_TYPE", "code":"b", "description":"prison ADDRESS_TYPE_b"},
            {"domain":"ADDRESS_TYPE", "code":"c", "description":"prison ADDRESS_TYPE_c"}
          ]
        """,
        )

        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/ADDR_TYPE",
          """
          [
            {"domain":"ADDR_TYPE", "code":"ta", "description":"prison ADDR_TYPE_ta"},
            {"domain":"ADDR_TYPE", "code":"tb", "description":"prison ADDR_TYPE_tb"},
            {"domain":"ADDR_TYPE", "code":"tc", "description":"prison ADDR_TYPE_tc"}
          ]
        """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
        ndeliusApiMockServer.stop()
      }

      it("returns from function with errors on NDelius 404") {
        ndeliusApiMockServer.stubForGet(
          "/reference-data",
          """
            {"message":"There is an error"}
          """,
          HttpStatus.NOT_FOUND,
        )

        val response = referenceDataService.referenceData()
        response.data.shouldBe(null)
        response.errors[0]
          .causedBy.name
          .shouldBe("NDELIUS")
        response.errors[0]
          .type.name
          .shouldBe("ENTITY_NOT_FOUND")
      }

      it("returns from function with errors on NOMIS 404") {
        nomisApiMockServer.stubForGet(
          "/api/reference-domains/domains/SEX",
          """
            {"message":"There is an error"}
          """,
          HttpStatus.NOT_FOUND,
        )

        val response = referenceDataService.referenceData()
        response.data.shouldBe(null)
        response.errors[0]
          .causedBy.name
          .shouldBe("PRISON_API")
        response.errors[0]
          .type.name
          .shouldBe("ENTITY_NOT_FOUND")
      }

      it("returns successfully") {
        val expectedData =
          """
          {
              "prisonReferenceData": {
                "PHONE_TYPE": [
                  {
                    "code": "a",
                    "description": "desc_a"
                  },
                  {
                    "code": "b",
                    "description": "desc_b"
                  },
                  {
                    "code": "c",
                    "description": "desc_c"
                  }
                ],
                "ALERT_TYPE": [
                  {
                    "code": "a",
                    "description": "desc_a"
                  },
                  {
                    "code": "b",
                    "description": "desc_b"
                  },
                  {
                    "code": "c",
                    "description": "desc_c"
                  }
                ],
                "ETHNICITY": [
                  {
                    "code": "a",
                    "description": "desc_a"
                  },
                  {
                    "code": "b",
                    "description": "desc_b"
                  },
                  {
                    "code": "c",
                    "description": "desc_c"
                  }
                ],
                "GENDER": [
                  {
                    "code": "a",
                    "description": "desc_a"
                  },
                  {
                    "code": "b",
                    "description": "desc_b"
                  },
                  {
                    "code": "c",
                    "description": "desc_c"
                  }
                ],
                "ADDRESS_TYPE": [
                  {
                    "code":"a",
                    "description":"prison ADDRESS_TYPE_a"
                  },
                  {
                    "code":"b",
                    "description":"prison ADDRESS_TYPE_b"
                  },
                  {
                    "code":"c",
                    "description":"prison ADDRESS_TYPE_c"
                  },
                  {
                    "code":"ta",
                    "description":"prison ADDR_TYPE_ta"
                  },
                  {
                    "code":"tb",
                    "description":"prison ADDR_TYPE_tb"},
                  {
                    "code":"tc",
                    "description":"prison ADDR_TYPE_tc"
                  }
                ]
              },
              "probationReferenceData": {
                "GENDER": [
                  {
                    "code": "M",
                    "description": "Male"
                  },
                  {
                    "code": "F",
                    "description": "Female"
                  }
                ],
                "ADDRESS_TYPE": [
                  {
                   "code": "A1",
                   "description": "Delius address status 1"
                  },
                  {
                   "code": "A2",
                   "description": "Delius address status 2"
                  }
                ]
              }
          }
          """.trimIndent()
        val response = referenceDataService.referenceData()
        val actual = objectMapper.writeValueAsString(response.data)
        val exp = objectMapper.writeValueAsString(objectMapper.readValue(expectedData, ReferenceData::class.java))
        response.errors.shouldBe(emptyList())
        actual.shouldBe(exp)
      }
    },
  )
