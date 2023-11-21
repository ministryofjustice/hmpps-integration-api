package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetLatestSentenceKeyDatesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nomisGateway: NomisGateway,
) : DescribeSpec(
  {
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetLatestSentenceKeyDatesForPerson(
        offenderNo,
        """
          {
            "sentenceDetail": {
              "automaticReleaseDate": "2023-03-01",
              "automaticReleaseOverrideDate": "2023-03-01",
              "conditionalReleaseDate": "2023-04-01",
              "conditionalReleaseOverrideDate": "2023-04-01",
              "dtoPostRecallReleaseDate": "2023-05-01",
              "dtoPostRecallReleaseDateOverride": "2023-05-01",
              "earlyTermDate": "2021-04-01",
              "etdOverrideDate": "2021-04-01",
              "etdCalculatedDate": "2021-04-01",
              "homeDetentionCurfewActualDate": "2022-05-01",
              "homeDetentionCurfewEligibilityDate": "2022-05-01",
              "homeDetentionCurfewEligibilityCalculatedDate": "2022-05-01",
              "homeDetentionCurfewEligibilityOverrideDate": "2022-05-01",
              "homeDetentionCurfewEndDate": "2022-05-01",
              "lateTermDate": "2022-02-01",
              "ltdOverrideDate": "2022-02-01",
              "ltdCalculatedDate": "2022-02-01",
              "licenceExpiryDate": "2025-02-01",
              "licenceExpiryCalculatedDate": "2025-02-01",
              "licenceExpiryOverrideDate": "2025-02-01",
              "midTermDate": "2023-02-01",
              "mtdCalculatedDate": "2023-02-01",
              "mtdOverrideDate": "2023-02-01",
              "nonDtoReleaseDate": "2023-02-01",
              "nonDtoReleaseDateType": "CRD",
              "nonParoleDate": "2026-02-01",
              "nonParoleOverrideDate": "2026-02-01",
              "paroleEligibilityDate": "2027-02-01",
              "paroleEligibilityCalculatedDate": "2027-02-01",
              "paroleEligibilityOverrideDate": "2027-02-01",
              "postRecallReleaseDate": "2028-02-01",
              "postRecallReleaseOverrideDate": "2028-02-01",
              "releaseDate": "2030-02-01",
              "confirmedReleaseDate": "2030-02-01"
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
      nomisGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns latest sentence key dates for a person with the matching ID") {
      val response = nomisGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

      response.data?.automaticRelease?.date.shouldBe(LocalDate.parse("2023-03-01"))
      response.data?.automaticRelease?.overrideDate.shouldBe(LocalDate.parse("2023-03-01"))
      response.data?.automaticRelease?.calculatedDate.shouldBe(null)

      response.data?.conditionalRelease?.date.shouldBe(LocalDate.parse("2023-04-01"))
      response.data?.conditionalRelease?.overrideDate.shouldBe(LocalDate.parse("2023-04-01"))
      response.data?.conditionalRelease?.calculatedDate.shouldBe(null)

      response.data?.dtoPostRecallRelease?.date.shouldBe(LocalDate.parse("2023-05-01"))
      response.data?.dtoPostRecallRelease?.overrideDate.shouldBe(LocalDate.parse("2023-05-01"))
      response.data?.dtoPostRecallRelease?.calculatedDate.shouldBe(null)

      response.data?.earlyTerm?.date.shouldBe(LocalDate.parse("2021-04-01"))
      response.data?.earlyTerm?.overrideDate.shouldBe(LocalDate.parse("2021-04-01"))
      response.data?.earlyTerm?.calculatedDate.shouldBe(LocalDate.parse("2021-04-01"))

      response.data?.homeDetentionCurfew?.actualDate.shouldBe(LocalDate.parse("2022-05-01"))
      response.data?.homeDetentionCurfew?.eligibilityCalculatedDate.shouldBe(LocalDate.parse("2022-05-01"))
      response.data?.homeDetentionCurfew?.eligibilityDate.shouldBe(LocalDate.parse("2022-05-01"))
      response.data?.homeDetentionCurfew?.eligibilityOverrideDate.shouldBe(LocalDate.parse("2022-05-01"))
      response.data?.homeDetentionCurfew?.endDate.shouldBe(LocalDate.parse("2022-05-01"))

      response.data?.lateTerm?.date.shouldBe(LocalDate.parse("2022-02-01"))
      response.data?.lateTerm?.overrideDate.shouldBe(LocalDate.parse("2022-02-01"))
      response.data?.lateTerm?.calculatedDate.shouldBe(LocalDate.parse("2022-02-01"))

      response.data?.licenceExpiry?.date.shouldBe(LocalDate.parse("2025-02-01"))
      response.data?.licenceExpiry?.overrideDate.shouldBe(LocalDate.parse("2025-02-01"))
      response.data?.licenceExpiry?.calculatedDate.shouldBe(LocalDate.parse("2025-02-01"))

      response.data?.midTerm?.date.shouldBe(LocalDate.parse("2023-02-01"))
      response.data?.midTerm?.overrideDate.shouldBe(LocalDate.parse("2023-02-01"))
      response.data?.midTerm?.calculatedDate.shouldBe(LocalDate.parse("2023-02-01"))

      response.data?.nonDto?.date.shouldBe(LocalDate.parse("2023-02-01"))
      response.data?.nonDto?.releaseDateType.shouldBe("CRD")

      response.data?.nonParole?.date.shouldBe(LocalDate.parse("2026-02-01"))
      response.data?.nonParole?.overrideDate.shouldBe(LocalDate.parse("2026-02-01"))
      response.data?.nonParole?.calculatedDate.shouldBe(null)

      response.data?.paroleEligibility?.date.shouldBe(LocalDate.parse("2027-02-01"))
      response.data?.paroleEligibility?.overrideDate.shouldBe(LocalDate.parse("2027-02-01"))
      response.data?.paroleEligibility?.calculatedDate.shouldBe(LocalDate.parse("2027-02-01"))

      response.data?.postRecallRelease?.date.shouldBe(LocalDate.parse("2028-02-01"))
      response.data?.postRecallRelease?.overrideDate.shouldBe(LocalDate.parse("2028-02-01"))
      response.data?.postRecallRelease?.calculatedDate.shouldBe(null)

      response.data?.release?.date.shouldBe(LocalDate.parse("2030-02-01"))
      response.data?.release?.confirmedDate.shouldBe(LocalDate.parse("2030-02-01"))
    }

    it("returns an error when 404 NOT FOUND is returned") {
      nomisApiMockServer.stubGetLatestSentenceKeyDatesForPerson(
        offenderNo,
        """
        {
          "developerMessage": "cannot find person"
        }
        """,
        HttpStatus.NOT_FOUND,
      )

      val response = nomisGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

      response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
    }
  },
)
