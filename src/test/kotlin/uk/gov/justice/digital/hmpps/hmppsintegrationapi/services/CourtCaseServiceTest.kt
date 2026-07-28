package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RemandAndSentencingGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasCharge
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasCourtAppearance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasSentencedCourtCase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasSentencedCourtCases
import java.time.LocalDate

class CourtCaseServiceTest {
  val remandAndSentencingGateway: RemandAndSentencingGateway = mock()
  val personService: GetPersonService = mock()
  val courtCaseService: CourtCaseService = CourtCaseService(remandAndSentencingGateway, personService)

  val requestContext = buildRequestContext()

  val rasSentencedCourtCases =
    RasSentencedCourtCases(
      listOf(
        RasSentencedCourtCase(
          latestAppearance =
            RasCourtAppearance(
              charges =
                listOf(RasCharge(sentence = RasSentence(convictionDate = LocalDate.of(2021, 4, 12)))),
            ),
          appearances =
            listOf(
              RasCourtAppearance(
                charges =
                  listOf(RasCharge(sentence = RasSentence(convictionDate = LocalDate.of(2021, 3, 12)))),
              ),
            ),
        ),
        RasSentencedCourtCase(
          latestAppearance =
            RasCourtAppearance(
              charges =
                listOf(RasCharge(sentence = RasSentence(convictionDate = LocalDate.of(2021, 4, 12)))),
            ),
          appearances =
            listOf(
              RasCourtAppearance(
                charges =
                  listOf(RasCharge(sentence = RasSentence(convictionDate = LocalDate.of(2020, 2, 19)))),
              ),
            ),
        ),
      ),
    )

  @BeforeEach
  fun setUp() {
    whenever(personService.getNomisNumber(any(), any())).thenReturn(Response(NomisNumber("A1234AB")))
    whenever(remandAndSentencingGateway.getSentencedCourtCases(any(), any())).thenReturn(Response(rasSentencedCourtCases))
  }

  @Test
  fun `successfully returns a court case history summary including the earliest conviction date`() {
    courtCaseService.getCourtCaseDetails("X123456", requestContext).data?.dateOfFirstConviction shouldBe LocalDate.of(2020, 2, 19)
  }

  @Test
  fun `returns an error because an error occurred when trying to resolve the nomis number`() {
    whenever(personService.getNomisNumber(any(), any())).thenReturn(Response(null, listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))))
    val response = courtCaseService.getCourtCaseDetails("X123456", requestContext)
    response.data shouldBe null
    response.errors shouldHaveSize 1
    response.errors.first().type shouldBe UpstreamApiError.Type.INTERNAL_SERVER_ERROR
  }

  @Test
  fun `returns an error because a null nomis number has been returned`() {
    whenever(personService.getNomisNumber(any(), any())).thenReturn(Response(null))
    val response = courtCaseService.getCourtCaseDetails("X123456", requestContext)
    response.data shouldBe null
    response.errors shouldHaveSize 1
    response.errors.first().type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
  }
}
