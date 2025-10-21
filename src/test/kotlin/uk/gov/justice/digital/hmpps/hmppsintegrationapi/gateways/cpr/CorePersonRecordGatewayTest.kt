package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.cpr

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

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
      val crn = "AB123123"
      val nomsId = "G2996UX"

      beforeEach {
        cprMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("CORE_PERSON_RECORD")).thenReturn(
          HmppsAuthMockServer.Companion.TOKEN,
        )
        cprMockServer.stubForGet("/person/probation/$crn", objectMapper.writeValueAsString(CorePersonRecord(identifiers = Identifiers(prisonNumbers = listOf(nomsId)))), HttpStatus.OK)
        cprMockServer.stubForGet("/person/prison/$nomsId", objectMapper.writeValueAsString(CorePersonRecord(identifiers = Identifiers(crns = listOf(crn)))), HttpStatus.OK)
      }

      afterTest {
        cprMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        corePersonRecordGateway.corePersonRecordFor(IdentifierType.NOMS, nomsId)
        verify(hmppsAuthGateway, times(1))
          .getClientToken("CORE_PERSON_RECORD")
      }

      it("upstream API successfully returns a core person record error for /person/probation") {
        val response = corePersonRecordGateway.corePersonRecordFor(IdentifierType.CRN, crn)
        response.getIdentifier(IdentifierType.NOMS).shouldBe(nomsId)
        cprMockServer.assertValidationPassed()
      }

      it("upstream API successfully returns a core person record error for /person/prison") {
        val response = corePersonRecordGateway.corePersonRecordFor(IdentifierType.NOMS, nomsId)
        response.getIdentifier(IdentifierType.CRN).shouldBe(crn)
        cprMockServer.assertValidationPassed()
      }

      it("upstream API returns a Not Found error, throw EntityNotFoundException") {
        cprMockServer.stubForGet("/person/probation/$crn", "", HttpStatus.NOT_FOUND)
        val response =
          shouldThrow<EntityNotFoundException> {
            corePersonRecordGateway.corePersonRecordFor(IdentifierType.CRN, crn)
          }
        response.message.shouldContain("Could not find core person record at /person/probation/$crn")
      }

      it("upstream API returns a bad request error, throw IllegalArgumentException") {
        cprMockServer.stubForGet("/person/prison/$crn", "", HttpStatus.BAD_REQUEST)
        val response =
          shouldThrow<ValidationException> {
            corePersonRecordGateway.corePersonRecordFor(IdentifierType.NOMS, crn)
          }
        response.message.shouldContain("Invalid request to core person record /person/prison/$crn")
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
            corePersonRecordGateway.corePersonRecordFor(IdentifierType.CRN, crn)
          }
        response.message.shouldContain("Error retrieving core person record from /person/probation/$crn")
      }
    },
  )
