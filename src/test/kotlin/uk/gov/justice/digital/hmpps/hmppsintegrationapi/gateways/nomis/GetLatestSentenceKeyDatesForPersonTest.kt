package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.RESTAPICLIENT_FOR_PRISON_API_GATEWAY
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiOffenderSentence
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetLatestSentenceKeyDatesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "A1234AB"
      val sentencePath = "/api/offenders/$offenderNo/sentences"
      val responseJson = """
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
              "confirmedReleaseDate": "2030-02-01",
              "effectiveSentenceEndDate": "2025-02-01",
              "sentenceExpiryCalculatedDate": "2025-02-01",
              "sentenceExpiryDate": "2025-02-01",
              "sentenceExpiryOverrideDate": "2025-02-01",
              "sentenceStartDate": "2025-02-01",
              "topupSupervisionExpiryCalculatedDate": "2022-04-01",
              "topupSupervisionExpiryDate": "2022-04-01",
              "topupSupervisionExpiryOverrideDate": "2022-04-01",
              "topupSupervisionStartDate": "2022-04-01",
              "actualParoleDate": "2031-02-01",
              "earlyRemovalSchemeEligibilityDate": "2031-02-01",
              "releaseOnTemporaryLicenceDate": "2031-02-01",
              "tariffDate": "2031-02-01",
              "tariffEarlyRemovalSchemeEligibilityDate": "2031-02-01"
            }
          }
        """
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          sentencePath,
          responseJson,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns latest sentence key dates for a person with the matching ID") {
        val response = prisonApiGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

        response.data
          ?.automaticRelease
          ?.date
          .shouldBe(LocalDate.parse("2023-03-01"))
        response.data
          ?.automaticRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-03-01"))

        response.data
          ?.conditionalRelease
          ?.date
          .shouldBe(LocalDate.parse("2023-04-01"))
        response.data
          ?.conditionalRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-04-01"))

        response.data
          ?.dtoPostRecallRelease
          ?.date
          .shouldBe(LocalDate.parse("2023-05-01"))
        response.data
          ?.dtoPostRecallRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-05-01"))

        response.data
          ?.earlyTerm
          ?.date
          .shouldBe(LocalDate.parse("2021-04-01"))
        response.data
          ?.earlyTerm
          ?.overrideDate
          .shouldBe(LocalDate.parse("2021-04-01"))
        response.data
          ?.earlyTerm
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2021-04-01"))

        response.data
          ?.homeDetentionCurfew
          ?.actualDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.eligibilityCalculatedDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.eligibilityDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.eligibilityOverrideDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.endDate
          .shouldBe(LocalDate.parse("2022-05-01"))

        response.data
          ?.lateTerm
          ?.date
          .shouldBe(LocalDate.parse("2022-02-01"))
        response.data
          ?.lateTerm
          ?.overrideDate
          .shouldBe(LocalDate.parse("2022-02-01"))
        response.data
          ?.lateTerm
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2022-02-01"))

        response.data
          ?.licenceExpiry
          ?.date
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.licenceExpiry
          ?.overrideDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.licenceExpiry
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2025-02-01"))

        response.data
          ?.midTerm
          ?.date
          .shouldBe(LocalDate.parse("2023-02-01"))
        response.data
          ?.midTerm
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-02-01"))
        response.data
          ?.midTerm
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2023-02-01"))

        response.data
          ?.nonDto
          ?.date
          .shouldBe(LocalDate.parse("2023-02-01"))
        response.data
          ?.nonDto
          ?.releaseDateType
          .shouldBe("CRD")

        response.data
          ?.nonParole
          ?.date
          .shouldBe(LocalDate.parse("2026-02-01"))
        response.data
          ?.nonParole
          ?.overrideDate
          .shouldBe(LocalDate.parse("2026-02-01"))

        response.data
          ?.paroleEligibility
          ?.date
          .shouldBe(LocalDate.parse("2027-02-01"))
        response.data
          ?.paroleEligibility
          ?.overrideDate
          .shouldBe(LocalDate.parse("2027-02-01"))
        response.data
          ?.paroleEligibility
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2027-02-01"))

        response.data
          ?.postRecallRelease
          ?.date
          .shouldBe(LocalDate.parse("2028-02-01"))
        response.data
          ?.postRecallRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2028-02-01"))

        response.data
          ?.release
          ?.date
          .shouldBe(LocalDate.parse("2030-02-01"))
        response.data
          ?.release
          ?.confirmedDate
          .shouldBe(LocalDate.parse("2030-02-01"))

        response.data
          ?.sentence
          ?.effectiveEndDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.expiryCalculatedDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.expiryDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.expiryOverrideDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.startDate
          .shouldBe(LocalDate.parse("2025-02-01"))

        response.data
          ?.topupSupervision
          ?.expiryCalculatedDate
          .shouldBe(LocalDate.parse("2022-04-01"))
        response.data
          ?.topupSupervision
          ?.expiryDate
          .shouldBe(LocalDate.parse("2022-04-01"))
        response.data
          ?.topupSupervision
          ?.expiryOverrideDate
          .shouldBe(LocalDate.parse("2022-04-01"))
        response.data
          ?.topupSupervision
          ?.startDate
          .shouldBe(LocalDate.parse("2022-04-01"))

        response.data?.actualParoleDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.earlyRemovalSchemeEligibilityDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.releaseOnTemporaryLicenceDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.tariffDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.tariffEarlyRemovalSchemeEligibilityDate?.shouldBe(LocalDate.parse("2031-02-01"))
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubForGet(
          sentencePath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = prisonApiGateway.getLatestSentenceKeyDatesForPerson(offenderNo)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"
        val headers = mapOf("Authorization" to "Bearer $authToken")

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS")).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(sentencePath), eq(PrisonApiOffenderSentence::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, PrisonApiOffenderSentence::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getLatestSentenceKeyDatesForPerson(offenderNo)

        // Then
        response.data
          ?.automaticRelease
          ?.date
          .shouldBe(LocalDate.parse("2023-03-01"))
        response.data
          ?.automaticRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-03-01"))

        response.data
          ?.conditionalRelease
          ?.date
          .shouldBe(LocalDate.parse("2023-04-01"))
        response.data
          ?.conditionalRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-04-01"))

        response.data
          ?.dtoPostRecallRelease
          ?.date
          .shouldBe(LocalDate.parse("2023-05-01"))
        response.data
          ?.dtoPostRecallRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-05-01"))

        response.data
          ?.earlyTerm
          ?.date
          .shouldBe(LocalDate.parse("2021-04-01"))
        response.data
          ?.earlyTerm
          ?.overrideDate
          .shouldBe(LocalDate.parse("2021-04-01"))
        response.data
          ?.earlyTerm
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2021-04-01"))

        response.data
          ?.homeDetentionCurfew
          ?.actualDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.eligibilityCalculatedDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.eligibilityDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.eligibilityOverrideDate
          .shouldBe(LocalDate.parse("2022-05-01"))
        response.data
          ?.homeDetentionCurfew
          ?.endDate
          .shouldBe(LocalDate.parse("2022-05-01"))

        response.data
          ?.lateTerm
          ?.date
          .shouldBe(LocalDate.parse("2022-02-01"))
        response.data
          ?.lateTerm
          ?.overrideDate
          .shouldBe(LocalDate.parse("2022-02-01"))
        response.data
          ?.lateTerm
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2022-02-01"))

        response.data
          ?.licenceExpiry
          ?.date
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.licenceExpiry
          ?.overrideDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.licenceExpiry
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2025-02-01"))

        response.data
          ?.midTerm
          ?.date
          .shouldBe(LocalDate.parse("2023-02-01"))
        response.data
          ?.midTerm
          ?.overrideDate
          .shouldBe(LocalDate.parse("2023-02-01"))
        response.data
          ?.midTerm
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2023-02-01"))

        response.data
          ?.nonDto
          ?.date
          .shouldBe(LocalDate.parse("2023-02-01"))
        response.data
          ?.nonDto
          ?.releaseDateType
          .shouldBe("CRD")

        response.data
          ?.nonParole
          ?.date
          .shouldBe(LocalDate.parse("2026-02-01"))
        response.data
          ?.nonParole
          ?.overrideDate
          .shouldBe(LocalDate.parse("2026-02-01"))

        response.data
          ?.paroleEligibility
          ?.date
          .shouldBe(LocalDate.parse("2027-02-01"))
        response.data
          ?.paroleEligibility
          ?.overrideDate
          .shouldBe(LocalDate.parse("2027-02-01"))
        response.data
          ?.paroleEligibility
          ?.calculatedDate
          .shouldBe(LocalDate.parse("2027-02-01"))

        response.data
          ?.postRecallRelease
          ?.date
          .shouldBe(LocalDate.parse("2028-02-01"))
        response.data
          ?.postRecallRelease
          ?.overrideDate
          .shouldBe(LocalDate.parse("2028-02-01"))

        response.data
          ?.release
          ?.date
          .shouldBe(LocalDate.parse("2030-02-01"))
        response.data
          ?.release
          ?.confirmedDate
          .shouldBe(LocalDate.parse("2030-02-01"))

        response.data
          ?.sentence
          ?.effectiveEndDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.expiryCalculatedDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.expiryDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.expiryOverrideDate
          .shouldBe(LocalDate.parse("2025-02-01"))
        response.data
          ?.sentence
          ?.startDate
          .shouldBe(LocalDate.parse("2025-02-01"))

        response.data
          ?.topupSupervision
          ?.expiryCalculatedDate
          .shouldBe(LocalDate.parse("2022-04-01"))
        response.data
          ?.topupSupervision
          ?.expiryDate
          .shouldBe(LocalDate.parse("2022-04-01"))
        response.data
          ?.topupSupervision
          ?.expiryOverrideDate
          .shouldBe(LocalDate.parse("2022-04-01"))
        response.data
          ?.topupSupervision
          ?.startDate
          .shouldBe(LocalDate.parse("2022-04-01"))

        response.data?.actualParoleDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.earlyRemovalSchemeEligibilityDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.releaseOnTemporaryLicenceDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.tariffDate?.shouldBe(LocalDate.parse("2031-02-01"))
        response.data?.tariffEarlyRemovalSchemeEligibilityDate?.shouldBe(LocalDate.parse("2031-02-01"))
      }
    },
  )
