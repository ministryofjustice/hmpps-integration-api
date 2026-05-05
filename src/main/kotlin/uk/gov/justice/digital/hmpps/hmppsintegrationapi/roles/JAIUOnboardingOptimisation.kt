package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val jaiuOnboardingOptimisation =
  role("jaiu-onboarding-optimisation") {
    permissions {
      -"/v1/status"
    }
  }
