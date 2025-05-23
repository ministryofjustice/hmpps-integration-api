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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.OCNCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.OCNPagination
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiCaseNote

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCaseNotesForPersonService::class],
)
class GetCaseNotesForPersonServiceTest(
  @MockitoBean val caseNotesGateway: CaseNotesGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getCaseNoteForPersonService: GetCaseNotesForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val nomisNumber = "Z99999ZZ"
      val caseNoteFilter = CaseNoteFilter(hmppsId = hmppsId)
      val filters = null
      val oCNCaseNote =
        OCNCaseNote(
          content = listOf(PrisonApiCaseNote(caseNoteId = "abcd1234")),
          page = OCNPagination(page = 1, size = 10, totalElements = 10),
        )

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(caseNotesGateway)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(caseNotesGateway.getCaseNotesForPerson(id = nomisNumber, caseNoteFilter)).thenReturn(Response(oCNCaseNote))
      }

      it("performs a search according to hmpps Id") {
        getCaseNoteForPersonService.execute(caseNoteFilter, filters)
        verify(getPersonService, times(1)).getNomisNumberWithPrisonFilter(hmppsId, filters)
      }

      it("should return case notes from gateway") {
        val result = getCaseNoteForPersonService.execute(caseNoteFilter, filters)
        result.data.shouldNotBeNull()
        result.data!!
          .content.size
          .shouldBe(1)
        result.data!!
          .content
          .first()
          .caseNoteId
          .shouldBe("abcd1234")
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
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getCaseNoteForPersonService.execute(caseNoteFilter, filters)
        result.errors.shouldBe(errors)
      }

      it("return entity not found if getPersonService returns no nomis number") {
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = NomisNumber(null),
          ),
        )

        val result = getCaseNoteForPersonService.execute(caseNoteFilter, filters)
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
        whenever(caseNotesGateway.getCaseNotesForPerson(id = nomisNumber, caseNoteFilter)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getCaseNoteForPersonService.execute(caseNoteFilter, filters)
        result.errors.shouldBe(errors)
      }
    },
  )
