package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [AddressController::class])
@ActiveProfiles("test")
internal class AddressControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getAddressesForPersonService: GetAddressesForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "2003/13116M"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val basePath = "/v1/persons"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $basePath/{encodedHmppsId}/addresses") {
        beforeTest {
          Mockito.reset(getAddressesForPersonService)
          Mockito.reset(auditService)
          whenever(getAddressesForPersonService.execute(hmppsId)).thenReturn(Response(data = listOf(generateTestAddress())))
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the addresses for a person with the matching ID") {
          mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

          verify(getAddressesForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
        }

        it("returns the addresses for a person with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")
          result.response.contentAsString.shouldBe(
            """
        {
          "data": [
            {
              "country": "England",
              "county": "Greater London",
              "endDate": "2023-05-20",
              "locality": "London Bridge",
              "name": "The chocolate factory",
              "noFixedAddress": false,
              "number": "89",
              "postcode": "SE1 1TZ",
              "startDate": "2021-05-10",
              "street": "Omeara",
              "town": "London Town",
              "types": [
                {
                  "code": "A99",
                  "description": "Chocolate Factory"
                },
                {
                  "code": "B99",
                  "description": "Glass Elevator"
                }
              ],
              "notes": "some interesting note"
            }
          ]
        }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")
          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_ADDRESS", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in all upstream APIs") {
          whenever(getAddressesForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                  UpstreamApiError(
                    causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 200 OK status code when person is found in one upstream API but not another") {
          whenever(getAddressesForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

          whenever(getAddressesForPersonService.execute(hmppsId)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")
          assert(result.response.status == 500)
          assert(
            result.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }
      }
    },
  )
