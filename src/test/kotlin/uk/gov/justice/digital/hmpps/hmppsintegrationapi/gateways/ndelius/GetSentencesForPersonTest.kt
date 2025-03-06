package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetSentencesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/case/$deliusCrn/supervisions"
      val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)

      beforeEach {
        nDeliusApiMockServer.start()
        nDeliusApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetSupervisionsResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nDeliusApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nDeliusGateway.getSentencesForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns sentences for the matching CRN") {
        val response = nDeliusGateway.getSentencesForPerson(deliusCrn)

        response.data.shouldBe(
          listOf(
            generateTestSentence(
              serviceSource = UpstreamApi.NDELIUS,
              systemSource = SystemSource.PROBATION_SYSTEMS,
              dateOfSentencing = LocalDate.parse("2009-07-07"),
              description = "CJA - Community Order",
              isActive = false,
              isCustodial = false,
              length =
                SentenceLength(
                  duration = 12,
                  units = "Months",
                  terms = emptyList(),
                ),
            ),
            generateTestSentence(
              serviceSource = UpstreamApi.NDELIUS,
              systemSource = SystemSource.PROBATION_SYSTEMS,
              dateOfSentencing = LocalDate.parse("2009-09-01"),
              description = "CJA - Suspended Sentence Order",
              isActive = true,
              isCustodial = false,
              length =
                SentenceLength(
                  duration = 12,
                  units = "Years",
                  terms = emptyList(),
                ),
            ),
          ),
        )
      }

      it("returns an empty list if no sentences are found") {
        nDeliusApiMockServer.stubForGet(
          path,
          """
          {
          "communityManager": {},
           "mappaDetail": {},
           "supervisions": [],
           "dynamicRisks": [],
           "personStatus": []
           }
          """,
        )

        val response = nDeliusGateway.getSentencesForPerson(deliusCrn)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nDeliusApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = nDeliusGateway.getSentencesForPerson(deliusCrn)

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
    },
  )
