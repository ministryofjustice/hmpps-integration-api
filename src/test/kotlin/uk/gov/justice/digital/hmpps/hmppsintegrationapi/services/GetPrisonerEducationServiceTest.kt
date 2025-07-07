package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.plp.PLPPrisonerEducation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.plp.PLPQualification
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonerEducationService::class],
)
internal class GetPrisonerEducationServiceTest(
  @MockitoBean val plpGateway: PLPGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getPrisonerEducationService: GetPrisonerEducationService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val nomisNumber = NomisNumber(hmppsId)
      val filters = null
      val timestamp = "2020-12-04T10:42:43"

      val plpPrisonerEducation =
        PLPPrisonerEducation(
          createdBy = "Joe Bloggs",
          createdByDisplayName = "Joe Bloggs",
          createdAt = LocalDateTime.parse(timestamp),
          createdAtPrison = "MKI",
          updatedBy = "Joe Bloggs",
          updatedByDisplayName = "Joe Bloggs",
          updatedAt = LocalDateTime.parse(timestamp),
          updatedAtPrison = "MKI",
          reference = "reference",
          educationLevel = "PRIMARY_SCHOOL",
          qualifications =
            listOf(
              PLPQualification(
                reference = "1234",
                subject = "Maths GCSE",
                level = "Entry Level",
                grade = "Distinction",
                createdBy = "Joe Bloggs",
                createdAt = LocalDateTime.parse(timestamp),
                createdAtPrison = "MKI",
                updatedBy = "Joe Bloggs",
                updatedAt = LocalDateTime.parse(timestamp),
                updatedAtPrison = "MKI",
              ),
            ),
        )

      val prisonerEducation = plpPrisonerEducation.toPrisonerEducation()

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(plpGateway)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(nomisNumber))
        whenever(plpGateway.getPrisonerEducation(hmppsId)).thenReturn(Response(plpPrisonerEducation))
      }

      it("performs a search according to hmpps Id") {
        getPrisonerEducationService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)
      }

      it("should return prisoner education") {
        val result = getPrisonerEducationService.execute(hmppsId, filters)
        result.data.shouldBe(prisonerEducation)
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISON_API,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = "notfound", filters = filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPrisonerEducationService.execute(hmppsId = "notfound", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if a bad request is made to getPersonService") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISON_API,
              type = UpstreamApiError.Type.BAD_REQUEST,
            ),
          )
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = "badRequest", filters = filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPrisonerEducationService.execute(hmppsId = "badRequest", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if plp gateway returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PLP,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(plpGateway.getPrisonerEducation(hmppsId)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPrisonerEducationService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
