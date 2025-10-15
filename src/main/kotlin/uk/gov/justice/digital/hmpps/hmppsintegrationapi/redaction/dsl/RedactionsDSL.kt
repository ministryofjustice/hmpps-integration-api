package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.DelegatingResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.JsonPathResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.ResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.JsonPathResponseRedactionBuilder.DelegatingResponseRedactionBuilder

val objectMapper: ObjectMapper =
  ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .configure(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun redactionPolicy(
  name: String,
  init: RedactionPolicyBuilder.() -> Unit,
): RedactionPolicy = RedactionPolicyBuilder(name).apply(init).build()

class RedactionPolicyBuilder(
  private val name: String,
) {
  private val responseRedactions = mutableListOf<ResponseRedaction>()

  fun responseRedactions(init: ResponseRedactionsBuilder.() -> Unit) {
    responseRedactions += ResponseRedactionsBuilder().apply(init).build()
  }

  fun build(): RedactionPolicy = RedactionPolicy(name, responseRedactions)
}

class ResponseRedactionsBuilder {
  private val redactions = mutableListOf<ResponseRedaction>()

  fun jsonPath(init: JsonPathResponseRedactionBuilder.() -> Unit) {
    redactions += JsonPathResponseRedactionBuilder().apply(init).build()
  }

  fun <T : Any> delegate(
    redactor: Redactor<T>,
    block: (DelegatingResponseRedactionBuilder<T>.() -> Unit)? = null,
  ) {
    val builder = DelegatingResponseRedactionBuilder(redactor)
    block?.invoke(builder)
    redactions += builder.build()
  }

  fun build(): List<ResponseRedaction> = redactions
}

class IncludesBuilder {
  val entries = mutableListOf<Pair<String, RedactionType>>()

  fun path(
    path: String,
    type: RedactionType,
  ) {
    entries += path to type
  }
}

class PathsBuilder {
  val content = mutableListOf<String>()

  operator fun String.unaryMinus() {
    content += this
  }
}

class JsonPathResponseRedactionBuilder {
  private val includeEntries = mutableListOf<Pair<String, RedactionType>>()
  private var paths: MutableList<String>? = null

  fun paths(init: PathsBuilder.() -> Unit) {
    val pathsBuilder = PathsBuilder().apply(init)
    if (paths == null) paths = mutableListOf()
    paths!!.addAll(pathsBuilder.content)
  }

  fun includes(init: IncludesBuilder.() -> Unit) {
    val includesBuilder = IncludesBuilder().apply(init)
    includeEntries += includesBuilder.entries
  }

  fun build(): List<JsonPathResponseRedaction> =
    includeEntries.map { (path, type) ->
      JsonPathResponseRedaction(
        objectMapper = objectMapper,
        type = type,
        paths = paths,
        includes = listOf(path),
      )
    }

  class DelegatingResponseRedactionBuilder<T : Any>(
    private val redactor: Redactor<T>,
  ) {
    private var paths: MutableList<String>? = null

    fun paths(init: PathsBuilder.() -> Unit) {
      val b = PathsBuilder().apply(init)
      if (paths == null) paths = mutableListOf()
      paths!!.addAll(b.content)
    }

    fun build(): DelegatingResponseRedaction<T> = DelegatingResponseRedaction(redactor, paths)
  }
}
