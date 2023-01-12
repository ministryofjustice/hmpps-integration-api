package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CredentialsTest : DescribeSpec({
  describe("#toBasicAuth") {
    it("returns username and password for basic authentication header") {
      val credentials = Credentials("dXNlcm5hbWUK", "cGFzc3dvcmQK")

      credentials.toBasicAuth().shouldBe("Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
    }
  }
})
