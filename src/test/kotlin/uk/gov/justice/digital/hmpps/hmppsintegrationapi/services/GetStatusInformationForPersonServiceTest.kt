package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetStatusInformationForPersonService::class],
)
internal class GetStatusInformationForPersonServiceTest(
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  @MockitoBean val personService: GetPersonService,
  private val getStatusInformationForPersonService: GetStatusInformationForPersonService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val nomisNumber = persona.identifiers.nomisNumber!!
      val deliusCrn = persona.identifiers.deliusCrn!!
      val person =
        Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = Identifiers(deliusCrn = deliusCrn))
      val statusInformation = StatusInformation(code = "ASFO", description = "Serious Further Offence - Subject to SFO review/investigation", startDate = "2013-10-17")
      val nonMatchingStatusInformation = StatusInformation(code = "INVALID", description = "Invalid status information data", startDate = "2010-07-07")
      val statusInformationList =
        listOf(
          statusInformation,
          nonMatchingStatusInformation,
        )

      beforeEach {
        Mockito.reset(personService)
        Mockito.reset(nDeliusGateway)

        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = nomisNumber)).thenReturn(Response(person))

        whenever(nDeliusGateway.getStatusInformationForPerson(deliusCrn)).thenReturn(Response(data = statusInformationList))
      }

      it("gets a person from getPersonService") {
        getStatusInformationForPersonService.execute(nomisNumber)
        verify(personService, times(1)).execute(hmppsId = nomisNumber)
      }

      it("gets person status from NDelius using a Delius crn number") {
        getStatusInformationForPersonService.execute(deliusCrn)
        verify(nDeliusGateway, times(1)).getStatusInformationForPerson(deliusCrn)
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
          val response = getStatusInformationForPersonService.execute(nomisNumber)
          response.errors.shouldBe(errors)
        }

        it("does not get person status data from NDelius") {
          getStatusInformationForPersonService.execute(nomisNumber)
          verify(nDeliusGateway, times(0)).getStatusInformationForPerson(id = deliusCrn)
        }
      }

      it("records errors when it cannot find person status for a person") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(nDeliusGateway.getStatusInformationForPerson(id = deliusCrn)).thenReturn(
          Response(
            data = emptyList(),
            errors,
          ),
        )

        val response = getStatusInformationForPersonService.execute(nomisNumber)
        response.errors.shouldBe(errors)
      }

      it("returns person status filtered data") {
        val response = getStatusInformationForPersonService.execute(nomisNumber)
        response.data.shouldBe(listOf(statusInformation))
      }

      it("returns an error when the person status code is not in the allowed list") {
        whenever(nDeliusGateway.getStatusInformationForPerson(deliusCrn)).thenReturn(
          Response(
            data = listOf(nonMatchingStatusInformation),
          ),
        )

        val response = getStatusInformationForPersonService.execute(nomisNumber)
        response.data.shouldHaveSize(0)
        response.errors.shouldHaveSize(0)
      }
    },
  )
