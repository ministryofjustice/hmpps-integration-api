package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway

internal class GetPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway
) : DescribeSpec({
  it("retrieves a person from NOMIS"){
    val id = 1
    val getPersonService = GetPersonService(nomisGateway)

    getPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getPerson(id)
  }
})