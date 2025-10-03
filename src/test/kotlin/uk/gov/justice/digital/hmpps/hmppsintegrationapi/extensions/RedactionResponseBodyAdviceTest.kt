package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.JsonPathResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.globalRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.RedactionService

class RedactionResponseBodyAdviceTest {
  val roleName = "private-prison"
  val examplePath = "/v1/persons"
  val redactionPolicies =
    listOf(
      RedactionPolicy(
        "redaction-policy",
        listOf(JsonPathResponseRedaction(RedactionType.MASK, listOf("$..someAttribute"))),
      ),
    )
  val mockRequest = mock(HttpServletRequest::class.java)

  lateinit var objectMapper: ObjectMapper
  lateinit var authorisationConfig: AuthorisationConfig
  lateinit var redactionService: RedactionService
  lateinit var advice: RedactionResponseBodyAdvice

  @BeforeEach
  fun beforeEach() {
    objectMapper = ObjectMapper()
    authorisationConfig = mock(AuthorisationConfig::class.java)
    redactionService = mock(RedactionService::class.java)

    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles } returns
      mapOf(
        roleName to
          Role(
            name = roleName,
            include = mutableListOf(examplePath),
            redactionPolicies = redactionPolicies,
          ),
      )

    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicyKt")
    every { globalRedactions } returns mapOf()

    whenever(mockRequest.getAttribute("clientName")).thenReturn("clientA")
    whenever(mockRequest.getRequestURI()).thenReturn(examplePath)

    advice = RedactionResponseBodyAdvice(objectMapper, authorisationConfig, redactionService)
  }

  @AfterEach
  fun afterEach() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicyKt")
  }

  @Test
  fun `should return body unchanged when not JSON`() {
    val body = mapOf("field" to "value")

    val result =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.TEXT_PLAIN,
        HttpMessageConverter::class.java,
        mock(),
        mock(),
      )

    result shouldBe body
    verifyNoInteractions(redactionService)
  }

  @Test
  fun `should apply global and role based redaction policies`() {
    val body = mapOf("field" to "value")
    val bodyNode: ObjectNode = objectMapper.valueToTree(body)
    val serverHttpRequest = ServletServerHttpRequest(mockRequest)
    val consumerConfig = ConsumerConfig(emptyList<String>(), null, listOf(roleName))

    val globalRedactionPolicy =
      RedactionPolicy(
        name = "global-redaction-policy",
        responseRedactions =
          listOf(
            JsonPathResponseRedaction(
              type = RedactionType.MASK,
            ),
          ),
      )
    every { globalRedactions } returns
      mapOf(
        "a-global-redaction" to globalRedactionPolicy,
      )

    whenever(authorisationConfig.consumers).thenReturn(mapOf("clientA" to consumerConfig))
    whenever(redactionService.applyPolicies(any(), any(), any())).thenReturn(bodyNode)

    advice.beforeBodyWrite(
      body,
      mock(MethodParameter::class.java),
      MediaType.APPLICATION_JSON,
      HttpMessageConverter::class.java,
      serverHttpRequest,
      mock(ServerHttpResponse::class.java),
    )

    verify(redactionService).applyPolicies(
      examplePath,
      bodyNode,
      buildList {
        add(globalRedactionPolicy)
        addAll(redactionPolicies)
      },
    )
  }

  @Test
  fun `should apply redaction policies when JSON and clientName present`() {
    val body = mapOf("field" to "value")
    val bodyNode: ObjectNode = objectMapper.valueToTree(body)

    val serverHttpRequest = ServletServerHttpRequest(mockRequest)

    val consumerConfig = ConsumerConfig(emptyList<String>(), null, listOf(roleName))

    whenever(authorisationConfig.consumers).thenReturn(mapOf("clientA" to consumerConfig))
    whenever(redactionService.applyPolicies(any(), any(), any())).thenReturn(bodyNode)

    val result =
      advice.beforeBodyWrite(
        body,
        mock(MethodParameter::class.java),
        MediaType.APPLICATION_JSON,
        HttpMessageConverter::class.java,
        serverHttpRequest,
        mock(ServerHttpResponse::class.java),
      )

    result shouldBe bodyNode
    verify(redactionService).applyPolicies(examplePath, bodyNode, redactionPolicies)
  }

  @Test
  fun `should return null when body is null`() {
    val result =
      advice.beforeBodyWrite(
        null,
        mock(MethodParameter::class.java),
        MediaType.APPLICATION_JSON,
        HttpMessageConverter::class.java,
        mock(),
        mock(),
      )

    result shouldBe null
    verifyNoInteractions(redactionService)
  }
}
