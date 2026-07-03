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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.PaginatedUsers
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
}
