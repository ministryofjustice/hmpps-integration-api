package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Needs as IntegrationApiNeeds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UnansweredNeeds as IntegrationApiUnansweredNeeds

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetNeedsForPersonService::class],
)
internal class GetNeedsForPersonServiceTest(
  @MockBean val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getNeedsForPersonService: GetNeedsForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val deliusCrn = "X123456"

    val personFromPrisonOffenderSearch =
      Person(firstName = "Chandler", lastName = "Bing", identifiers = Identifiers(nomisNumber = nomisNumber))
    val personFromProbationOffenderSearch =
      Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(deliusCrn = deliusCrn))

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(assessRisksAndNeedsGateway)

      whenever(getPersonService.execute(pncId = pncId)).thenReturn(
        Response(
          data = mapOf(
            "prisonerOffenderSearch" to personFromPrisonOffenderSearch,
            "probationOffenderSearch" to personFromProbationOffenderSearch,
          ),
        ),
      )

      whenever(assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)).thenReturn(Response(data = null))
    }

    it("retrieves a person from getPersonService") {
      getNeedsForPersonService.execute(pncId)

      verify(getPersonService, VerificationModeFactory.times(1)).execute(pncId = pncId)
    }

    it("retrieves needs for a person from ARN API using a CRN") {
      getNeedsForPersonService.execute(pncId)

      verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(1)).getNeedsForPerson(deliusCrn)
    }

    it("returns needs for a person") {
      val needs = IntegrationApiNeeds(
        assessedOn = LocalDateTime.now(),
        unansweredNeeds = IntegrationApiUnansweredNeeds(type = "RELATIONSHIPS"),
      )

      whenever(assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)).thenReturn(Response(data = needs))

      val response = getNeedsForPersonService.execute(pncId)

      response.data.shouldBe(needs)
    }

    describe("when an upstream API returns an error") {
      xdescribe("when a person cannot be found by pnc ID in probation offender search") {
        beforeEach {
          whenever(getPersonService.execute(pncId)).thenReturn(
            Response(
              data = mapOf(
                "prisonerOffenderSearch" to null,
                "probationOffenderSearch" to null,
              ),
              errors = listOf(
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

        it("records upstream API error for probation offender search") {
          val response = getNeedsForPersonService.execute(pncId)

          response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, UpstreamApi.PROBATION_OFFENDER_SEARCH).shouldBe(true)
        }

        it("does not get needs from ARN") {
          getNeedsForPersonService.execute(pncId)

          verify(assessRisksAndNeedsGateway, VerificationModeFactory.times(0)).getNeedsForPerson(id = deliusCrn)
        }
      }

      it("returns error from ARN API when person/crn cannot be found in ARN") {
        whenever(assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)).thenReturn(
          Response(
            data = null,
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val response = getNeedsForPersonService.execute(pncId)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.ASSESS_RISKS_AND_NEEDS)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    }
  },
)
