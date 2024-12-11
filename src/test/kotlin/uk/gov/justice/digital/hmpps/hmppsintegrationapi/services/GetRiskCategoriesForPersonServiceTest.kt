package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskAssessment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskCategoriesForPersonService::class],
)
internal class GetRiskCategoriesForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getRiskCategoriesForPersonService: GetRiskCategoriesForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val nomisNumber = "A7796DY"

      val personFromProbationOffenderSearch =
        Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(nomisNumber = nomisNumber))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(nomisGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nomisGateway.getRiskCategoriesForPerson(nomisNumber)).thenReturn(Response(data = RiskCategory()))
      }

      it("gets a person from getPersonService") {
        getRiskCategoriesForPersonService.execute(hmppsId)

        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("gets a risk category for a person from ARN API using Nomis") {
        getRiskCategoriesForPersonService.execute(hmppsId)

        verify(nomisGateway, VerificationModeFactory.times(1)).getRiskCategoriesForPerson(nomisNumber)
      }

      it("returns a risk category for a person") {
        val riskCategory = RiskCategory(offenderNo = "A7796DY", assessments = listOf(RiskAssessment(classificationCode = "987")))

        whenever(nomisGateway.getRiskCategoriesForPerson(nomisNumber)).thenReturn(
          Response(data = riskCategory),
        )

        val response = getRiskCategoriesForPersonService.execute(hmppsId)

        response.data.shouldBe(riskCategory)
      }

      describe("when an upstream API returns an error") {

        it("returns error from ARN API when person cannot be found in ARN") {

          whenever(nomisGateway.getRiskCategoriesForPerson(nomisNumber)).thenReturn(
            Response(
              data = RiskCategory(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getRiskCategoriesForPersonService.execute(hmppsId)

          response.errors.shouldHaveSize(1)
          response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
          response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }
      }
    },
  )
