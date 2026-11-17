package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSupervisionStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.PrisonData
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.ProbationData
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ProbationTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCommunityOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

@Service
class PersonRecordService(
  private val corePersonRecordGateway: CorePersonRecordGateway,
  private val personService: GetPersonService,
  private val communityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
) {
  fun personRecord(
    hmppsId: String,
    requestContext: RequestContext,
  ): Response<CorePersonRecord> {
    val type = identifyHmppsId(hmppsId)
    if (type == IdentifierType.UNKNOWN) {
      throw ValidationException("Not a valid HMPPS Id: $hmppsId")
    }
    val response = corePersonRecordGateway.corePersonRecordFor(type, hmppsId, requestContext)
    return Response(data = response)
  }

  fun personRecordSupervisionStatus(
    hmppsId: String,
    requestContext: RequestContext,
  ): Response<CorePersonRecordSupervisionStatus?> {
    val cprResponse = personRecord(hmppsId, requestContext)
    val prisonData =
      cprResponse.data.identifiers?.let {
        it.prisonNumbers.map { nomisId ->
          val prisoner = personService.getPersonFromNomis(nomisId, requestContext)
          prisoner.data.let { prisoner ->
            PrisonData(
              hmppsId = nomisId,
              prisonId = prisoner?.prisonId,
              lastPrisonId = prisoner?.lastPrisonId,
              prisonerStatus = prisoner?.status,
              lastMovementType = prisoner?.lastMovementTypeCode,
              receptionDate = prisoner?.receptionDate,
              inPrison = prisoner?.status?.startsWith("ACTIVE") == true,
            )
          }
        }
      } ?: emptyList()

    val probationData =
      cprResponse.data.identifiers?.let {
        it.crns.map { crn ->
          val responsibleOfficer = communityOffenderManagerForPersonService.execute(crn, requestContext.filters)
          val probationOffender = personService.getPersonFromDelius(crn, requestContext = requestContext)
          ProbationData(
            hmppsId = crn,
            activeSupervision = probationOffender.data?.underActiveSupervision == true,
            provider = responsibleOfficer.data?.let { officer -> ProbationProvider(officer.team.provider?.code, officer.team.provider?.description) },
            team = responsibleOfficer.data?.let { officer -> ProbationTeam(officer.team.code, officer.team.description) },
          )
        }
      } ?: emptyList()

    val overallSupervisionStatus =
      when {
        (prisonData.size > 1 || probationData.size > 1) -> Pair<SupervisionStatus, String?>(SupervisionStatus.UNKNOWN, null)
        (prisonData.size == 1 && prisonData[0].inPrison) -> Pair<SupervisionStatus, String?>(SupervisionStatus.PRISONS, prisonData[0].hmppsId)
        (probationData.size == 1 && probationData[0].activeSupervision) -> Pair(SupervisionStatus.PROBATION, probationData[0].hmppsId)
        else -> Pair<SupervisionStatus, String?>(SupervisionStatus.NONE, null)
      }

    val supervisionStatus =
      CorePersonRecordSupervisionStatus(
        supervisionStatus = overallSupervisionStatus.first,
        determinedBy = overallSupervisionStatus.second,
        prison = prisonData,
        probation = probationData,
      )
    return Response(data = supervisionStatus)
  }

  fun identifyHmppsId(input: String): IdentifierType {
    val nomsPattern = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    val crnPattern = Regex("^[A-Z]{1,2}\\d{6}$")

    return when {
      nomsPattern.matches(input) -> IdentifierType.NOMS
      crnPattern.matches(input) -> IdentifierType.CRN
      else -> IdentifierType.UNKNOWN
    }
  }
}
