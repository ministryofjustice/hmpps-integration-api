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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskAssessment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInNomisOnlyPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskCategoriesForPersonService::class],
)
internal class GetRiskCategoriesForPersonServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getRiskCategoriesForPersonService: GetRiskCategoriesForPersonService,
) : DescribeSpec(
    {
      val person =
        Person(
          firstName = personInNomisOnlyPersona.firstName,
          lastName = personInNomisOnlyPersona.lastName,
          identifiers = personInNomisOnlyPersona.identifiers,
        )

      val nomisNumber = person.identifiers.nomisNumber!!
      val hmppsId = nomisNumber
      val filters = ConsumerFilters(null)

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonApiGateway)

        whenever(getPersonService.getNomisNumber(hmppsId, filters)).thenReturn(Response(data = NomisNumber(nomisNumber)))
        whenever(prisonApiGateway.getRiskCategoriesForPerson(nomisNumber)).thenReturn(Response(data = RiskCategory()))
      }

      it("gets a person from getPersonService") {
        getRiskCategoriesForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getNomisNumber(hmppsId, filters)
      }

      it("gets a risk category for a person from ARN API using Nomis") {
        getRiskCategoriesForPersonService.execute(hmppsId, filters)
        verify(prisonApiGateway, times(1)).getRiskCategoriesForPerson(nomisNumber)
      }

      it("returns a risk category for a person") {
        val riskCategory = RiskCategory(offenderNo = nomisNumber, assessments = listOf(RiskAssessment(classificationCode = "987")))
        whenever(prisonApiGateway.getRiskCategoriesForPerson(nomisNumber)).thenReturn(
          Response(data = riskCategory),
        )

        val response = getRiskCategoriesForPersonService.execute(hmppsId, filters)
        response.data.shouldBe(riskCategory)
      }

      it("returns error when prisonApiGateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISON_API,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(prisonApiGateway.getRiskCategoriesForPerson(nomisNumber)).thenReturn(
          Response(
            data = RiskCategory(),
            errors,
          ),
        )

        val response = getRiskCategoriesForPersonService.execute(hmppsId, filters)
        response.errors.shouldBe(errors)
      }
    },
  )
