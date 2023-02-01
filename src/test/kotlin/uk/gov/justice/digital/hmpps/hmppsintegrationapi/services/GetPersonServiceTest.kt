package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

internal class GetPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway
) : DescribeSpec({
  val id = "abc123"

  beforeEach {
    Mockito.reset(nomisGateway)
    Mockito.reset(prisonerOffenderSearchGateway)
  }

  it("retrieves a person from NOMIS") {
    val getPersonService = GetPersonService(nomisGateway, prisonerOffenderSearchGateway)

    getPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getPerson(id)
  }

  it("retrieves a person from Prisoner Offender Search") {
    val getPersonService = GetPersonService(nomisGateway, prisonerOffenderSearchGateway)

    getPersonService.execute(id)

    verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(id)
  }

  it("returns a person") {
    val getPersonService = GetPersonService(nomisGateway, prisonerOffenderSearchGateway)

    val personFromNomis = Person("Billy", "Bob")
    Mockito.`when`(nomisGateway.getPerson(id)).thenReturn(personFromNomis)

    val person = getPersonService.execute(id)

    person.shouldBe(personFromNomis)
  }
})
