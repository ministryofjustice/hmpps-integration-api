package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
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

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAddressesForPersonService::class],
)
internal class GetAddressesForPersonServiceTest(
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val nomisGateway: NomisGateway,
  private val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec({
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
    val addressesFromProbationOffenderSearch = Address(postcode = "SE1 1TE")
    val addressesFromNomis = Address(postcode = "BS1 6PU")

    whenever(probationOffenderSearchGateway.getAddressesForPerson(pncId)).thenReturn(
      listOf(
        addressesFromProbationOffenderSearch,
      ),
    )
    whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(listOf(addressesFromNomis))

    val result = getAddressesForPersonService.execute(pncId)

    result?.shouldContain(addressesFromProbationOffenderSearch)
    result?.shouldContain(addressesFromNomis)
  }

  it("returns null when person cannot be found in all upstream APIs") {
    whenever(probationOffenderSearchGateway.getAddressesForPerson(pncId)).thenReturn(null)
    whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(null)

    val result = getAddressesForPersonService.execute(pncId)

    result.shouldBeNull()
  }
},)
