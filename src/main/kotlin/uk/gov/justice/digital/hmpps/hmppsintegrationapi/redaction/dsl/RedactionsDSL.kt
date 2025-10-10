package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.DelegatingResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.JsonPathResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.ResponseRedaction

val objectMapper = ObjectMapper().registerKotlinModule()

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

  fun jsonPath(init: JsonPathResponseRedactionBuilder.() -> Unit) {
    redactions.add(JsonPathResponseRedactionBuilder().apply(init).build())
  }

  fun <T : Any> delegate(
    redactor: Redactor<T>,
    block: (DelegatingResponseRedactionBuilder<T>.() -> Unit)? = null,
  ) {
    val builder = DelegatingResponseRedactionBuilder(redactor)
    block?.invoke(builder)
    redactions.add(builder.build())
  }

  fun build() = redactions
}

class JsonPathResponseRedactionBuilder {
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

  fun build(): JsonPathResponseRedaction = JsonPathResponseRedaction(objectMapper, requireNotNull(type), paths, includes)
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

class DelegatingResponseRedactionBuilder<T : Any>(
  private val redactor: Redactor<T>,
) {
  var paths: MutableList<String>? = null

  fun paths(init: PathsBuilder.() -> Unit) {
    PathsBuilder().apply(init).content?.let {
      if (paths == null) {
        paths = mutableListOf()
      }
      paths?.addAll(it)
    }
  }

  fun build(): DelegatingResponseRedaction<T> = DelegatingResponseRedaction(redactor, paths)
}
