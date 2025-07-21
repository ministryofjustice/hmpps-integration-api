package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * ApplicationContextProvider.
 * Required to access Beans from POJOs (specifically WebClientWrapper)
 */

@Component
class ApplicationContextProvider : ApplicationContextAware {
  @Throws(BeansException::class)
  override fun setApplicationContext(applicationContext: ApplicationContext) {
    featureFlagConfig = applicationContext.getBean(FeatureFlagConfig::class.java)
  }

  companion object {
    var featureFlagConfig: FeatureFlagConfig? = null
      private set
  }
}
