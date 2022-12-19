package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

internal class GetPersonServiceTest(@MockBean val nomisGateway: NomisGateway) : DescribeSpec({
  val id = "abc123"

  it("retrieves a person from NOMIS") {
    val getPersonService = GetPersonService(nomisGateway)

    getPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getPerson(id)
  }

  it("returns a person") {
    val getPersonService = GetPersonService(nomisGateway)

    val personFromNomis = Person("Billy", "Bob")
    Mockito.`when`(nomisGateway.getPerson(id)).thenReturn(personFromNomis)

    val person = getPersonService.execute(id)

    person.shouldBe(personFromNomis)
  }
})
