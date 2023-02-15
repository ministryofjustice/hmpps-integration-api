package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class]
)
internal class GetImageMetadataForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  private val getPersonService: GetPersonService
) : DescribeSpec({
  val id = "abc123"

  beforeEach {
    Mockito.reset(nomisGateway)
  }

  it("retrieves images details from NOMIS") {
    getPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getPerson(id)
  }
})
