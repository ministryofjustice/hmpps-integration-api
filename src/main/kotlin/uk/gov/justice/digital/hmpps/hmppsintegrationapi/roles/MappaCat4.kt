package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory.CAT4
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val mappaCat4 =
  role("mappa-cat4") {
    include {
      -"/v1/persons"
      -"/v1/persons/[^/]*$"
      -"/v1/persons/.*/images"
      -"/v1/images/.*"
      -"/v1/persons/.*/addresses"
      -"/v1/persons/.*/offences"
      -"/v1/persons/.*/alerts"
      -"/v1/persons/.*/sentences"
      -"/v1/persons/.*/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/.*/risks/scores"
      -"/v1/persons/.*/needs"
      -"/v1/persons/.*/risks/serious-harm"
      -"/v1/persons/.*/reported-adjudications"
      -"/v1/persons/.*/adjudications"
      -"/v1/persons/.*/licences/conditions"
      -"/v1/persons/.*/case-notes"
      -"/v1/persons/.*/protected-characteristics"
      -"/v1/persons/.*/risks/mappadetail"
      -"/v1/persons/.*/risks/categories"
      -"/v1/persons/.*/person-responsible-officer"
      -"/v1/persons/.*/risk-management-plan"
      -"/v1/persons/.*/contact-events"
      -"/v1/persons/.*/contact-events/.*"
      -"/v1/hmpps/reference-data"
      -"/v1/status"
    }
    filters {
      mappaCategories {
        -CAT4
      }
    }
  }
