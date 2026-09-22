package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedLiveRoll
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import java.time.LocalDate

@Service
class GetLiveRollService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(
    prisonId: String,
    term: String?,
    alerts: List<String>?,
    fromDate: LocalDate?,
    toDate: LocalDate?,
    cellLocationPrefix: String?,
    incentiveLevelCode: String?,
    responseFields: List<String>?,
    page: Int,
    size: Int,
    requestContext: RequestContext? = null,
  ): Response<PaginatedLiveRoll?> {
    val response = prisonerOffenderSearchGateway.getPersonsFromPrisonId(prisonId, term, alerts, fromDate, toDate, cellLocationPrefix, incentiveLevelCode, responseFields, page, size, requestContext)

    return Response(data = response.data?.toPaginatedLiveRoll(), errors = response.errors)
  }
}
