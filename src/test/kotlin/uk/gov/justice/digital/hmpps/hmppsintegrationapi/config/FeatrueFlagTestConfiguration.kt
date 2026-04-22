@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class FeatureFlagTestConfig {
  @Bean
  @Primary
  fun featureFlagConfig(): FeatureFlagConfig =
    FeatureFlagConfig(
      flags =
        mapOf(
          FeatureFlagConfig.COURSE_COMPLETION_EVENT to true,
          FeatureFlagConfig.USE_ACTIVE_ALERTS_ENDPOINT to true,
          FeatureFlagConfig.USE_CONTACT_EVENTS_ENDPOINT to true,
          FeatureFlagConfig.USE_EMERGENCY_CONTACTS_ENDPOINT to true,
          FeatureFlagConfig.EPF_ENDPOINT_INCLUDES_LAO to false,
          FeatureFlagConfig.EPF_GATEWAY_DISABLED to false,
          FeatureFlagConfig.CPR_ENABLED to true,
          FeatureFlagConfig.GATEWAY_CACHE_ENABLED to true,
          FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH to false,
          // Events feature flags
          FeatureFlagConfig.ENABLE_DELETE_PROCESSED_EVENTS to true,
          FeatureFlagConfig.ENABLE_PUBLISH_PENDING_EVENTS to true,
          FeatureFlagConfig.ENABLE_DOMAIN_EVENTS_QUEUE_LISTENER to true,
          FeatureFlagConfig.ENABLE_SUBSCRIPTION_FILTER_POLICY_UPDATER to true,
          FeatureFlagConfig.PERSON_LANGUAGES_CHANGED_NOTIFICATIONS_ENABLED to true,
          FeatureFlagConfig.PRISONER_BASE_LOCATION_CHANGED_NOTIFICATIONS_ENABLED to true,
          FeatureFlagConfig.PRISONER_MERGED_NOTIFICATIONS_ENABLED to true,
          FeatureFlagConfig.CONTACT_EVENTS_NOTIFICATIONS_ENABLED to true,
          FeatureFlagConfig.LIMITED_ACCESS_NOTIFICATIONS_ENABLED to true,
          FeatureFlagConfig.DEDUPLICATE_EVENTS to true,
          FeatureFlagConfig.DIRECT_SQS_NOTIFICATIONS to false,
          FeatureFlagConfig.INCLUDE_SUPERVISION_STATUS_ATTRIBUTE to true,
        ),
    )
}
