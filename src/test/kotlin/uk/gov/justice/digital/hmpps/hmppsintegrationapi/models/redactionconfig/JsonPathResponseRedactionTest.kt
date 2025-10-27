package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper

private val mapper = ObjectMapper().registerKotlinModule()

class JsonPathResponseRedactionTest :
  DescribeSpec(
    {
      describe("JsonPathResponseRedaction.apply") {

        val response = DataResponse<Offender>(Offender("fName", "sName", listOf("mName")))

        val config = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS)

        it("should mask field when paths is null (always applies)") {
          val redaction =
            JsonPathResponseRedaction(
              objectMapper = mapper,
              type = RedactionType.MASK,
              paths = null, // always applies
              includes = listOf("$.data.middleNames"),
            )

          val result = redaction.apply("/any/uri", response)
          val doc = JsonPath.using(config).parse(objectMapper.writeValueAsString(result))

          doc.read<String>("$.data.firstName") shouldBe "fName"
          doc.read<String>("$.data.middleNames") shouldBe "**REDACTED**"
        }

        it("should remove field when requestUri matches one of the regex paths") {
          val redaction =
            JsonPathResponseRedaction(
              objectMapper = mapper,
              type = RedactionType.REMOVE,
              paths = listOf("/v1/persons/.*/licences/conditions"),
              includes = listOf("$.data.middleNames"),
            )

          val result = redaction.apply("/v1/persons/123/licences/conditions", response)
          val doc = JsonPath.using(config).parse(objectMapper.writeValueAsString(result))

          doc.read<String>("$.data.firstName") shouldBe "fName"
          doc.read<String>("$.data.middleNames") shouldBe null
        }

        it("should not apply when requestUri does not match paths") {
          val redaction =
            JsonPathResponseRedaction(
              objectMapper = mapper,
              type = RedactionType.MASK,
              paths = listOf("/v1/other/endpoint"),
              includes = listOf("$.data.middleNames"),
            )

          val result = redaction.apply("/v1/persons/123/licences/conditions", response)
          val doc = JsonPath.using(config).parse(objectMapper.writeValueAsString(result))

          doc.read<String>("$.data.middleNames") shouldBe listOf("mName") // unchanged
        }

        it("should ignore missing JSON paths without throwing") {

          val redaction =
            JsonPathResponseRedaction(
              objectMapper = mapper,
              type = RedactionType.MASK,
              includes = listOf("$.nonexistent"),
            )

          // Should not throw
          val result = redaction.apply("/any/uri", response)
          val doc = JsonPath.using(config).parse(objectMapper.writeValueAsString(result))

          doc.read<Int>("$.data.firstName") shouldBe "fName"
        }

        it("should handle replace-all-in-path correctly") {
          val redactor = JsonPathResponseRedaction(mapper, RedactionType.MASK)

          val doc =
            redactor.parse(
              """
              {"data":{"a":"A","b":{"c":"C"}}}
              """.trimIndent(),
            )

          redactor.redactValues(".c", doc)

          doc.jsonString() shouldBe
            """
            {"data":{"a":"A","b":{"c":"$REDACTION_MASKING_TEXT"}}}
            """.trimIndent()
        }

        it("replaces multiple occurrences of a value in a path") {
          val redactor = JsonPathResponseRedaction(mapper, RedactionType.MASK)

          val doc =
            redactor.parse(
              """
              {"data":{"a":"A","b":{"c":null}, "c": "C"}}
              """.trimIndent(),
            )

          redactor.redactValues(".c", doc)

          doc.jsonString() shouldBe
            """
            {"data":{"a":"A","b":{"c":"$REDACTION_MASKING_TEXT"},"c":"$REDACTION_MASKING_TEXT"}}
            """.trimIndent()
        }

        it("handles no matching paths correctly") {
          val redactor = JsonPathResponseRedaction(mapper, RedactionType.MASK)

          val doc =
            redactor.parse(
              """
              {"data":{"a":"A","b":{"c":null},"c":"C"}}
              """.trimIndent(),
            )

          redactor.redactValues(".q", doc)

          doc.jsonString() shouldBe
            """
            {"data":{"a":"A","b":{"c":null},"c":"C"}}
            """.trimIndent()
        }

        it("correctly removes multiple paths") {
          val redactor = JsonPathResponseRedaction(mapper, RedactionType.REMOVE)

          val doc =
            redactor.parse(
              """
              {"data":{"a":"A","b":{"c":null},"c":"C"}}
              """.trimIndent(),
            )

          redactor.redactValues(".c", doc)

          doc.jsonString() shouldBe
            """
            {"data":{"a":"A","b":{}}}
            """.trimIndent()
        }
      }
    },
  )
