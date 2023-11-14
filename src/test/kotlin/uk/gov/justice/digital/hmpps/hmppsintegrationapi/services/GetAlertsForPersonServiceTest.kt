package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAlertsForPersonService::class],
)
internal class GetAlertsForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getAlertsForPersonService: GetAlertsForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"
    val alert = Alert()

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(prisonerOffenderSearchGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = hmppsId)).thenReturn(
        Response(data = listOf(Person(firstName = "Chandler", lastName = "Bing", identifiers = Identifiers(nomisNumber = prisonerNumber)))),
      )

      whenever(nomisGateway.getAlertsForPerson(prisonerNumber)).thenReturn(
        Response(
          data = listOf(
            alert,
          ),
        ),
      )
    }

    it("retrieves prisoner ID from Prisoner Offender Search using a Hmpps Id") {
      getAlertsForPersonService.execute(hmppsId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = hmppsId)
    }

    it("retrieves alerts from NOMIS using a prisoner number") {
      getAlertsForPersonService.execute(hmppsId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getAlertsForPerson(prisonerNumber)
    }

    describe("when an upstream API returns an error when looking up a person by a Hmmps Id") {
      beforeEach {
        whenever(prisonerOffenderSearchGateway.getPersons(pncId = hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
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
          errors = listOf(
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
  },
)
