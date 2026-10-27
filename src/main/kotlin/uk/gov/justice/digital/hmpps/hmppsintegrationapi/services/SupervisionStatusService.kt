package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsSupervisionStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

@Service
class SupervisionStatusService(
  private val getPersonService: GetPersonService,
  private val communityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
) {
  fun supervisionStatus(
    hmppsId: String,
    requestContext: RequestContext,
  ): Response<HmppsSupervisionStatus?> {
    val status = getPersonService.getSupervisionStatus(hmppsId)
    val probationProvider =
      if (status == SupervisionStatus.PROBATION) {
        val com = communityOffenderManagerForPersonService.execute(hmppsId, requestContext.filters)
        if (com.errors.isNotEmpty()) {
          return Response(data = null, errors = com.errors)
        }
        com.data
          ?.team
          ?.provider
          ?.let { provider -> ProbationProvider(provider.code, provider.description) }
      } else {
        null
      }

    val prisonId =
      if (status == SupervisionStatus.PRISONS) {
        val prisoner = getPersonService.getPrisoner(hmppsId, requestContext)
        if (prisoner.errors.isNotEmpty()) {
          return Response(data = null, errors = prisoner.errors)
        }
        prisoner.data?.prisonId
      } else {
        null
      }

    return Response(data = HmppsSupervisionStatus(status, prisonId, probationProvider))
  }
}
