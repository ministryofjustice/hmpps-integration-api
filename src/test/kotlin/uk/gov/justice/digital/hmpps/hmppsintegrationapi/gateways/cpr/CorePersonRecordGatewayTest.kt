package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.cpr

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import jakarta.validation.ValidationException
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CorePersonRecordGateway::class],
)
class CorePersonRecordGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val corePersonRecordGateway: CorePersonRecordGateway,
) : DescribeSpec(
    {
      val cprMockServer = ApiMockServer.Companion.create(UpstreamApi.CORE_PERSON_RECORD)
      val objectMapper = jacksonObjectMapper()
      val hmppsId = "AB123123"

      beforeEach {
        cprMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("CORE_PERSON_RECORD")).thenReturn(
          HmppsAuthMockServer.Companion.TOKEN,
        )
        cprMockServer.stubForGet("/person/probation/$hmppsId", objectMapper.writeValueAsString(CorePersonRecord()), HttpStatus.OK)
        cprMockServer.stubForGet("/person/prison/$hmppsId", objectMapper.writeValueAsString(CorePersonRecord()), HttpStatus.OK)
      }

      afterTest {
        cprMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        corePersonRecordGateway.corePersonRecordFor("prison", hmppsId)
        verify(hmppsAuthGateway, times(1))
          .getClientToken("CORE_PERSON_RECORD")
      }

      it("upstream API returns a Not Found error, throw EntityNotFoundException") {
        cprMockServer.stubForGet("/person/probation/$hmppsId", "", HttpStatus.NOT_FOUND)
        val response =
          shouldThrow<EntityNotFoundException> {
            corePersonRecordGateway.corePersonRecordFor("probation", hmppsId)
          }
        response.message.shouldContain("Could not find core person record at /person/probation/$hmppsId")
      }

      it("upstream API returns a bad request error, throw IllegalArgumentException") {
        cprMockServer.stubForGet("/person/prison/$hmppsId", "", HttpStatus.BAD_REQUEST)
        val response =
          shouldThrow<ValidationException> {
            corePersonRecordGateway.corePersonRecordFor("prison", hmppsId)
          }
        response.message.shouldContain("Invalid request to core person record /person/prison/$hmppsId")
      }

      it("upstream API returns an unexpected exception, throw RuntimeException") {
        val client = Mockito.mock(WebClientWrapper::class.java)
        val mockResponse = WebClientWrapper.WebClientWrapperResponse.Error(emptyList())
        whenever(client.getResponseBodySpec(any(), any(), any(), isNull())).thenThrow(
          WebClientResponseException(
            500,
            "Error",
            null,
            null,
            null,
          ),
        )
        whenever(client.getErrorType(any(), any(), any(), any())).thenReturn(mockResponse)
        ReflectionTestUtils.setField(corePersonRecordGateway, "webClient", client)
        val response =
          shouldThrow<RuntimeException> {
            corePersonRecordGateway.corePersonRecordFor("probation", hmppsId)
          }
        response.message.shouldContain("Error retrieving core person record from /person/probation/$hmppsId")
      }
    },
  )
