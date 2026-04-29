package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import com.github.fge.jackson.NodeType
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.format.AbstractFormatAttribute
import com.github.fge.jsonschema.processors.data.FullData
import com.github.fge.msgsimple.bundle.MessageBundle
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class LenientDateTimeAttribute : AbstractFormatAttribute("date-time", NodeType.STRING) {
  private val formats =
    listOf(
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyy-MM-dd'T'HH:mm:ssZ",
      "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z",
    )

  override fun validate(
    report: ProcessingReport,
    bundle: MessageBundle,
    data: FullData,
  ) {
    val formatter =
      DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .optionalStart()
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .optionalEnd()
        .optionalStart()
        .appendPattern("XXX")
        .optionalEnd()
        .optionalStart()
        .appendPattern("Z")
        .optionalEnd()
        .toFormatter()

    val value = data.instance.getNode().textValue()
    try {
      formatter.parse(value)
    } catch (_: IllegalArgumentException) {
      report.error(
        newMsg(data, bundle, "err.format.invalidDate")
          .putArgument("value", value)
          .putArgument("expected", formats),
      )
    }
  }
}
