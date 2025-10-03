package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class JsonPathResponseRedactionTest :
  DescribeSpec(
    {
      describe("JsonPathResponseRedaction.apply") {

        it("should mask field when paths is null (always applies)") {
          val json = """{"name":"Alice","email":"alice@example.com"}"""
          val doc: DocumentContext = JsonPath.parse(json)

          val redaction =
            JsonPathResponseRedaction(
              type = RedactionType.MASK,
              paths = null, // always applies
              includes = listOf("$.email"),
            )

          redaction.apply("/any/uri", doc)

          doc.read<String>("$.name") shouldBe "Alice"
          doc.read<String>("$.email") shouldBe "*** REDACTED ***"
        }

        it("should remove field when requestUri matches one of the regex paths") {
          val json = """{"id":123,"secret":"top-secret"}"""
          val doc: DocumentContext = JsonPath.parse(json)

          val redaction =
            JsonPathResponseRedaction(
              type = RedactionType.REMOVE,
              paths = listOf("/v1/persons/.*/licences/conditions"),
              includes = listOf("$.secret"),
            )

          redaction.apply("/v1/persons/123/licences/conditions", doc)

          doc.read<Int>("$.id") shouldBe 123
          doc.read<String>("$.secret") shouldBe null
        }

        it("should not apply when requestUri does not match paths") {
          val json = """{"id":123,"secret":"top-secret"}"""
          val doc: DocumentContext = JsonPath.parse(json)

          val redaction =
            JsonPathResponseRedaction(
              type = RedactionType.MASK,
              paths = listOf("/v1/other/endpoint"),
              includes = listOf("$.secret"),
            )

          redaction.apply("/v1/persons/123/licences/conditions", doc)

          doc.read<String>("$.secret") shouldBe "top-secret" // unchanged
        }

        it("should ignore missing JSON paths without throwing") {
          val json = """{"id":123}"""
          val doc: DocumentContext = JsonPath.parse(json)

          val redaction =
            JsonPathResponseRedaction(
              type = RedactionType.MASK,
              includes = listOf("$.nonexistent"),
            )

          // Should not throw
          redaction.apply("/any/uri", doc)

          doc.read<Int>("$.id") shouldBe 123
        }
      }
    },
  )
