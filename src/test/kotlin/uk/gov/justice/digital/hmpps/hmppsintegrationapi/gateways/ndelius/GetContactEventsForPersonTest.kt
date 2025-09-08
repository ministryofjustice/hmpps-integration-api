package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator.generateNDeliusContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator.generateNDeliusContactEvents

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetContactEventsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/case/$deliusCrn/contacts"
      val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)

      beforeEach {
        nDeliusApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nDeliusApiMockServer.stop()
      }
      it("returns contact events from delius for the matching CRN") {
        nDeliusApiMockServer.stubForGet(
          "$path?page=1&size=10",
          MockMvcExtensions.writeAsJson(generateNDeliusContactEvents(crn = deliusCrn, pageSize = 10, pageNumber = 1, totalRecords = 100)),
        )
        val response = nDeliusGateway.getContactEventsForPerson(deliusCrn, 1, 10)
        response.data?.size shouldBe 10
      }

      it("returns an error when 404 Not Found is returned from delius") {
        nDeliusApiMockServer.stubForGet("$path?page=1&size=10", "", HttpStatus.NOT_FOUND)

        val response = nDeliusGateway.getContactEventsForPerson(deliusCrn, 1, 10)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NDELIUS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns a contact event from delius for the matching CRN and contact event id") {
        nDeliusApiMockServer.stubForGet(
          "$path/2",
          MockMvcExtensions.writeAsJson(generateNDeliusContactEvent(crn = deliusCrn, id = 2L)),
        )
        val response = nDeliusGateway.getContactEventForPerson(deliusCrn, 2L)
        response.data?.contactEventIdentifier.shouldBe(2)
      }

      it("returns an error when 404 Not Found is returned from delius for contact event id") {
        nDeliusApiMockServer.stubForGet("$path/2", "", HttpStatus.NOT_FOUND)
        val response = nDeliusGateway.getContactEventForPerson(deliusCrn, 2L)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NDELIUS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns stubbed contact events") {
        val response = nDeliusGateway.getStubbedContactEventsForPerson(deliusCrn, 1, 10)
        response.data?.size.shouldBe(10)
      }

      it("returns a stubbed contact event") {
        val response = nDeliusGateway.getStubbedContactEventForPerson(deliusCrn, 1)
        response.data?.contactEventIdentifier.shouldBe(1)
      }
    },
  )
