package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LastMovementType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class GetPrisonerBaseLocationForPersonService(
  @Autowired private val getPersonService: GetPersonService,
  @Autowired private val prisonerBaseLocationGateway: PrisonerBaseLocationGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PrisonerBaseLocation?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    return prisonerBaseLocationGateway.getPrisonerBaseLocation(nomisNumber)
  }
}

/**
 * <p><This unreal gateway is pending for refactoring /p>
 *
 * - Actual Gateway shall be created in next enhancement, which pull out logic to an API of another Domain Service.
 * - All business logic shall be implemented inside (to be pulled out later), as the tactical solution
 */
@Service
class PrisonerBaseLocationGateway(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisonerBaseLocation(nomisNumber: String): Response<PrisonerBaseLocation?> {
    logger.debug("Getting prisoner base location for prisonNumber=$nomisNumber")
    val prisonResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = nomisNumber)

    // to be refactored: translate from reference data, at domain service
    val baseLocation =
      prisonResponse.data?.let {
        PrisonerBaseLocation(
          inPrison = it.inOutStatus == "IN",
          prisonId = it.prisonId,
          lastPrisonId = it.lastPrisonId,
          lastMovementType = it.lastMovementTypeCode?.let { translateLastMovementType(it) },
          receptionDate = it.receptionDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) },
        )
      }
    return Response(data = baseLocation, errors = prisonResponse.errors)
  }

  private fun translateLastMovementType(lastMovementTypeCode: String) =
    when (lastMovementTypeCode) {
      "ADM" -> LastMovementType.ADMISSION
      "REL" -> LastMovementType.RELEASE
      "TRN" -> LastMovementType.TRANSFERS
      "CRT" -> LastMovementType.COURT
      "TAP" -> LastMovementType.TEMPORARY_ABSENCE
      else -> null
    }
}
