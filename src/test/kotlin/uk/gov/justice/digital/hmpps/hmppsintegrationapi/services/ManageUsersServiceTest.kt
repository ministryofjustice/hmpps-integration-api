package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthUser
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.NomisRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.PaginatedUsers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ManageUsersService.Companion.NOMIS_GLOBAL_SEARCH_ROLE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManageUsersServiceTest {
  lateinit var service: ManageUsersService
  val manageUsersGateway: ManageUsersGateway = mock()

  @BeforeEach
  fun setup() {
    service = ManageUsersService(manageUsersGateway)
  }

  @Test
  fun `username is found in one of the required sources`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(PaginatedUsers(listOf(HmppsAuthUser("testName1", "delius")))))
    assertTrue(service.usernameExists("testName1", listOf("delius")))
  }

  @Test
  fun `username is NOT found in one of the required sources`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(PaginatedUsers(emptyList())))
    assertFalse(service.usernameExists("testName2", listOf("delius")))
  }

  @Test
  fun `username is found but is not enabled`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(PaginatedUsers(listOf(HmppsAuthUser("testName2", "delius", enabled = false)))))
    assertFalse(service.usernameExists("testName2", listOf("delius")))
  }

  @Test
  fun `username is found but and is locked`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(PaginatedUsers(listOf(HmppsAuthUser("testName2", "delius", locked = true)))))
    assertFalse(service.usernameExists("testName2", listOf("delius")))
  }

  @Test
  fun `no data is returned from manage users gateway`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(data = null))
    assertFalse(service.usernameExists("testName2", listOf("delius")))
  }

  @Test
  fun `Gateway returns an error`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.MANAGE_USERS))))
    val error =
      assertThrows<RuntimeException> {
        (service.usernameExists("testName2", listOf("delius")))
      }
    assertEquals("Call to MANAGE_USERS failed with error: BAD_REQUEST", error.message)
  }

  @Test
  fun `hasApplicableRole throws exception when find nomis users returns an error`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.MANAGE_USERS))))
    val error =
      assertThrows<RuntimeException> {
        (service.hasApplicableRole("testName3"))
      }
    assertEquals("Call to MANAGE_USERS findUser failed with error: BAD_REQUEST", error.message)
  }

  @Test
  fun `hasApplicableRole throws exception when get roles returns an error`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(PaginatedUsers(listOf(HmppsAuthUser("testName1", "nomis")))))
    whenever(manageUsersGateway.getRoles(any())).thenReturn(Response(null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.MANAGE_USERS))))
    val error =
      assertThrows<RuntimeException> {
        (service.hasApplicableRole("testName3"))
      }
    assertEquals("Call to MANAGE_USERS getRoles failed with error: BAD_REQUEST", error.message)
  }

  @Test
  fun `hasApplicableRole returns true when the user has multiple nomis user records and the second has the correct role`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(
      Response(
        PaginatedUsers(
          listOf(
            HmppsAuthUser("testName1", "nomis"),
            HmppsAuthUser("testName2", "nomis"),
          ),
        ),
      ),
    )
    whenever(manageUsersGateway.getRoles("testName1")).thenReturn(
      Response(listOf(NomisRole("TEST_ROLE_1"), NomisRole("TEST_ROLE_2")), errors = emptyList()),
    )
    whenever(manageUsersGateway.getRoles("testName2")).thenReturn(
      Response(listOf(NomisRole("TEST_ROLE_3"), NomisRole(NOMIS_GLOBAL_SEARCH_ROLE)), errors = emptyList()),
    )
    assertTrue(service.hasApplicableRole("testName"))
  }

  @Test
  fun `hasApplicableRole returns false when the user has multiple nomis user records and none have the correct role`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(
      Response(
        PaginatedUsers(
          listOf(
            HmppsAuthUser("testName1", "nomis"),
            HmppsAuthUser("testName2", "nomis"),
          ),
        ),
      ),
    )
    whenever(manageUsersGateway.getRoles("testName1")).thenReturn(
      Response(listOf(NomisRole("TEST_ROLE_1"), NomisRole("TEST_ROLE_2")), errors = emptyList()),
    )
    whenever(manageUsersGateway.getRoles("testName2")).thenReturn(
      Response(listOf(NomisRole("TEST_ROLE_3"), NomisRole("TEST_ROLE_4")), errors = emptyList()),
    )
    assertFalse(service.hasApplicableRole("testName"))
  }

  @Test
  fun `hasApplicableRole returns false when the user has no nomis user records`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(
      Response(PaginatedUsers(emptyList())),
    )
    assertFalse(service.hasApplicableRole("testName"))
  }

  @Test
  fun `hasApplicableRole returns false when the user has a nomis user record but it is locked`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(
      Response(PaginatedUsers(listOf(HmppsAuthUser("testName2", "nomis", locked = true))), errors = emptyList()),
    )
    assertFalse(service.hasApplicableRole("testName"))
  }
}
