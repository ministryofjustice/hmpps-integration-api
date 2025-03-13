package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
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
      val person = Person(firstName = "Julianna", lastName = "Blake", identifiers = Identifiers(nomisNumber = nomisNumber))
      val filters = null

      val prisonOffenderManager = PrisonOffenderManager(forename = "Paul", surname = "Smith", prison = Prison(code = "RED"))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(managePOMCaseGateway)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(managePOMCaseGateway.getPrimaryPOMForNomisNumber(id = nomisNumber)).thenReturn(Response(prisonOffenderManager))
      }

      it("performs a search according to hmpps Id") {
        getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)
      }

      it("Returns a prison offender manager for person given a hmppsId") {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = person,
          ),
        )
        val result = getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
        result.shouldBe(Response(data = prisonOffenderManager))
      }

      it("should return a list of errors if person not found") {
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = "NOT_FOUND", filters)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.MANAGE_POM_CASE,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getPrisonOffenderManagerForPersonService.execute("NOT_FOUND", filters)
        result.data.shouldBe(PrisonOffenderManager())
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
