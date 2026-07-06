package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.manageUsers

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import kotlin.test.assertEquals

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ManageUsersGateway::class],
)
class ManageUsersTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val manageUsersGateway: ManageUsersGateway,
) : DescribeSpec(
    {
      val manageUsersMockServer = ApiMockServer.create(UpstreamApi.MANAGE_USERS)
      val path = "/users/search?username=testUser&authSources=azuread"

      beforeEach {
        manageUsersMockServer.start()
        manageUsersMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken(UpstreamApi.MANAGE_USERS.name)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        manageUsersGateway.findUser("testUser", listOf("auth", "azuread"))
        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken(UpstreamApi.MANAGE_USERS.name)
      }

      it("successfully finds a user") {
        val response = manageUsersGateway.findUser("testUser", listOf("azuread"))
        assertEquals(response.data?.content[0]?.username, "testUser")
        assertEquals(response.data?.content[0]?.source, "azuread")
      }

      it("returns a 400 ") {
        manageUsersMockServer.stubForGet(
          path,
          "",
          HttpStatus.BAD_REQUEST,
        )
        val response = manageUsersGateway.findUser("testUser", listOf("azuread"))
        assertEquals(null, response.data)
        assertEquals(UpstreamApiError.Type.BAD_REQUEST, response.errors[0].type)
        assertEquals(UpstreamApi.MANAGE_USERS, response.errors[0].causedBy)
      }
    },
  )
