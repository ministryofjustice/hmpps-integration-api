package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.JsonPathResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType

fun redactionPolicy(
  name: String,
  init: RedactionPolicyBuilder.() -> Unit,
): RedactionPolicy = RedactionPolicyBuilder(name).apply(init).build()

class RedactionPolicyBuilder(
  private val name: String,
) {
  val responseRedactions = mutableListOf<JsonPathResponseRedaction>()

  fun responseRedactions(init: ResponseRedactionsBuilder.() -> Unit) {
    responseRedactions.addAll(ResponseRedactionsBuilder().apply(init).build())
  }

  fun build(): RedactionPolicy = RedactionPolicy(name, responseRedactions)
}

class ResponseRedactionsBuilder {
  private val redactions = mutableListOf<JsonPathResponseRedaction>()

  fun redaction(init: ResponseRedactionBuilder.() -> Unit) {
    redactions.add(ResponseRedactionBuilder().apply(init).build())
  }

  fun build() = redactions
}

class ResponseRedactionBuilder {
  var type: RedactionType? = null
  var paths: MutableList<String>? = null
  var includes: MutableList<String>? = null

  fun paths(init: PathsBuilder.() -> Unit) {
    PathsBuilder().apply(init).content?.let {
      if (paths == null) {
        paths = mutableListOf()
      }
      paths?.addAll(it)
    }
  }

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

  fun build(): JsonPathResponseRedaction = JsonPathResponseRedaction(requireNotNull(type), includes)
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

class PathsBuilder {
  var content: MutableList<String>? = null

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }
}
