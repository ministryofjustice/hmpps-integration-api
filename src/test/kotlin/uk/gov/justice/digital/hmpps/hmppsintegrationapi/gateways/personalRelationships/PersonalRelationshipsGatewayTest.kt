package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.personalRelationships

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PersonalRelationshipsApiMockServer

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PersonalRelationshipsGateway::class],
)
class PersonalRelationshipsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val personalRelationshipsGateway: PersonalRelationshipsGateway,
) : DescribeSpec({
    val contactId: Long = 123456
    val personalRelationshipsApiMockServer = PersonalRelationshipsApiMockServer()

    beforeEach {
      personalRelationshipsApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("PERSONAL-RELATIONSHIPS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      personalRelationshipsApiMockServer.stop()
    }
    // "authenticates using HMPPS Auth with credentials"
    it("authenticates using HMPPS Auth with credentials") {
      personalRelationshipsGateway.getPrisonerContactId(contactId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS")
    }

    it("gets a list of prisoner contact ids") {
      val path = "/contact/$contactId/linked-prisoners"
      personalRelationshipsApiMockServer.stubPersonalRelationshipsApiResponse(
        path,
        body =
          """
          [
            {
              "prisonerContactId": 123456
            },
            {
              "prisonerContactId": 234567
            }
          ]
          """.trimIndent(),
      )

      val response = personalRelationshipsGateway.getPrisonerContactId(contactId)
      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data.count().shouldBeGreaterThan(1)
    }

    // "Get a list of prisoner contact ids"
    // "Get the restrictions that apply for a particular relationship."
  })
