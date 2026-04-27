package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores.generalRiskScoreRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val police =
  role("police") {
    permissions {
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/pnd/persons/{hmppsId}/alerts"
      -"/v1/persons/{hmppsId}/alerts"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/{hmppsId}/risks/scores"
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/persons/{hmppsId}/risks/dynamic"
      -"/v1/persons/{hmppsId}/risks/mappadetail"
      -"/v1/persons/{hmppsId}/licences/conditions"
      -"/v1/persons/{hmppsId}/person-responsible-officer"
      -"/v1/hmpps/reference-data"
      -"/v1/status"
    }
    redactionPolicies {
      -laoRedactionPolicy
      -generalRiskScoreRedactions
    }
    filters {
      alertCodes {
        -"HA"
        -"HA2"
        -"XA"
        -"XC"
        -"XCA"
        -"XCI"
        -"XCO"
        -"XCOL"
        -"XCOP"
        -"XCOR"
        -"XEL"
        -"XELH"
        -"XER"
        -"XHT"
        -"XILLENT"
        -"XIS"
        -"XRF"
      }
      supervisionStatuses {
        -"PROBATION"
      }
    }
  }
