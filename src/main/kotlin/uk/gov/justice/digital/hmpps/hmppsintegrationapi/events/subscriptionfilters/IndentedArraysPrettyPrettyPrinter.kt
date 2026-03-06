package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter

/**
 * Class to create arrays on new lines to make file changes easier to see
 */
class IndentedArraysPrettyPrettyPrinter : DefaultPrettyPrinter() {
  override fun createInstance(): DefaultPrettyPrinter {
    val prettyPrinter = IndentedArraysPrettyPrettyPrinter()
    prettyPrinter.indentArraysWith(DefaultIndenter())
    return prettyPrinter
  }

  override fun writeObjectFieldValueSeparator(g: JsonGenerator) {
    g.writeRaw(": ")
  }
}
