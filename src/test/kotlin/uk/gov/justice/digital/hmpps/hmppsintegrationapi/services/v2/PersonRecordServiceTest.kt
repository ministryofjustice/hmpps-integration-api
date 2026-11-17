package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import jakarta.validation.ValidationException
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerTeamProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCommunityOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import java.time.LocalDate
import kotlin.test.Test

class PersonRecordServiceTest {
  val corePersonRecordGateway: CorePersonRecordGateway = mock(CorePersonRecordGateway::class.java)
  val personService: GetPersonService = mock(GetPersonService::class.java)
  val communityOffenderManager: GetCommunityOffenderManagerForPersonService = mock(GetCommunityOffenderManagerForPersonService::class.java)
  val service = PersonRecordService(corePersonRecordGateway, personService, communityOffenderManager)
  val requestContext = buildRequestContext()
  val successResponse =
    CorePersonRecord(firstName = "John", lastName = "Smith")

  @Test
  fun `should successfully get a person for a person for a NOMIS`() {
    whenever(corePersonRecordGateway.corePersonRecordFor(GetPersonService.IdentifierType.NOMS, "A1234DC", requestContext)).thenReturn(successResponse)
    val response = service.personRecord("A1234DC", requestContext)
    response shouldBe Response(successResponse)
  }

  @Test
  fun `should successfully get a person for a person for a CRN`() {
    whenever(corePersonRecordGateway.corePersonRecordFor(GetPersonService.IdentifierType.CRN, "A123456", requestContext)).thenReturn(successResponse)
    val response = service.personRecord("A123456", requestContext)
    response shouldBe Response(successResponse)
  }

  @Test
  fun `should throw a not found exception when the gateway returns not found`() {
    whenever(corePersonRecordGateway.corePersonRecordFor(GetPersonService.IdentifierType.CRN, "A123456", requestContext))
      .thenThrow(EntityNotFoundException("Not found"))
    val exception = shouldThrow<EntityNotFoundException> { service.personRecord("A123456", requestContext) }
    exception.message shouldBe "Not found"
  }

  @Test
  fun `should return a bad request when supplied with neither a CRN or NOMIS`() {
    val exception = shouldThrow<ValidationException> { service.personRecord("671230K", requestContext) }
    exception.message.shouldContain("Not a valid HMPPS Id")
  }

  @Test
  fun `should return the supervision status of PRISONS based on identifiers from CPR`() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), true, "ACTIVE IN")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.PRISONS
  }

  @Test
  fun `should return the supervision status of PRISONS based on identifiers from CPR when probation status is false`() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), false, "ACTIVE IN", provider = null)
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.PRISONS
  }

  @Test
  fun `should return the supervision status of PROBATION based on identifiers from CPR`() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), true, "INACTIVE OUT")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.PROBATION
  }

  @Test
  fun `should return the supervision status of PROBATION based on identifiers from CPR when prison status is null`() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), true, null)
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.PROBATION
  }

  @Test
  fun `should return the supervision status of UNKNOWN based on identifiers from CPR - due to multiple crn identifiers`() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456", "A123457"), listOf("A1234DD")), true, "ACTIVE IN")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.UNKNOWN
  }

  @Test
  fun `should return the supervision status of UNKNOWN based on identifiers from CPR - due to multiple prison identifiers`() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD", "A3214AD")), true, "ACTIVE IN")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.UNKNOWN
  }

  @Test
  fun `should return the supervision status of NONE based on identifiers from CPR - due to no identifiers`() {
    setUpSupervisionStatusTest(Identifiers(emptyList(), emptyList()), true, "ACTIVE IN")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.NONE
  }

  @Test
  fun `should return the supervision status of PROBATION on identifiers from CPR `() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), true, "INACTIVE OUT")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.PROBATION
  }

  @Test
  fun `should return the supervision status of PROBATION on identifiers from CPR when no data returned from prisons or probation `() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), true, "INACTIVE OUT")
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.PROBATION
  }

  @Test
  fun `should return the supervision status of NONE when no data returned from prisons or probation `() {
    setUpSupervisionStatusTest(Identifiers(listOf("A123456"), listOf("A1234DD")), true, "INACTIVE OUT", personFoundInCPR = true, personFoundInNomis = false, communityOfficerFound = false, personFoundInDelius = false)
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.NONE
  }

  @Test
  fun `should return the supervision status of NONE when no identifiers returned from CPR `() {
    setUpSupervisionStatusTest(null, true, "INACTIVE OUT", true, personFoundInNomis = false, communityOfficerFound = false, personFoundInDelius = false)
    val supervisionStatus = service.personRecordSupervisionStatus("A123456", requestContext).data!!
    supervisionStatus.supervisionStatus shouldBe SupervisionStatus.NONE
  }

  @Test
  fun `should throw a not found exception when record not found in CPR the supervision status of NONE when no identifiers returned from CPR `() {
    setUpSupervisionStatusTest(null, true, "", personFoundInCPR = false, personFoundInNomis = false, communityOfficerFound = false, personFoundInDelius = false)
    val exception =
      assertThrows<EntityNotFoundException> {
        service.personRecordSupervisionStatus("A123456", requestContext)
      }
    exception.message shouldBe "Not found"
  }

  fun setUpSupervisionStatusTest(
    identifiers: Identifiers? = null,
    probationStatus: Boolean,
    prisonStatus: String? = null,
    personFoundInCPR: Boolean = true,
    personFoundInNomis: Boolean = true,
    communityOfficerFound: Boolean = true,
    personFoundInDelius: Boolean = true,
    provider: PersonResponsibleOfficerTeamProvider? = PersonResponsibleOfficerTeamProvider("PROV1", "Provider 1"),
  ) {
    val successResponse =
      CorePersonRecord(
        firstName = "John",
        lastName = "Smith",
        identifiers = identifiers,
      )

    if (personFoundInCPR) {
      whenever(corePersonRecordGateway.corePersonRecordFor(eq(GetPersonService.IdentifierType.CRN), any(), eq(requestContext)))
        .thenReturn(successResponse)
    } else {
      whenever(corePersonRecordGateway.corePersonRecordFor(eq(GetPersonService.IdentifierType.CRN), any(), eq(requestContext))).thenThrow(EntityNotFoundException("Not found"))
    }

    if (communityOfficerFound) {
      whenever(communityOffenderManager.execute(any(), eq(requestContext.filters)))
        .thenReturn(
          Response(
            CommunityOffenderManager(
              team = PersonResponsibleOfficerTeam(code = "TEAM1", description = "Team 1", provider = provider),
            ),
          ),
        )
    } else {
      whenever(communityOffenderManager.execute(any(), eq(requestContext.filters)))
        .thenReturn(
          Response(null, listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISON_API))),
        )
    }

    if (personFoundInDelius) {
      whenever(personService.getPersonFromDelius(any(), eq(false), eq(requestContext))).thenReturn(
        Response(
          PersonOnProbation(
            person =
              Person(
                firstName = "John",
                lastName = "Smith",
                middleName = "Richard",
                dateOfBirth = LocalDate.of(1980, 1, 1),
              ),
            underActiveSupervision = probationStatus,
          ),
        ),
      )
    } else {
      whenever(personService.getPersonFromDelius(any(), eq(false), eq(requestContext))).thenReturn(
        Response(null, listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NDELIUS))),
      )
    }

    if (personFoundInNomis) {
      whenever(personService.getPersonFromNomis(any(), eq(requestContext))).thenReturn(
        Response(
          POSPrisoner(
            firstName = "John",
            lastName = "Smith",
            dateOfBirth = LocalDate.of(1980, 1, 1),
            youthOffender = false,
            status = prisonStatus,
            prisonId = "MKI",
            lastPrisonId = "BWI",
          ),
        ),
      )
    } else {
      whenever(personService.getPersonFromNomis(any(), eq(requestContext))).thenReturn(
        Response(null, listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISON_API))),
      )
    }
  }
}
