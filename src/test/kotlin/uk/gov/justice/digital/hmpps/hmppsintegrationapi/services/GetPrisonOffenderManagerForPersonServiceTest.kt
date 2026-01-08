package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Prison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonOffenderManagerForPersonService::class],
)
class GetPrisonOffenderManagerForPersonServiceTest(
  @MockitoBean val managePOMCaseGateway: ManagePOMCaseGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getPrisonOffenderManagerForPersonService: GetPrisonOffenderManagerForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "56789B"
      val nomisNumber = "Z99999ZZ"
      val filters = null

      val prisonOffenderManager = PrisonOffenderManager(forename = "Paul", surname = "Smith", prison = Prison(code = "RED"))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(managePOMCaseGateway)

        whenever(getPersonService.getNomisNumberWithFiltering(hmppsId = hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomsNumber = nomisNumber)).thenReturn(Response(prisonOffenderManager))
      }

      it("performs a search according to hmpps Id") {
        getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getNomisNumberWithFiltering(hmppsId = hmppsId, filters)
      }

      it("Returns a prison offender manager for person given a hmppsId") {
        val result = getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
        result.shouldBe(Response(data = prisonOffenderManager))
      }

      it("should return errors if getPersonService returns errors") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISON_API,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getNomisNumberWithFiltering(hmppsId = "NOT_FOUND", filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPrisonOffenderManagerForPersonService.execute("NOT_FOUND", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("Should return null if Manage POM gateway returns 404") {
        whenever(managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomsNumber = nomisNumber)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_POM_CASE, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )

        val result = getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBeEmpty()
      }

      it("Should return errors if Manage POM gateway returns non 404 errors") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.MANAGE_POM_CASE,
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              description = "Error returned by Manage POM gateway",
            ),
          )
        whenever(managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomsNumber = nomisNumber)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
