package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.ResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

class RedactionResponseBodyAdviceTest {
  private lateinit var objectMapper: ObjectMapper
  private lateinit var authorisationConfig: AuthorisationConfig
  private lateinit var accessFor: GetCaseAccess
  private lateinit var telemetryService: TelemetryService
  private lateinit var advice: RedactionResponseBodyAdvice

  private val roleName = "private-prison"
  private val examplePath = "/v1/persons"
  private lateinit var redactionContext: RedactionContext

  @BeforeEach
  fun setup() {
    objectMapper = ObjectMapper().registerKotlinModule()
    authorisationConfig = mock(AuthorisationConfig::class.java)

    // Mock global redactions
    val redaction = mock(ResponseRedaction::class.java)
    whenever(redaction.apply(any(), any(), any())).thenAnswer { it.arguments[1] } // no-op

    val globalPolicy =
      RedactionPolicy(
        name = "global-policy",
        responseRedactions = listOf(redaction),
      )

    accessFor = mock(GetCaseAccess::class.java)
    telemetryService = mock(TelemetryService::class.java)

    redactionContext = RedactionContext(examplePath, accessFor, telemetryService)
    // mock the roles registry globally
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles } returns
      mapOf(
        roleName to
          Role(
            name = roleName,
            permissions = mutableListOf(examplePath),
            redactionPolicies = listOf(globalPolicy),
          ),
      )

    advice = RedactionResponseBodyAdvice(authorisationConfig, accessFor, telemetryService)
  }

  @AfterEach
  fun cleanup() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  // -------------------------------------------------------------

  @Test
  fun `should return null when body is null`() {
    val result =
      advice.beforeBodyWrite(
        null,
        mock(MethodParameter::class.java),
        MediaType.APPLICATION_JSON,
        HttpMessageConverter::class.java,
        mock(ServerHttpRequest::class.java),
        mock(ServerHttpResponse::class.java),
      )

    result shouldBe null
  }

  // -------------------------------------------------------------

  @Test
  fun `should return body unchanged when not JSON`() {
    val body = mapOf("field" to "value")

    val result =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.TEXT_PLAIN,
        HttpMessageConverter::class.java,
        mock(ServerHttpRequest::class.java),
        mock(ServerHttpResponse::class.java),
      )

    result shouldBe body
  }

  // -------------------------------------------------------------

  @Test
  fun `should return body unchanged when not successful`() {
    val body = mapOf("error" to "value")
    val servletResponse = mock(HttpServletResponse::class.java)
    whenever(servletResponse.status).thenReturn(HttpServletResponse.SC_BAD_REQUEST)
    val response = ServletServerHttpResponse(servletResponse)
    val result =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.TEXT_PLAIN,
        HttpMessageConverter::class.java,
        mock(ServerHttpRequest::class.java),
        response,
      )

    result shouldBe body
  }

  // -------------------------------------------------------------

  @Test
  fun `should apply redaction policies when JSON and clientName present`() {
    val serverHttpRequest = mock(HttpServletRequest::class.java)

    val servletResponse = mock(HttpServletResponse::class.java)
    whenever(servletResponse.status).thenReturn(HttpServletResponse.SC_OK)
    val response = ServletServerHttpResponse(servletResponse)

    whenever(serverHttpRequest.getAttribute("clientName")).thenReturn("clientA")
    whenever(serverHttpRequest.requestURI).thenReturn(examplePath)

    val servletRequest = ServletServerHttpRequest(serverHttpRequest)

    val consumerConfig = ConsumerConfig(emptyList(), null, listOf(roleName))
    whenever(authorisationConfig.consumers).thenReturn(mapOf("clientA" to consumerConfig))

    // Create a spy redaction to verify that it's invoked
    val redaction = mock(ResponseRedaction::class.java)
    val redactedBody = mapOf("field" to "*** REDACTED ***")

    whenever(redaction.apply(any(), any(), any())).thenReturn(redactedBody)

    val rolePolicy = RedactionPolicy("role-policy", listOf(redaction))

    every { roles } returns
      mapOf(
        roleName to
          Role(
            name = roleName,
            permissions = mutableListOf(examplePath),
            redactionPolicies = listOf(rolePolicy),
          ),
      )

    val body = DataResponse(mapOf("field" to "value"))

    val result =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.APPLICATION_JSON,
        HttpMessageConverter::class.java,
        servletRequest,
        response,
      )

    verify(redaction, times(1)).apply(any(), any(), any())
    result shouldBe redactedBody
  }

  // -------------------------------------------------------------

  @Test
  fun `should gracefully handle client without roles`() {
    val serverHttpRequest = mock(HttpServletRequest::class.java)
    val servletResponse = mock(HttpServletResponse::class.java)
    whenever(servletResponse.status).thenReturn(HttpServletResponse.SC_OK)
    val response = ServletServerHttpResponse(servletResponse)
    whenever(serverHttpRequest.getAttribute("clientName")).thenReturn("unknownClient")
    whenever(serverHttpRequest.requestURI).thenReturn(examplePath)

    val servletRequest = ServletServerHttpRequest(serverHttpRequest)
    whenever(authorisationConfig.consumers).thenReturn(emptyMap())

    val body = mapOf("field" to "value")

    val result =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.APPLICATION_JSON,
        HttpMessageConverter::class.java,
        servletRequest,
        response,
      )

    // Should not throw, should still return body
    result shouldBe body
  }

  // -------------------------------------------------------------

  companion object {
    @JvmStatic
    fun laoArguments() =
      listOf(
        // Case is not lao but an lao only reject - should not mask
        Arguments.of(false, true, "crn", "unmaskedValue", false),
        // Case is not lao but not an LAO only rejection - should continue to MASK
        Arguments.of(false, false, "crn", "**REDACTED**", false),
        // Case is an lao but not an LAO only rejection - should continue to MASK
        Arguments.of(true, false, "crn", "**REDACTED**", false),
        // Case is an lao and not an LAO only rejection - should continue to MASK
        Arguments.of(true, true, "crn", "**REDACTED**", false),
        // Unable to find a crn in order to redact - throws exception
        Arguments.of(true, true, null, "", true),
      )
  }

  @ParameterizedTest
  @MethodSource("laoArguments")
  fun `should handle lao redactions appropriately depending on whether the case is lao`(
    caseIsLao: Boolean,
    policyIsLao: Boolean,
    crn: String?,
    expected: String,
    expectLaoException: Boolean = false,
  ) {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles } returns
      mapOf(
        roleName to
          Role(
            name = roleName,
            permissions = mutableListOf(examplePath),
            redactionPolicies =
              listOf(
                redactionPolicy(
                  "lao-redactions",
                ) {
                  responseRedactions {
                    jsonPath {
                      laoOnly(policyIsLao)
                      endpoints {
                        -examplePath
                      }
                      redactions {
                        -("$..test" to MASK)
                      }
                    }
                  }
                },
              ),
          ),
      )

    val servletResponse = mock(HttpServletResponse::class.java)
    whenever(servletResponse.status).thenReturn(HttpServletResponse.SC_OK)
    val response = ServletServerHttpResponse(servletResponse)
    val serverHttpRequest = mock(HttpServletRequest::class.java)
    whenever(serverHttpRequest.requestURI).thenReturn(examplePath)
    val servletRequest = ServletServerHttpRequest(serverHttpRequest)
    whenever(accessFor.getAccessFor(any())).thenReturn(CaseAccess("crn", caseIsLao, false))
    whenever(serverHttpRequest.getAttribute("clientName")).thenReturn("clientA")
    whenever(serverHttpRequest.getAttribute("hmppsId")).thenReturn(crn)
    val consumerConfig = ConsumerConfig(emptyList(), null, listOf(roleName))
    whenever(authorisationConfig.consumers).thenReturn(mapOf("clientA" to consumerConfig))
    val body = DataResponse(mapOf("test" to "unmaskedValue"))

    fun callFunction(): Any? =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.APPLICATION_JSON,
        HttpMessageConverter::class.java,
        servletRequest,
        response,
      )

    if (expectLaoException) {
      val exception =
        assertThrows<LimitedAccessFailedException> {
          callFunction()
        }
      exception.message shouldBe "No hmppsId available for LAO check"
    } else {
      val result = callFunction()
      result shouldBe DataResponse(mapOf("test" to expected))
    }
  }
}
