package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores.generalRiskScoreRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mappa =
  role("mappa") {
    permissions {
      -"/v1/persons"
      -"/v1/persons/{hmppsId}"
      -"/v1/persons/{hmppsId}/images"
      -"/v1/images/{id}"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/alerts"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/{hmppsId}/risks/scores"
      -"/v1/persons/{hmppsId}/needs"
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/persons/{hmppsId}/reported-adjudications"
      -"/v1/persons/{hmppsId}/adjudications"
      -"/v1/persons/{hmppsId}/licences/conditions"
      -"/v1/persons/{hmppsId}/case-notes"
      -"/v1/persons/{hmppsId}/protected-characteristics"
      -"/v1/persons/{hmppsId}/risks/mappadetail"
      -"/v1/persons/{hmppsId}/risks/categories"
      -"/v1/persons/{hmppsId}/person-responsible-officer"
      -"/v1/persons/{hmppsId}/risk-management-plan"
      -"/v1/persons/{hmppsId}/contact-events"
      -"/v1/persons/{hmppsId}/contact-events/{contactEventId}"
      -"/v1/hmpps/reference-data"
      -"/v1/status"
    }
    filters {
      mappaCategories {
        -"*"
      }
    }
    redactionPolicies(
      listOf(
        generalRiskScoreRedactions,
      ),
    )
  }
