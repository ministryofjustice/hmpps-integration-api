package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class GlobalsConfigTest {
  @Autowired lateinit var globalsConfig: GlobalsConfig

  @Test
  fun `globals should contain roles`() {
    globalsConfig.roles.size.shouldBeGreaterThan(0)
    globalsConfig.roles[0].name.shouldBe("private-prison")
  }
}
