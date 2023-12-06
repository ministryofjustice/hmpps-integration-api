package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AuthoriseConsumerService::class],
)
internal class AuthoriseConsumerServiceTest(
  private val subjectDistinguishedName: String = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client",
  private val authoriseConsumerService: AuthoriseConsumerService,
) : DescribeSpec(
  {

    var consumerPathConfig = mapOf(
      "automated-test-client" to listOf("/persons/.*"),
    )

    var requestedPath = "/persons/123"

    describe("Access is allowed") {
      it("when the path is listed under that consumer") {
        val authResult = authoriseConsumerService.execute(
          subjectDistinguishedName,
          consumerPathConfig,
          requestedPath,
        )
        authResult.shouldBeTrue()
      }
    }

    describe("Access is denied") {
      it("when the extracted consumer is null") {
        val result = authoriseConsumerService.execute("", emptyMap(), "")

        result.shouldBeFalse()
      }

      it("when the path isn't listed as allowed on the consumer") {
        requestedPath = "/some-other-path/123"

        val result = authoriseConsumerService.execute(subjectDistinguishedName, consumerPathConfig, requestedPath)

        result.shouldBeFalse()
      }
    }
  },
)
