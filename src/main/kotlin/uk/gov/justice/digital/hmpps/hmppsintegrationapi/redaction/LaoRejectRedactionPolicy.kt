package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoRejectRedactionPolicy =
  redactionPolicy(
    "lao-rejection",
    endpoints =
      listOf(
        "/v1/persons/.*/risks/serious-harm",
        "/v1/persons/.*/risk-management-plan",
        "/v1/persons/.*/risks/scores",
      ),
    laoOnly = true,
    reject = true,
  ) {}
