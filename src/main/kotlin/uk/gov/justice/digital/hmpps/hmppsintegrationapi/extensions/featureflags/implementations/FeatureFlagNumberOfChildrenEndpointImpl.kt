package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags.implementations

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags.FeatureFlagValidator

@Component
class FeatureFlagNumberOfChildrenEndpointImpl(
  @Autowired val featureFlagConfigs: FeatureFlagConfig,
) : FeatureFlagValidator {
  override val featureFlagName: String
    get() = FeatureFlagConfig.USE_NUMBER_OF_CHILDREN_ENDPOINTS

  override fun validate(vararg args: Any?): Boolean {
    val result: Boolean = featureFlagConfigs?.useNumberOfChildrenEndpoints ?: false
    return result
  }
}
