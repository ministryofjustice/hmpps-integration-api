package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetImageMetadataForPersonService::class],
)
internal class GetImageMetadataForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getImageMetadataForPersonService: GetImageMetadataForPersonService,
) : DescribeSpec({
  val pncId = "2003/13116M"
  val prisonerNumber = "abc123"

  beforeEach {
    Mockito.reset(nomisGateway)

    whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
      Response(data = listOf(Person(firstName = "Joey", lastName = "Tribbiani", identifiers = Identifiers(nomisNumber = prisonerNumber)))),
    )
    whenever(nomisGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(Response(data = emptyList()))
  }

  it("retrieves prisoner ID from Prisoner Offender Search") {
    getImageMetadataForPersonService.execute(pncId)

    verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
  }

  it("retrieves images details from NOMIS") {
    getImageMetadataForPersonService.execute(pncId)

    verify(nomisGateway, VerificationModeFactory.times(1)).getImageMetadataForPerson(prisonerNumber)
  }

  it("returns metadata for a persons images") {
    val imageMetadataFromNomis = listOf(
      ImageMetadata(
        id = 2461788,
        active = false,
        captureDateTime = LocalDateTime.parse("2023-03-01T08:30:45"),
        view = "FACE",
        orientation = "FRONT",
        type = "OFF_BKG",
      ),
    )
    whenever(nomisGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(Response(data = imageMetadataFromNomis))

    val response = getImageMetadataForPersonService.execute(pncId)

    response.data.shouldBe(imageMetadataFromNomis)
  }

  it("returns the error from NOMIS when an error occurs") {
    whenever(nomisGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(
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

    val response = getImageMetadataForPersonService.execute(pncId)

    response.errors.shouldHaveSize(1)
    response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
    response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
  }
},)
