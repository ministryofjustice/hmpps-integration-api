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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationOnlyPersona
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskSeriousHarmForPersonService::class],
)
internal class
GetRiskSeriousHarmForPersonServiceTest(
  @MockitoBean val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getRiskSeriousHarmForPersonService: GetRiskSeriousHarmForPersonService,
) : DescribeSpec(
    {
      val personFromProbationOffenderSearch =
        Person(
          firstName = personInProbationOnlyPersona.firstName,
          lastName = personInProbationOnlyPersona.lastName,
          identifiers = personInProbationOnlyPersona.identifiers,
        )

      val deliusCrn = personFromProbationOffenderSearch.identifiers.deliusCrn!!
      val hmppsId = deliusCrn

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(assessRisksAndNeedsGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(data = personFromProbationOffenderSearch),
        )
        whenever(assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)).thenReturn(Response(data = null))
      }

      it("gets a person from getPersonService") {
        getRiskSeriousHarmForPersonService.execute(hmppsId)
        verify(getPersonService, times(1)).execute(hmppsId = hmppsId)
      }

      it("gets risks for a person from ARN API using a CRN") {
        getRiskSeriousHarmForPersonService.execute(hmppsId)
        verify(assessRisksAndNeedsGateway, times(1)).getRiskSeriousHarmForPerson(deliusCrn)
      }

      it("returns risks for a person") {
        val risks = Risks(assessedOn = LocalDateTime.now())
        whenever(assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)).thenReturn(Response(data = risks))

        val response = getRiskSeriousHarmForPersonService.execute(hmppsId)
        response.data.shouldBe(risks)
      }

      describe("when an upstream API returns an error") {
        describe("when getPersonService returns an error") {
          val errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            )

          beforeEach {
            whenever(getPersonService.execute(hmppsId)).thenReturn(
              Response(
                data = null,
                errors,
              ),
            )
          }

          it("returns the errors") {
            val response = getRiskSeriousHarmForPersonService.execute(hmppsId)
            response.errors.shouldBe(errors)
          }

          it("does not get risks from ARN") {
            getRiskSeriousHarmForPersonService.execute(hmppsId)
            verify(assessRisksAndNeedsGateway, times(0)).getRiskSeriousHarmForPerson(id = deliusCrn)
          }
        }

        it("returns error when assessRisksAndNeedsGateway returns an error") {
          val errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            )
          whenever(assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)).thenReturn(
            Response(
              data = null,
              errors,
            ),
          )

          val response = getRiskSeriousHarmForPersonService.execute(hmppsId)
          response.errors.shouldBe(errors)
        }
      }
    },
  )
