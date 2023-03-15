package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StringExtensionTest : DescribeSpec({
  describe("#removeWhitespaceAndNewlines") {
    it("removes all whitespace and new lines") {
      """
      {
        "never": "gonna",
        "give": "you",
        "up": null
      }
      """.removeWhitespaceAndNewlines().shouldBe("{\"never\":\"gonna\",\"give\":\"you\",\"up\":null}")
    }

    it("maintains spaces in values") {
      """
      {
        "cat": "meow meow"
      }
      """.removeWhitespaceAndNewlines().shouldBe("{\"cat\":\"meow meow\"}")
    }
  }

  describe("#decodeUrl") {
    it("decodes URL encoded string") {
      "never%21gonna%26give%2Fyou%23up".decodeUrl().shouldBe("never!gonna&give/you#up")
    }
  }
},)
