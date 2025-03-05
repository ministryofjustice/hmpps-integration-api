package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties

@EnableConfigurationProperties(RolesConfig::class)
class RoleConfigTest(
  @Autowired val rolesConfig: RolesConfig,
) {
  @Test
  fun `get values from roles config yml`() {
    rolesConfig.roles.size.shouldBeGreaterThan(0)
    rolesConfig.roles[0].name.shouldBe("private-prison")
  }
}
