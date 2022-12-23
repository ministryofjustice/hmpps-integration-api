package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class CredentialsTest : DescribeSpec({
  describe("#toBase64") {
    it("encodes username and password to Base64 for basic authentication") {
      val credentials = Credentials("username", "password")

      credentials.toBase64().shouldBe("dXNlcm5hbWU6cGFzc3dvcmQ=")
    }

    it("returns null if username is null") {
      val credentials = Credentials(null, "password")

      credentials.toBase64().shouldBeNull()
    }

    it("returns null if password is null") {
      val credentials = Credentials("username", null)

      credentials.toBase64().shouldBeNull()
    }
  }
})
