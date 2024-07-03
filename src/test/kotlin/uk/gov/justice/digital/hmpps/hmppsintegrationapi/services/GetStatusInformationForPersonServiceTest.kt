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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetStatusInformationForPersonService::class],
)
internal class GetStatusInformationForPersonServiceTest(
  @MockBean val nDeliusGateway: NDeliusGateway,
  @MockBean val personService: GetPersonService,
  private val getStatusInformationForPersonService: GetStatusInformationForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val deliusCrn = "X112233"
      val statusInformation = StatusInformation(code = "ASFO", description = "Serious Further Offence - Subject to SFO review/investigation", startDate = "2013-10-17")
      val nonMatchingStatusInformation = StatusInformation(code = "INVALID", description = "Invalid status information data", startDate = "2010-07-07")

      val person =
        Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(deliusCrn = deliusCrn))

      beforeEach {
        Mockito.reset(nDeliusGateway)
        Mockito.reset(personService)

        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))

        whenever(nDeliusGateway.getStatusInformationForPerson(deliusCrn)).thenReturn(
          Response(
            data =
              listOf(
                statusInformation,
                nonMatchingStatusInformation,
              ),
          ),
        )
      }

      it("gets a person from getPersonService") {
        getStatusInformationForPersonService.execute(hmppsId)

        verify(personService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("gets person status from NDelius using a Delius crn number") {
        getStatusInformationForPersonService.execute(hmppsId)

        verify(nDeliusGateway, VerificationModeFactory.times(1)).getStatusInformationForPerson(deliusCrn)
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
          val response = getStatusInformationForPersonService.execute(hmppsId)
          response.errors.shouldHaveSize(1)
        }

        it("does not get person status data from NDelius") {
          getStatusInformationForPersonService.execute(hmppsId)
          verify(nDeliusGateway, VerificationModeFactory.times(0)).getStatusInformationForPerson(id = deliusCrn)
        }
      }

      it("records errors when it cannot find person status for a person") {
        whenever(nDeliusGateway.getStatusInformationForPerson(id = deliusCrn)).thenReturn(
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

        val response = getStatusInformationForPersonService.execute(hmppsId)
        response.errors.shouldHaveSize(1)
      }

      it("returns person status filtered data") {
        val response = getStatusInformationForPersonService.execute(hmppsId)

        response.data.shouldHaveSize(1)
        response.data[0].code shouldBe "ASFO"
        response.data[0].description shouldBe "Serious Further Offence - Subject to SFO review/investigation"
      }

      it("returns an error when the person status code is not in the allowed list") {
        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(nDeliusGateway.getStatusInformationForPerson(deliusCrn)).thenReturn(
          Response(
            data = listOf(nonMatchingStatusInformation),
          ),
        )

        val response = getStatusInformationForPersonService.execute(hmppsId)

        response.errors.shouldHaveSize(0)
        response.data.shouldHaveSize(0)
      }
    },
  )
