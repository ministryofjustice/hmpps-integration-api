package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AuthoriseConsumerService::class],
)
internal class AuthoriseConsumerServiceTest(
  private val authoriseConsumerService: AuthoriseConsumerService,
) : DescribeSpec(
    {
      val requestedPath = "/persons/123"

      describe("doesConsumerHaveIncludesAccess") {
        val consumerConfig = ConsumerConfig(listOf("/persons/.*"), listOf(), null)

        it("access is allowed when the path is listed under that consumer") {
          val authResult =
            authoriseConsumerService.doesConsumerHaveIncludesAccess(
              consumerConfig,
              requestedPath,
            )
          authResult.shouldBeTrue()
        }

        it("access is denied when the extracted consumer is null") {
          val result = authoriseConsumerService.doesConsumerHaveIncludesAccess(null, "")
          result.shouldBeFalse()
        }

        it("when the path isn't listed as allowed on the consumer") {
          val invalidPath = "/some-other-path/123"
          val result = authoriseConsumerService.doesConsumerHaveIncludesAccess(consumerConfig, invalidPath)
          result.shouldBeFalse()
        }
      }

      describe("doesConsumerHaveRoleAccess") {
        val consumerRolesInclude = listOf("/persons/.*")

        it("access is allowed when the path is listed in the role included paths") {
          val authResult =
            authoriseConsumerService.doesConsumerHaveRoleAccess(
              consumerRolesInclude,
              requestedPath,
            )
          authResult.shouldBeTrue()
        }

        it("access is denied when the role has no included paths") {
          val result = authoriseConsumerService.doesConsumerHaveRoleAccess(emptyList(), requestedPath)
          result.shouldBeFalse()
        }

        it("when the path isn't listed as allowed on the consumer") {
          val invalidPath = "/some-other-path/123"
          val result = authoriseConsumerService.doesConsumerHaveRoleAccess(consumerRolesInclude, invalidPath)
          result.shouldBeFalse()
        }
      }
    },
  )
