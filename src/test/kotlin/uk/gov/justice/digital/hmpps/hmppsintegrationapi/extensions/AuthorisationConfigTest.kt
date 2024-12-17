package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@ActiveProfiles("test")
@EnableConfigurationProperties(AuthorisationConfig::class)
class AuthorisationConfigTest {
//  fun `will extract included endpoints into config`() {
//    val config = """
//      authorisation:
//        test-consumer:
//          - example/endpoint
//    """.trimIndent()
//
//    val actual = Something.convert(config, AuthorisationConfig.class)
//    actual.shouldBe(AuthorisationConfig(mapOf("test-consumer" to listOf("example/endpoint"))))
//  }

  @Test
  fun `will extract create empty list of endpoints if missing`() {
    val config = """
      authorisation:
        test-consumer:
    """.trimIndent()

    val actual = ConsumerConfigConverter().convert(config)
    val expected = AuthorisationConfig()
    expected.consumers = mapOf("test-consumer" to ConsumerConfig(emptyList<String>()))
    actual.shouldBe(expected.consumers.get("test-consumer"))
  }

  @Test
  fun `will extract includes`() {
    val config = """
      authorisation:
        test-consumer:
          include:
            - "/v1/prison"
    """.trimIndent()

    val actual = ConsumerConfigConverter().convert(config)
    val expected = AuthorisationConfig()
    expected.consumers = mapOf("test-consumer" to ConsumerConfig(listOf("/v1/prison")))
    actual.shouldBe(expected.consumers.get("test-consumer"))
  }
}
