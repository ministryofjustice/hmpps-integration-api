package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Credentials

class CredentialsTest : DescribeSpec({
  describe("#toBasicAuth") {
    it("returns username and password for basic authentication header") {
      val credentials = Credentials("my-client-id", "my-client-secret")

      credentials.toBasicAuth().shouldBe("Basic bXktY2xpZW50LWlkOm15LWNsaWVudC1zZWNyZXQ=")
    }
  }
})
