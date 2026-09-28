package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.casestatus

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CaseStatusValidator::class])
annotation class ValidCaseStatus(
  val message: String = "status is not a recognised value",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class CaseStatusValidator : ConstraintValidator<ValidCaseStatus, String> {
  override fun isValid(
    value: String?,
    context: ConstraintValidatorContext,
  ): Boolean {
    if (value == null) return true

    if (CaseStatus.fromValue(value) != CaseStatus.UNKNOWN) return true

    val received = value.take(MAX_LOGGED_LENGTH)
    log.warn("Case status update rejected: status '{}' did not match a valid case status", received)

    context.disableDefaultConstraintViolation()
    context
      .buildConstraintViolationWithTemplate(
        "status '$received' is not recognised. Must be one of: ${CaseStatus.validValues().joinToString()}",
      ).addConstraintViolation()

    return false
  }

  companion object {
    private const val MAX_LOGGED_LENGTH = 100
    private val log = LoggerFactory.getLogger(CaseStatusValidator::class.java)
  }
}
