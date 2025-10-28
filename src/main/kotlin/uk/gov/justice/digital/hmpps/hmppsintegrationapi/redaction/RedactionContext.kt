package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess

class RedactionContext(
  val requestUri: String,
  val hasAccess: GetCaseAccess,
  val hmppsId: String? = null,
)
