package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAssessmentSummary

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCsraForPersonService::class],
)
class GetCsraForPersonServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getCsraForPersonService: GetCsraForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val nomisNumber = "Z99999ZZ"
      val filters = null

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonApiGateway)

        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(prisonApiGateway.getCsraAssessmentsForPerson(nomisNumber)).thenReturn(Response(listOf(PrisonApiAssessmentSummary(offenderNo = hmppsId, cellSharingAlertFlag = true))))
      }

      it("performs a search according to hmpps Id") {
        getCsraForPersonService.getCsraAssessments(hmppsId, filters)
        verify(getPersonService, times(1)).getNomisNumber(hmppsId, filters)
      }

      it("should return case notes from gateway") {
        val result = getCsraForPersonService.getCsraAssessments(hmppsId, filters)
        result.data.shouldNotBeNull()
        result.data
          .size
          .shouldBe(1)
        result.data
          .first()
          .offenderNo
          .shouldBe(hmppsId)
        result.errors.count().shouldBe(0)
      }

      it("return errors if getPersonService returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.PRISON_API,
              description = "Mock error from person service",
            ),
          )
        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getCsraForPersonService.getCsraAssessments(hmppsId, filters)
        result.errors.shouldBe(errors)
      }

      it("return entity not found if getPersonService returns no nomis number") {
        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(
          Response(
            data = NomisNumber(null),
          ),
        )

        val result = getCsraForPersonService.getCsraAssessments(hmppsId, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }

      it("return errors if case notes gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.CASE_NOTES,
              description = "Mock error from case notes gateway",
            ),
          )
        whenever(prisonApiGateway.getCsraAssessmentsForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = errors,
          ),
        )

        val result = getCsraForPersonService.getCsraAssessments(hmppsId, filters)
        result.errors.shouldBe(errors)
      }
    },
  )
