package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAddressesForPersonService::class],
)
internal class GetAddressesForPersonServiceTest(
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val personService: GetPersonService,
  private val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "2003/13116M"
    val prisonerNumber = "A5553AA"
    val deliusCrn = "X777776"

    val person = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = prisonerNumber, deliusCrn = deliusCrn))

    beforeEach {
      Mockito.reset(probationOffenderSearchGateway)
      Mockito.reset(nomisGateway)
      Mockito.reset(personService)

      whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
      whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))

      whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = emptyList()))
      whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(Response(data = emptyList()))
    }

    it("retrieves a person from getPersonService") {
      getAddressesForPersonService.execute(hmppsId)

      verify(personService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
    }

    it("retrieves addresses for a person from Probation Offender Search using a Hmpps Id") {
      getAddressesForPersonService.execute(hmppsId = hmppsId)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getAddressesForPerson(hmppsId)
    }

    it("retrieves addresses for a person from Probation Offender Search using a Delius CRN") {
      getAddressesForPersonService.execute(hmppsId = deliusCrn)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getAddressesForPerson(deliusCrn)
    }

    it("retrieves addresses for a person from NOMIS using prisoner number") {
      getAddressesForPersonService.execute(hmppsId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getAddressesForPerson(prisonerNumber)
    }

    it("returns all addresses for a person") {
      val addressesFromProbationOffenderSearch = generateTestAddress(name = "Probation Address")
      val addressesFromNomis = generateTestAddress(name = "NOMIS Address")

      whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(
        Response(data = listOf(addressesFromProbationOffenderSearch)),
      )
      whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(
        Response(data = listOf(addressesFromNomis)),
      )

      val response = getAddressesForPersonService.execute(hmppsId)

      response.data.shouldContain(addressesFromProbationOffenderSearch)
      response.data.shouldContain(addressesFromNomis)
    }

    it("returns all errors when person cannot be found in all upstream APIs") {
      whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )
      whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(
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

      val response = getAddressesForPersonService.execute(hmppsId)

      response.errors.shouldHaveSize(2)
    }
  },
)
