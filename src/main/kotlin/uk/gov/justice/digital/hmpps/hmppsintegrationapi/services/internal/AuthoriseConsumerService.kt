package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.normalisePath
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Component
@Service
class AuthoriseConsumerService(
  @Autowired var featureFlagConfig: FeatureFlagConfig,
) {
  fun matches(
    path: String,
    pathTemplate: String,
  ): Boolean =
    Regex(
      if (featureFlagConfig.isEnabled(FeatureFlagConfig.NORMALISED_PATH_MATCHING)) {
        normalisePath(pathTemplate)
      } else {
        pathTemplate
      },
    ).matches(path)

  fun doesConsumerHaveIncludesAccess(
    consumerConfig: ConsumerConfig?,
    requestedPath: String,
  ): Boolean {
    consumerConfig?.permissions()?.forEach {
      if (matches(requestedPath, it)) {
        return true
      }
    }
    return false
  }

  fun doesConsumerHaveRoleAccess(
    consumerRolesInclude: List<String>,
    requestPath: String,
  ): Boolean {
    consumerRolesInclude.forEach {
      if (matches(requestPath, it)) {
        return true
      }
    }
    return false
  }
}
