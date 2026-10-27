package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus
import kotlin.test.Test

class SupervisionStatusServiceTest {
  lateinit var service: SupervisionStatusService
  val personService: GetPersonService = mock(GetPersonService::class.java)
  val communityOffenderManagerService: GetCommunityOffenderManagerForPersonService = mock(GetCommunityOffenderManagerForPersonService::class.java)
  val requestContext = buildRequestContext()
  val crn = "A123456"
  val com = CommunityOffenderManager(team = PersonResponsibleOfficerTeam("PROV1", "Provider 1"))
  val pos = PersonInPrison(firstName = "test", lastName = "test", prisonId = "MKI", youthOffender = false)

  @BeforeEach
  internal fun setUp() {
    service = SupervisionStatusService(personService, communityOffenderManagerService)
  }

  @Test
  fun `should get UNKNOWN supervision status`() {
    whenever { personService.getSupervisionStatus(crn) }.thenReturn(SupervisionStatus.UNKNOWN)
    val status = service.supervisionStatus(crn, requestContext)
    status.data?.supervisionStatus shouldBe SupervisionStatus.UNKNOWN
    status.data?.prisonId shouldBe null
    status.data?.probationProvider shouldBe null
  }

  @Test
  fun `should get NONE supervision status`() {
    whenever { personService.getSupervisionStatus(crn) }.thenReturn(SupervisionStatus.NONE)
    val status = service.supervisionStatus(crn, requestContext)
    status.data?.supervisionStatus shouldBe SupervisionStatus.NONE
    status.data?.prisonId shouldBe null
    status.data?.probationProvider shouldBe null
  }

  @Test
  fun `should get PROBATION supervision status`() {
    whenever { personService.getSupervisionStatus(crn) }.thenReturn(SupervisionStatus.PROBATION)
    whenever { communityOffenderManagerService.execute(crn, requestContext.filters) }.thenReturn(
      Response(data = com),
    )
    val status = service.supervisionStatus(crn, requestContext)
    status.data?.supervisionStatus shouldBe SupervisionStatus.PROBATION
    status.data?.prisonId shouldBe null
    status.data?.probationProvider shouldBe com.team.provider
  }

  @Test
  fun `should get PRISON supervision status`() {
    whenever { personService.getSupervisionStatus(crn) }.thenReturn(SupervisionStatus.PRISONS)
    whenever { personService.getPrisoner(crn, requestContext) }.thenReturn(
      Response(data = pos),
    )
    val status = service.supervisionStatus(crn, requestContext)
    status.data?.supervisionStatus shouldBe SupervisionStatus.PRISONS
    status.data?.prisonId shouldBe "MKI"
    status.data?.probationProvider shouldBe null
  }

  @Test
  fun `should return a not found error when PROBATION supervision status`() {
    whenever { personService.getSupervisionStatus(crn) }.thenReturn(SupervisionStatus.PROBATION)
    whenever { communityOffenderManagerService.execute(crn, requestContext.filters) }.thenReturn(
      Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NDELIUS, Type.ENTITY_NOT_FOUND))),
    )
    val status = service.supervisionStatus(crn, requestContext)
    status.data shouldBe null
    status.errors shouldContain UpstreamApiError(UpstreamApi.NDELIUS, Type.ENTITY_NOT_FOUND)
  }

  @Test
  fun `should return a not found error when PRISON supervision status`() {
    whenever { personService.getSupervisionStatus(crn) }.thenReturn(SupervisionStatus.PRISONS)
    whenever { personService.getPrisoner(crn, requestContext) }.thenReturn(
      Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, Type.ENTITY_NOT_FOUND))),
    )
    val status = service.supervisionStatus(crn, requestContext)
    status.data shouldBe null
    status.errors shouldContain UpstreamApiError(UpstreamApi.PRISON_API, Type.ENTITY_NOT_FOUND)
  }
}
