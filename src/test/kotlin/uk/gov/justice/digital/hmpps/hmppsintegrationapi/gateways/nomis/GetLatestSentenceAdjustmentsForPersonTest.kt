package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetLatestSentenceAdjustmentsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nomisGateway: NomisGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = NomisApiMockServer()
      val offenderNo = "abc123"
      val sentenceSummaryPath = "/api/offenders/$offenderNo/booking/latest/sentence-summary"
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubNomisApiResponse(
          sentenceSummaryPath,
          """
          {
            "prisonerNumber": "A1234AA",
            "latestPrisonTerm": {
              "sentenceAdjustments": {
                "additionalDaysAwarded": 12,
                "unlawfullyAtLarge": 10,
                "lawfullyAtLarge": 2,
                "restoredAdditionalDaysAwarded": 0,
                "specialRemission": 11,
                "recallSentenceRemand": 1,
                "recallSentenceTaggedBail": 3,
                "remand": 6,
                "taggedBail": 3,
                "unusedRemand": 6
              }
            }
          }
        """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns sentence adjustments for a person with the matching ID") {
        val response = nomisGateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        response.data?.additionalDaysAwarded.shouldBe(12)
        response.data?.unlawfullyAtLarge.shouldBe(10)
        response.data?.lawfullyAtLarge.shouldBe(2)
        response.data?.restoredAdditionalDaysAwarded.shouldBe(0)
        response.data?.specialRemission.shouldBe(11)
        response.data?.recallSentenceRemand.shouldBe(1)
        response.data?.recallSentenceTaggedBail.shouldBe(3)
        response.data?.remand.shouldBe(6)
        response.data?.taggedBail.shouldBe(3)
        response.data?.unusedRemand.shouldBe(6)
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubNomisApiResponse(
          sentenceSummaryPath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = nomisGateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
