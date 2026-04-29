package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import com.github.fge.jackson.NodeType
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.format.AbstractFormatAttribute
import com.github.fge.jsonschema.processors.data.FullData
import com.github.fge.msgsimple.bundle.MessageBundle
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * This can be used by the mock server validator to provide a more lenient date-time check
 * There are instances where date-times are not in the RFC3339 format where they do not contain a Z or an offset
 *  e.g
 *  2026-01-26T15:32:45 IS NOT RFC3339 compliant
 *  2026-04-29T10:30:00Z IS RFC3339 compliant
 *  2026-04-29T10:30:00+00:00 IS RFC3339 compliant
 */
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

    val value = data.instance.node.textValue()
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
