package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import kotlin.test.assertTrue

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AuthoriseConsumerService::class, FeatureFlagConfig::class],
)
internal class AuthoriseConsumerServiceTest(
  private val authoriseConsumerService: AuthoriseConsumerService,
) : DescribeSpec(
    {
      val requestedPath = "/persons/123"

      describe("doesConsumerHaveIncludesAccess") {
        val consumerConfig = ConsumerConfig(listOf("/persons/.*"), ConsumerFilters(null, null), listOf())

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

      describe("verifyNormalisedPathParameters") {
        it("works with normalised path parameters") {
          val features = FeatureFlagConfig(mapOf(FeatureFlagConfig.NORMALISED_PATH_MATCHING to true))
          AuthoriseConsumerService(features).matches("/v1/persons/123", "/v1/persons/{hmppsId}") shouldBe true
        }
      }

      describe("verifyAllRolesCanAccessEndpointsAndRejectAllOthers") {
        it("test all role endpoints to all mentiond path") {
          val allRoles = roles
          val allAccessRole = allRoles.getValue("all-endpoints")
          for (role in allRoles) {
            for (permisson in role.value.permissions!!) {
              val result = authoriseConsumerService.doesConsumerHaveRoleAccess(emptyList(), permisson)
              result.shouldBeTrue()
            }
          }
          assertTrue(true)
        }
      }
    },
  )
