package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
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
      val hmppsId = "1234/56789B"
      val deliusCrn = "X123456"

      val personFromProbationOffenderSearch =
        Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(deliusCrn = deliusCrn))

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

        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("gets risks for a person from ARN API using a CRN") {
        getRiskSeriousHarmForPersonService.execute(hmppsId)

        verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(1)).getRiskSeriousHarmForPerson(deliusCrn)
      }

      it("returns risks for a person") {
        val risks = Risks(assessedOn = LocalDateTime.now())

        whenever(assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)).thenReturn(Response(data = risks))

        val response = getRiskSeriousHarmForPersonService.execute(hmppsId)

        response.data.shouldBe(risks)
      }

      describe("when an upstream API returns an error") {
        xdescribe("when a person cannot be found by hmpps Id in probation offender search") {
          beforeEach {
            whenever(getPersonService.execute(hmppsId)).thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                      type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    ),
                    UpstreamApiError(
                      causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                      type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    ),
                  ),
              ),
            )
          }

          it("records upstream 404 API error for probation offender search") {
            val response = getRiskSeriousHarmForPersonService.execute(hmppsId)

            response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, UpstreamApi.PROBATION_OFFENDER_SEARCH).shouldBe(true)
          }

          it("records upstream 403 API error for probation offender search") {
            val response = getRiskSeriousHarmForPersonService.execute(hmppsId)

            response.data.shouldBeNull()
            response.errors.shouldHaveSize(1)
            response.hasErrorCausedBy(UpstreamApiError.Type.FORBIDDEN, UpstreamApi.ASSESS_RISKS_AND_NEEDS).shouldBe(true)
          }

          it("does not get risks from ARN") {
            getRiskSeriousHarmForPersonService.execute(hmppsId)

            verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(0)).getRiskSeriousHarmForPerson(id = deliusCrn)
          }
        }

        it("returns error from ARN API when person/crn cannot be found in ARN") {
          whenever(assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getRiskSeriousHarmForPersonService.execute(hmppsId)

          response.errors.shouldHaveSize(1)
          response.errors
            .first()
            .causedBy
            .shouldBe(UpstreamApi.ASSESS_RISKS_AND_NEEDS)
          response.errors
            .first()
            .type
            .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }
      }
    },
  )
