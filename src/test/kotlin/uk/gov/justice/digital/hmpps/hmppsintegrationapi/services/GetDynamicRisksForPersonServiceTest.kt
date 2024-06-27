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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetDynamicRisksForPersonService::class],
)
internal class GetDynamicRisksForPersonServiceTest(
  @MockBean val nDeliusGateway: NDeliusGateway,
  @MockBean val personService: GetPersonService,
  private val getDynamicRisksForPersonService: GetDynamicRisksForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val deliusCrn = "X112233"
      val dynamicRisk = DynamicRisk(code = "RCCO", description = "Child Concerns", startDate = "2010-07-07")
      val nonMatchingDynamicRisk = DynamicRisk(code = "INVALID", description = "Invalid Dynamic Risk!", startDate = "2010-07-07")

      val person =
        Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(deliusCrn = deliusCrn))

      beforeEach {
        Mockito.reset(nDeliusGateway)
        Mockito.reset(personService)

        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))

        whenever(nDeliusGateway.getDynamicRisksForPerson(deliusCrn)).thenReturn(
          Response(
            data =
              listOf(
                dynamicRisk,
                nonMatchingDynamicRisk,
              ),
          ),
        )
      }

      it("gets a person from getPersonService") {
        getDynamicRisksForPersonService.execute(hmppsId)

        verify(personService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("gets dynamic risks from NDelius using a Delius crn number") {
        getDynamicRisksForPersonService.execute(hmppsId)

        verify(nDeliusGateway, VerificationModeFactory.times(1)).getDynamicRisksForPerson(deliusCrn)
      }

      describe("when an upstream API returns an error when looking up a person by a Hmmps Id") {
        beforeEach {
          whenever(personService.execute(hmppsId = hmppsId)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
        }

        it("records upstream API errors") {
          val response = getDynamicRisksForPersonService.execute(hmppsId)
          response.errors.shouldHaveSize(1)
        }

        it("does not get dynamic risks from NDelius") {
          getDynamicRisksForPersonService.execute(hmppsId)
          verify(nDeliusGateway, VerificationModeFactory.times(0)).getDynamicRisksForPerson(id = deliusCrn)
        }
      }

      it("records errors when it cannot find dynamic risks for a person") {
        whenever(nDeliusGateway.getDynamicRisksForPerson(id = deliusCrn)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NDELIUS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getDynamicRisksForPersonService.execute(hmppsId)
        response.errors.shouldHaveSize(1)
      }

      it("returns dynamic risks filtered data") {
        val response = getDynamicRisksForPersonService.execute(hmppsId)

        response.data.shouldHaveSize(1)
        response.data[0].code shouldBe "RCCO"
        response.data[0].description shouldBe "Child Concerns"
      }

      it("returns an error when the dynamic risk code is not in the allowed list") {
        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(nDeliusGateway.getDynamicRisksForPerson(deliusCrn)).thenReturn(
          Response(
            data = listOf(nonMatchingDynamicRisk),
          ),
        )

        val response = getDynamicRisksForPersonService.execute(hmppsId)

        response.errors.shouldHaveSize(0)
        response.data.shouldHaveSize(0)
      }
    },
  )
