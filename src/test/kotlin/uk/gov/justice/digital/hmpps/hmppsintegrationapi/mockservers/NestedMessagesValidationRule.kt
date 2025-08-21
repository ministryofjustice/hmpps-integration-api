package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.atlassian.oai.validator.model.ApiOperation
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.report.ValidationReport
import com.atlassian.oai.validator.whitelist.rule.WhitelistRule

/**
 * Extra atlassian rule to check for nested validation messages for whitelisting
 * Atlassian validation only allows for top level messages and does not check nested messages.
 * This is required so we can perform more finegrained whitelisting. Namely checking for the discriminator errors
 * that only appear in nested messages
 *
 */

class NestedMessagesValidationRule(
  val representation: String,
  val function: WhitelistRule,
) : WhitelistRule {
  override fun matches(
    message: ValidationReport.Message?,
    operation: ApiOperation?,
    request: Request?,
    response: Response?,
  ): Boolean = function.matches(message, operation, request, response)

  companion object {
    fun nestedMessageHasKey(key: String): WhitelistRule {
      // Extract nested messages recursively
      fun getAllNestedMessages(
        message: ValidationReport.Message,
        list: MutableList<ValidationReport.Message> = mutableListOf<ValidationReport.Message>(),
      ): List<ValidationReport.Message> {
        message.nestedMessages.map { nestedMessage ->
          if (nestedMessage.nestedMessages.size > 0) {
            list.addAll(nestedMessage.nestedMessages)
            nestedMessage.nestedMessages.forEach { getAllNestedMessages(it, list) }
          }
        }
        return list
      }
      // Check key exists in nested messages
      return NestedMessagesValidationRule(
        "Message with key: '$key'",
      ) { message: ValidationReport.Message, _: ApiOperation?, _: Request?, _: Response? ->
        getAllNestedMessages(message).any { key.equals(it.key, ignoreCase = true) }
      }
    }
  }
}
