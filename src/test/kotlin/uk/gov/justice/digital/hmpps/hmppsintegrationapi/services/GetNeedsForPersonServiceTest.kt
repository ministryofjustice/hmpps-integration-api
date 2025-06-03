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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationOnlyPersona
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetNeedsForPersonService::class],
)
internal class GetNeedsForPersonServiceTest(
  @MockitoBean val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getNeedsForPersonService: GetNeedsForPersonService,
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
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)).thenReturn(Response(data = null))
      }

      it("gets a person from getPersonService") {
        getNeedsForPersonService.execute(hmppsId)
        verify(getPersonService, times(1)).execute(hmppsId = hmppsId)
      }

      it("gets needs for a person from ARN API using a CRN") {
        getNeedsForPersonService.execute(hmppsId)

        verify(assessRisksAndNeedsGateway, times(1)).getNeedsForPerson(deliusCrn)
      }

      it("returns needs for a person") {
        val needs =
          Needs(
            assessedOn = LocalDateTime.now(),
            identifiedNeeds =
              listOf(
                Need(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY", riskOfHarm = null, riskOfReoffending = false, severity = "MINOR"),
                Need(type = "FINANCIAL_MANAGEMENT_AND_INCOME", riskOfHarm = false, riskOfReoffending = true, severity = "MINOR"),
              ),
            notIdentifiedNeeds =
              listOf(
                Need(type = "RELATIONSHIPS", riskOfHarm = null, riskOfReoffending = true, severity = "SEVERE"),
              ),
            unansweredNeeds =
              listOf(
                Need(type = "LIFESTYLE_AND_ASSOCIATES", riskOfHarm = null, riskOfReoffending = true, severity = "SEVERE"),
                Need(type = "DRUG_MISUSE", riskOfHarm = true, riskOfReoffending = true, severity = "SEVERE"),
                Need(type = "ALCOHOL_MISUSE", riskOfHarm = false, riskOfReoffending = false, severity = "MINOR"),
              ),
          )

        whenever(assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)).thenReturn(Response(data = needs))

        val response = getNeedsForPersonService.execute(hmppsId)
        response.data.shouldBe(needs)
      }

      describe("when an upstream API returns an error") {
        describe("when a person cannot be found by hmpps ID in probation offender search") {
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

          it("records upstream error from getPersonService") {
            val response = getNeedsForPersonService.execute(hmppsId)
            response.errors.shouldBe(errors)
          }

          it("does not get needs from ARN") {
            getNeedsForPersonService.execute(hmppsId)
            verify(assessRisksAndNeedsGateway, times(0)).getNeedsForPerson(id = deliusCrn)
          }
        }

        it("returns error from ARN API when person/crn cannot be found in ARN") {
          val errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            )
          whenever(assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)).thenReturn(
            Response(
              data = null,
              errors,
            ),
          )

          val response = getNeedsForPersonService.execute(hmppsId)
          response.errors.shouldBe(errors)
        }
      }
    },
  )
