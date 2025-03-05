package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role

class RoleExtractionFilterTest {
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()
  private var roleExtractionFilter: RoleExtractionFilter = RoleExtractionFilter(roles = RolesConfig(roles = emptyList<Role>()))
}
