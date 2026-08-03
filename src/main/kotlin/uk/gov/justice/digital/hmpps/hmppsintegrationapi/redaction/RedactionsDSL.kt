package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.JsonPathResponseRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.LaoRejectRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.PersonSearchResponseLaoRedaction
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

  fun laoRejection(init: LaoRejectRedactionBuilder.() -> Unit) {
    redactions += LaoRejectRedactionBuilder().apply(init).build()
  }

  fun personSearchLao(init: PersonSearchLaoRedactionBuilder.() -> Unit) {
    redactions += PersonSearchLaoRedactionBuilder().apply(init).build()
  }

  fun build(): List<ResponseRedaction> = redactions
}

class RedactionsBuilder {
  var entries = mutableListOf<Pair<String, RedactionType>>()

  operator fun Pair<String, RedactionType>.unaryMinus() {
    entries += this
  }

  operator fun List<Pair<String, RedactionType>>.unaryMinus() {
    entries += this
  }
}

class EndpointsBuilder {
  val content = mutableListOf<String>()

  operator fun String.unaryMinus() {
    content += this
  }
}

class JsonPathResponseRedactionBuilder {
  private val redactions = mutableListOf<Pair<String, RedactionType>>()
  private var endpoints: MutableList<String>? = null
  private var laoOnly: Boolean = false

  fun laoOnly(laoOnly: Boolean) {
    this.laoOnly = laoOnly
  }

  fun endpoints(init: EndpointsBuilder.() -> Unit) {
    val endpointsBuilder = EndpointsBuilder().apply(init)
    if (endpoints == null) endpoints = mutableListOf()
    endpoints!!.addAll(endpointsBuilder.content)
  }

  fun redactions(init: RedactionsBuilder.() -> Unit) {
    val redactionsBuilder = RedactionsBuilder().apply(init)
    redactions += redactionsBuilder.entries
  }

  fun build(): List<JsonPathResponseRedaction> =
    redactions.map { (path, type) ->
      JsonPathResponseRedaction(
        objectMapper = defaultObjectMapper,
        type = type,
        endpoints = endpoints,
        redactions = listOf(path),
        laoOnly = laoOnly,
      )
    }
}

class LaoRejectRedactionBuilder {
  private var endpoints: MutableList<String>? = null

  fun endpoints(init: EndpointsBuilder.() -> Unit) {
    val endpointsBuilder = EndpointsBuilder().apply(init)
    if (endpoints == null) endpoints = mutableListOf()
    endpoints!!.addAll(endpointsBuilder.content)
  }

  fun build(): LaoRejectRedaction = LaoRejectRedaction(paths = endpoints)
}

class PersonSearchLaoRedactionBuilder {
  private val redactions = mutableListOf<Pair<String, RedactionType>>()

  fun redactions(init: RedactionsBuilder.() -> Unit) {
    val redactionsBuilder = RedactionsBuilder().apply(init)
    redactions += redactionsBuilder.entries
  }

  fun build(): List<PersonSearchResponseLaoRedaction> =
    redactions.map { (path, type) ->
      PersonSearchResponseLaoRedaction(
        objectMapper = defaultObjectMapper,
        type = type,
        redactions = listOf(path),
      )
    }
}
