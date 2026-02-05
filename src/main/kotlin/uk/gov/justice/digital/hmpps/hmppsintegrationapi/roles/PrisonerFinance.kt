package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerFinance =
  role("prisoner-finance") {
    permissions {
      -"/v1/status"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/balances"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/accounts/{accountCode}/balances"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/accounts/{accountCode}/transactions"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/transactions"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/transactions/{clientUniqueRef}"
    }
    filters {
      prisons {}
    }
  }
