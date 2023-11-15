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
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec(
  {
    val pncId = "2003/13116M"
    val prisonerNumber = "A5553AA"
    val addressesFromProbationOffenderSearch = listOf(generateTestAddress())

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(nomisGateway)
      Mockito.reset(getPersonService)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(data = listOf(Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = prisonerNumber)))),
      )

      whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(Response(data = emptyList()))

      whenever(getPersonService.getAddressesForPerson(hmppsId = pncId)).thenReturn(
        Response(
          data = addressesFromProbationOffenderSearch,
        ),
      )
    }

    it("retrieves prisoner ID from Prisoner Offender Search") {
      getAddressesForPersonService.execute(pncId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
    }

    it("retrieves addresses for a person from Person Service using PND ID") {
      getAddressesForPersonService.execute(pncId)

      verify(getPersonService, VerificationModeFactory.times(1)).getAddressesForPerson(pncId)
    }

    it("retrieves addresses for a person from NOMIS using prisoner number") {
      getAddressesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getAddressesForPerson(prisonerNumber)
    }

    it("returns all addresses for a person") {
      val addressesFromProbationOffenderSearch = generateTestAddress(name = "Probation Address")
      val addressesFromNomis = generateTestAddress(name = "NOMIS Address")

      whenever(getPersonService.getAddressesForPerson(pncId)).thenReturn(
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
      whenever(getPersonService.getAddressesForPerson(pncId)).thenReturn(
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
