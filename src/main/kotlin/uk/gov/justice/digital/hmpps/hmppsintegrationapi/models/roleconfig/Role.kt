package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.allEndpoints
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.curious
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.constants
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.fullAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mappa
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.mappaCat4
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.police
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.prisonerEscortCustodyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.privatePrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.referenceDataOnly

data class Role(
  val name: String? = null,
  val include: List<String>? = null,
  val filters: ConsumerFilters? = null,
)

val roleConstants =
  constants {
    allEndpoints {
      // Provides access to all endpoints
      -"/.*"
      // Provides access to all events on queue
      -"/v1/activities/attendance-reasons"
      -"/v1/contacts/[^/]*$"
      -"/v1/persons/.*/addresses"
      -"/v1/persons/.*/alerts"
      -"/v1/persons/.*/care-needs"
      -"/v1/persons/.*/case-notes"
      -"/v1/persons/.*/cell-location"
      -"/v1/persons/.*/contacts[^/]*$"
      -"/v1/persons/.*/health-and-diet"
      -"/v1/persons/.*/iep-level"
      -"/v1/persons/.*/images"
      -"/v1/persons/.*/images/.*"
      -"/v1/persons/.*/languages"
      -"/v1/persons/.*/licences/conditions"
      -"/v1/persons/.*/name"
      -"/v1/persons/.*/number-of-children"
      -"/v1/persons/.*/offences"
      -"/v1/persons/.*/person-responsible-officer"
      -"/v1/persons/.*/prisoner-base-location"
      -"/v1/persons/.*/physical-characteristics"
      -"/v1/persons/.*/plp-induction-schedule"
      -"/v1/persons/.*/plp-review-schedule"
      -"/v1/persons/.*/protected-characteristics"
      -"/v1/persons/.*/reported-adjudications"
      -"/v1/persons/.*/risks/categories"
      -"/v1/persons/.*/risks/dynamic"
      -"/v1/persons/.*/risks/mappadetail"
      -"/v1/persons/.*/risks/scores"
      -"/v1/persons/.*/risks/serious-harm"
      -"/v1/persons/.*/sentences"
      -"/v1/persons/.*/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/.*/status-information"
      -"/v1/persons/.*/visit-orders"
      -"/v1/persons/.*/visit-restrictions"
      -"/v1/persons/.*/visit/future"
      -"/v1/persons/.*/visitor/.*/restrictions"
      -"/v1/persons/[^/]*$"
      -"/v1/pnd/persons/.*/alerts"
      -"/v1/prison/.*/capacity"
      -"/v1/prison/.*/location/[^/]*$"
      -"/v1/prison/.*/prisoners/.*/accounts/.*/balances"
      -"/v1/prison/.*/prisoners/.*/accounts/.*/transactions"
      -"/v1/prison/.*/prisoners/.*/non-associations"
      -"/v1/prison/.*/prisoners/[^/]*/balances$"
      -"/v1/prison/.*/residential-details"
      -"/v1/prison/.*/residential-hierarchy"
      -"/v1/prison/.*/visit/search[^/]*$"
      -"/v1/prison/prisoners"
      -"/v1/prison/prisoners/[^/]*$"
      -"/v1/visit/[^/]*$"
      -"/v1/visit/id/by-client-ref/[^/]*$"
      -"/v1/activities/schedule/.*/deallocate"
      -"/v1/prison/.*/.*/scheduled-instances"
      -"/v1/activities/deallocation-reasons"
      -"/v1/activities/schedule/.*/allocate"
      -"/v1/prison/prisoners/.*/activities/attendances"
      -"/v1/activities/schedule/.*/waiting-list-applications"
      -"/v1/status"
      -"/v1/persons/.*/education/san/plan-creation-schedule"
      -"/v1/persons/.*/education/san/review-schedule"
      -"/v1/persons/.*/education/status"
      -"/v1/persons/.*/education/aln-assessment"
      -"/v1/persons/.*/contact-events"
      -"/v1/persons/.*/contact-events/.*"
    }
  }

val roles =
  listOf(
    privatePrison,
    police,
    curious,
    referenceDataOnly,
    fullAccess,
    prisonerEscortCustodyService,
    mappa,
    allEndpoints,
    mappaCat4,
  ).associateBy { it.name }
