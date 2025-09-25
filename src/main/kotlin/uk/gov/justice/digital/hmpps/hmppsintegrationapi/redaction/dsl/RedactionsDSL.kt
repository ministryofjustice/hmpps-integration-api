package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.ResponseRedaction

fun redactionPolicy(
  name: String,
  init: RedactionPolicyBuilder.() -> Unit,
): RedactionPolicy = RedactionPolicyBuilder(name).apply(init).build()

class RedactionPolicyBuilder(
  private val name: String,
) {
  val responseRedactions = mutableListOf<ResponseRedaction>()

  fun responseRedactions(init: ResponseRedactionsBuilder.() -> Unit) {
    responseRedactions.addAll(ResponseRedactionsBuilder().apply(init).build())
  }

  fun build(): RedactionPolicy = RedactionPolicy(name, responseRedactions)
}

class ResponseRedactionsBuilder {
  private val redactions = mutableListOf<ResponseRedaction>()

  fun redaction(init: ResponseRedactionBuilder.() -> Unit) {
    redactions.add(ResponseRedactionBuilder().apply(init).build())
  }

  fun build() = redactions
}

class ResponseRedactionBuilder {
  var type: RedactionType? = null
  var includes: MutableList<String>? = null

  fun includes(init: IncludesBuilder.() -> Unit) {
    IncludesBuilder().apply(init).content?.let {
      if (includes == null) {
        includes = mutableListOf()
      }
      includes?.addAll(it)
    }
  }

  fun type(type: RedactionType) {
    this.type = type
  }

  fun build(): ResponseRedaction = ResponseRedaction(requireNotNull(type), includes)
}

class IncludesBuilder {
  var content: MutableList<String>? = null

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }
}
