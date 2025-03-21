package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import kotlin.reflect.KClass

interface Redactor<T : Any> {
  val type: KClass<T>

  fun redact(toRedact: Any): T

  companion object {
    const val REDACTED = "**REDACTED**"
  }
}

object LaoRedactor {
  private val redactors =
    setOf(
      LaoPersonLicencesRedactor,
      LaoStatusInformationRedactor,
      LaoMappaDetailRedactor,
      LaoDynamicRiskRedactor,
      LaoRisksRedactor,
    )

  @Suppress("UNCHECKED_CAST")
  fun of(any: Any): Redactor<*>? = redactors.firstOrNull { it.type == any::class }
}
