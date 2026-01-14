package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.allEndpoints
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.cats
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.ctrlo
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.curious
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.constants
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.fullAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.fullAccessLaoRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.hmppsSystem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mappa
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mappaCat4
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mojPrisonerEducation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mojPrisonerFacing
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.police
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.prisonerEscortCustodyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.prisonerFinance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.privatePrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.referenceDataOnly
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.serco
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.smartinbox
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.statusOnly

data class Role(
  val name: String? = null,
  val permissions: List<String>? = null,
  val filters: ConsumerFilters? = null,
  val redactionPolicies: List<RedactionPolicy>? = emptyList<RedactionPolicy>(),
)

val roleConstants =
  constants {
    allEndpoints {
      // Provides access to all endpoints
      -"/.*"
      // Provides access to all events on queue
      -"/v1/activities/attendance-reasons"
      -"/v1/contacts/{contactId}"
      -"/v1/persons/{hmppsId}/addresses"
      -"/v1/persons/{hmppsId}/alerts"
      -"/v1/persons/{hmppsId}/care-needs"
      -"/v1/persons/{hmppsId}/case-notes"
      -"/v1/persons/{hmppsId}/cell-location"
      -"/v1/contacts/"
      -"/v1/persons/{hmppsId}/health-and-diet"
      -"/v1/persons/{hmppsId}/iep-level"
      -"/v1/persons/{hmppsId}/images"
      -"/v1/persons/{hmppsId}/images/{id}"
      -"/v1/images/{id}"
      -"/v1/persons/{hmppsId}/languages"
      -"/v1/persons/{hmppsId}/licences/conditions"
      -"/v1/persons/{hmppsId}/name"
      -"/v1/persons/{hmppsId}/number-of-children"
      -"/v1/persons/{hmppsId}/offences"
      -"/v1/persons/{hmppsId}/person-responsible-officer"
      -"/v1/persons/{hmppsId}/prisoner-base-location"
      -"/v1/persons/{hmppsId}/physical-characteristics"
      -"/v1/persons/{hmppsId}/plp-induction-schedule"
      -"/v1/persons/{hmppsId}/plp-review-schedule"
      -"/v1/persons/{hmppsId}/protected-characteristics"
      -"/v1/persons/{hmppsId}/reported-adjudications"
      -"/v1/persons/{hmppsId}/risks/categories"
      -"/v1/persons/{hmppsId}/risks/dynamic"
      -"/v1/persons/{hmppsId}/risks/mappadetail"
      -"/v1/persons/{hmppsId}/risks/scores"
      -"/v1/persons/{hmppsId}/risks/serious-harm"
      -"/v1/persons/{hmppsId}/sentences"
      -"/v1/persons/{hmppsId}/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/{hmppsId}/status-information"
      -"/v1/persons/{hmppsId}/visit-orders"
      -"/v1/persons/{hmppsId}/visit-restrictions"
      -"/v1/persons/{hmppsId}/visit/future"
      -"/v1/persons/{hmppsId}/visitor/{contactId}/restrictions"
      -"/v1/persons/{hmppsId}"
      -"/v1/pnd/persons/{hmppsId}/alerts"
      -"/v1/prison/{prisonId}/capacity"
      -"/v1/prison/{prisonId}/location/{key}"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/accounts/{accountCode}/balances"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/accounts/{accountCode}/transactions"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/non-associations"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/balances"
      -"/v1/prison/{prisonId}/residential-details"
      -"/v1/prison/{prisonId}/residential-hierarchy"
      -"/v1/prison/{prisonId}/visit/search"
      -"/v1/prison/prisoners"
      -"/v1/prison/prisoners/{hmppsId}"
      -"/v1/visit/{visitReference}"
      -"/v1/visit"
      -"/v1/visit/id/by-client-ref/{clientReference}"
      -"/v1/activities/schedule/{scheduleId}/deallocate"
      -"/v1/prison/{prisonId}/{hmppsId}/scheduled-instances"
      -"/v1/activities/deallocation-reasons"
      -"/v1/activities/schedule/scheduleId/allocate"
      -"/v1/prison/prisoners/{hmppsId}/activities/attendances"
      -"/v1/activities/schedule/scheduleId/waiting-list-applications"
      -"/v1/status"
      -"/v1/persons/{hmppsId}/education/san/plan-creation-schedule"
      -"/v1/persons/{hmppsId}/education/san/review-schedule"
      -"/v1/persons/{hmppsId}/education/status"
      -"/v1/persons/{hmppsId}/education/aln-assessment"
      -"/v1/persons/{hmppsId}/contact-events"
      -"/v1/persons/{hmppsId}/contact-events/{contactEventId}"
      -"/v1/prison/{prisonId}/prison-pay-bands"
      -"/v1/prison/{prisonId}/prison-regime"
      -"/v1/hmpps/id/nomis-number/by-hmpps-id/{hmppsId}"
      -"/v1/hmpps/reference-data"
      -"/v1/persons/{hmppsId}/risk-management-plan"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/accounts/{accountCode}/transactions"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/transactions"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/transactions/{clientUniqueRef}"
      -"/v1/prison/{prisonId}/prisoners/{hmppsId}/transactions/{clientUniqueRef}"
      -"/v1/activities/{activityId}/schedules"
      -"/v1/activities/schedule/{scheduleId}"
      -"/v1/activities/schedule/{scheduleId}/suitability-criteria"
    }
  }

val roles =
  listOf(
    hmppsSystem,
    privatePrison,
    police,
    ctrlo,
    curious,
    referenceDataOnly,
    fullAccess,
    prisonerEscortCustodyService,
    mappa,
    allEndpoints,
    mappaCat4,
    mojPrisonerEducation,
    mojPrisonerFacing,
    serco,
    statusOnly,
    smartinbox,
    fullAccessLaoRedactions,
    prisonerFinance,
    cats,
  ).associateBy { it.name }
