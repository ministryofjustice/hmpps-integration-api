package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val prisonerFinance =
  role("prisoner-finance") {
    permissions {
      -"/v1/prison/.*/prisoners/[^/]*/balances$"
      -"/v1/prison/.*/prisoners/.*/accounts/.*/balances"
      -"/v1/prison/.*/prisoners/.*/accounts/.*/transactions"
      -"/v1/prison/.*/prisoners/.*/transactions/[^/]*$"
    }
    filters {
      prisons {}
    }
  }
