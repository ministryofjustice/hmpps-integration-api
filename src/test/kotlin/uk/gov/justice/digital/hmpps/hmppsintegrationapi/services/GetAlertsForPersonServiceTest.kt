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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAlertsForPersonService::class],
)
internal class GetAlertsForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val personService: GetPersonService,
  private val getAlertsForPersonService: GetAlertsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val prisonerNumber = "Z99999ZZ"
      val deliusCrn = "X777776"
      val alert = Alert(code = "XA", codeDescription = "Test Alert XA")
      val nonMatchingAlert = Alert(code = "INVALID", codeDescription = "Invalid Alert")

      val person =
        Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = prisonerNumber, deliusCrn = deliusCrn))

      beforeEach {
        Mockito.reset(nomisGateway)
        Mockito.reset(personService)

        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))

        whenever(nomisGateway.getAlertsForPerson(prisonerNumber)).thenReturn(
          Response(
            data =
              listOf(
                alert,
                nonMatchingAlert,
              ),
          ),
        )
      }

      it("gets a person from getPersonService") {
        getAlertsForPersonService.execute(hmppsId)

        verify(personService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("gets alerts from NOMIS using a prisoner number") {
        getAlertsForPersonService.execute(hmppsId)

        verify(nomisGateway, VerificationModeFactory.times(1)).getAlertsForPerson(prisonerNumber)
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
          val response = getAlertsForPersonService.execute(hmppsId)
          response.errors.shouldHaveSize(1)
        }

        it("does not get alerts from Nomis") {
          getAlertsForPersonService.execute(hmppsId)
          verify(nomisGateway, VerificationModeFactory.times(0)).getAlertsForPerson(id = prisonerNumber)
        }
      }

      it("records errors when it cannot find alerts for a person") {
        whenever(nomisGateway.getAlertsForPerson(id = prisonerNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getAlertsForPersonService.execute(hmppsId)
        response.errors.shouldHaveSize(1)
      }

      it("records errors when it cannot find PND alerts for a person") {
        whenever(nomisGateway.getAlertsForPerson(id = prisonerNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getAlertsForPersonService.getAlertsForPnd(hmppsId)
        response.errors.shouldHaveSize(1)
      }

      it("returns PND filtered data") {
        val response = getAlertsForPersonService.getAlertsForPnd(hmppsId)

        response.data.shouldHaveSize(1)
        response.data[0].code shouldBe "XA"
        response.data[0].codeDescription shouldBe "Test Alert XA"
      }

      it("returns an error when the alert code is not in the allowed list") {
        whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(nomisGateway.getAlertsForPerson(prisonerNumber)).thenReturn(
          Response(
            data = listOf(nonMatchingAlert),
          ),
        )

        val response = getAlertsForPersonService.getAlertsForPnd(hmppsId)

        response.errors.shouldHaveSize(1)
        response.data.shouldHaveSize(0)
      }
    },
  )
