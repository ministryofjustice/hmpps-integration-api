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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetDynamicRisksForPersonService::class],
)
internal class GetDynamicRisksForPersonServiceTest(
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  @MockitoBean val personService: GetPersonService,
  private val getDynamicRisksForPersonService: GetDynamicRisksForPersonService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val nomisNumber = persona.identifiers.nomisNumber!!
      val deliusCrn = persona.identifiers.deliusCrn!!
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)
      val dynamicRisk = DynamicRisk(code = "RCCO", description = "Child Concerns", startDate = "2010-07-07")
      val nonMatchingDynamicRisk = DynamicRisk(code = "INVALID", description = "Invalid Dynamic Risk!", startDate = "2010-07-07")
      val dynamicRisks =
        listOf(
          dynamicRisk,
          nonMatchingDynamicRisk,
        )

      beforeEach {
        Mockito.reset(personService)
        Mockito.reset(nDeliusGateway)

        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = nomisNumber)).thenReturn(Response(person))

        whenever(nDeliusGateway.getDynamicRisksForPerson(deliusCrn)).thenReturn(Response(data = dynamicRisks))
      }

      it("gets a person from getPersonService") {
        getDynamicRisksForPersonService.execute(nomisNumber)
        verify(personService, times(1)).execute(hmppsId = nomisNumber)
      }

      it("gets dynamic risks from NDelius using a Delius crn number") {
        getDynamicRisksForPersonService.execute(deliusCrn)
        verify(nDeliusGateway, times(1)).getDynamicRisksForPerson(deliusCrn)
      }

      describe("when an upstream API returns an error when looking up a person by a Hmmps Id") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )

        beforeEach {
          whenever(personService.execute(hmppsId = nomisNumber)).thenReturn(
            Response(
              data = null,
              errors,
            ),
          )
        }

        it("records upstream API errors") {
          val response = getDynamicRisksForPersonService.execute(nomisNumber)
          response.errors.shouldBe(errors)
        }

        it("does not get dynamic risks from NDelius") {
          getDynamicRisksForPersonService.execute(nomisNumber)
          verify(nDeliusGateway, times(0)).getDynamicRisksForPerson(id = deliusCrn)
        }
      }

      it("records errors when it cannot find dynamic risks for a person") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(nDeliusGateway.getDynamicRisksForPerson(id = deliusCrn)).thenReturn(
          Response(
            data = emptyList(),
            errors,
          ),
        )

        val response = getDynamicRisksForPersonService.execute(nomisNumber)
        response.errors.shouldBe(errors)
      }

      it("returns dynamic risks filtered data") {
        val response = getDynamicRisksForPersonService.execute(nomisNumber)
        response.data.shouldBe(dynamicRisks)
      }
    },
  )
