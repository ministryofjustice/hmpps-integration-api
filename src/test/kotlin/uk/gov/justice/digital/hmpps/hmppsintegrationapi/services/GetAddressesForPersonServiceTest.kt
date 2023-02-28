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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAddressesForPersonService::class]
)
internal class GetAddressesForPersonServiceTest(
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockBean val nomisGateway: NomisGateway,
  private val getAddressesForPersonService: GetAddressesForPersonService
) : DescribeSpec({
  val id = "abc123"

  beforeEach {
    Mockito.reset(probationOffenderSearchGateway)
    Mockito.reset(nomisGateway)
  }

  it("retrieves addresses for a person from Probation Offender Search") {
    getAddressesForPersonService.execute(id)

    verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getAddressesForPerson(id)
  }

  it("retrieves addresses for a person from NOMIS") {
    getAddressesForPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getAddressesForPerson(id)
  }

  it("returns all addresses for a person") {
    val addressesFromProbationOffenderSearch = Address(postcode = "SE1 1TE")
    val addressesFromNomis = Address(postcode = "BS1 6PU")
    whenever(probationOffenderSearchGateway.getAddressesForPerson(id)).thenReturn(
      listOf(
        addressesFromProbationOffenderSearch
      )
    )
    whenever(nomisGateway.getAddressesForPerson(id)).thenReturn(listOf(addressesFromNomis))

    val result = getAddressesForPersonService.execute(id)

    result?.shouldContain(addressesFromProbationOffenderSearch)
    result?.shouldContain(addressesFromNomis)
  }

  it("returns null when person cannot be found in all upstream APIs") {
    whenever(probationOffenderSearchGateway.getAddressesForPerson(id)).thenReturn(null)
    whenever(nomisGateway.getAddressesForPerson(id)).thenReturn(null)

    val result = getAddressesForPersonService.execute(id)

    result.shouldBeNull()
  }
})
