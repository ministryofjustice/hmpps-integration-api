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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.PaginatedUsers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManageUsersServiceTest {
  lateinit var service: ManageUsersService
  val manageUsersGateway = mock<ManageUsersGateway>()

  @BeforeEach
  fun setup() {
    val users = listOf(User("testName1", "azuread"), User("testName2", "delius"), User("testName3", "azuread"))
    val paginatedUsers = PaginatedUsers(users)
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(paginatedUsers))
    service = ManageUsersService(manageUsersGateway)
  }

  @Test
  fun `username is found in one of the required sources`() {
    assertTrue(service.usernameExists("testName2", listOf("delius")))
  }

  @Test
  fun `username is found, but is not in one of the required sources`() {
    assertFalse(service.usernameExists("testName2", listOf("someOtherSource")))
  }

  @Test
  fun `username is NOT found in one of the required sources`() {
    assertFalse(service.usernameExists("testName5", listOf("delius")))
  }

  @Test
  fun `Gateway returns empty content`() {
    whenever(manageUsersGateway.findUser(any(), any())).thenReturn(Response(PaginatedUsers(emptyList())))
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
