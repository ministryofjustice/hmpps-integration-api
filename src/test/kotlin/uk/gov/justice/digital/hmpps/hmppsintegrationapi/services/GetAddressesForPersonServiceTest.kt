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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
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
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val nomisGateway: NomisGateway,
  private val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec(
  {
    val pncId = "2003/13116M"
    val prisonerNumber = "A5553AA"

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(probationOffenderSearchGateway)
      Mockito.reset(nomisGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        listOf(
          Person(
            firstName = "Qui-gon",
            lastName = "Jin",
            prisonerId = prisonerNumber,
          ),
        ),
      )
      whenever(probationOffenderSearchGateway.getAddressesForPerson(pncId)).thenReturn(Response(data = emptyList()))
      whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(Response(data = emptyList()))
    }

    it("retrieves prisoner ID from Prisoner Offender Search") {
      getAddressesForPersonService.execute(pncId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
    }

    it("retrieves addresses for a person from Probation Offender Search using PND ID") {
      getAddressesForPersonService.execute(pncId)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getAddressesForPerson(pncId)
    }

    it("retrieves addresses for a person from NOMIS using prisoner number") {
      getAddressesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getAddressesForPerson(prisonerNumber)
    }

    it("returns all addresses for a person") {
      val addressesFromProbationOffenderSearch = Address(
        country = "England",
        county = "Berkshire",
        endDate = "2023-01-01",
        locality = "Surrey",
        name = "Some building name",
        number = "90",
        postcode = "SE1 1TE",
        startDate = "2022-01-01",
        street = "O'meara street",
        town = "London Town",
        type = "Type?",
      )

      val addressesFromNomis = Address(
        postcode = "BS1 6PU",
        country = "England",
        county = "Berkshire",
        endDate = "2023-01-01",
        locality = "Surrey",
        name = "Some building name",
        number = "90",
        startDate = "2022-01-01",
        street = "O'meara street",
        town = "London Town",
        type = "Type?",
      )

      whenever(probationOffenderSearchGateway.getAddressesForPerson(pncId)).thenReturn(
        Response(data = listOf(addressesFromProbationOffenderSearch)),
      )
      whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(
        Response(data = listOf(addressesFromNomis)),
      )

      val response = getAddressesForPersonService.execute(pncId)

      response.data.shouldContain(addressesFromProbationOffenderSearch)
      response.data.shouldContain(addressesFromNomis)
    }

    it("returns all errors when person cannot be found in all upstream APIs") {
      whenever(probationOffenderSearchGateway.getAddressesForPerson(pncId)).thenReturn(
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

      val response = getAddressesForPersonService.execute(pncId)

      response.errors.shouldHaveSize(2)
    }
  },
)
