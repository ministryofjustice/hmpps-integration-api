package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetImageMetadataForPersonService::class],
)
internal class GetImageMetadataForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getImageMetadataForPersonService: GetImageMetadataForPersonService,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val prisonerNumber = "abc123"
    val filters = null

    beforeEach {
      Mockito.reset(nomisGateway)
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(
        Response(
          data = NomisNumber(prisonerNumber),
        ),
      )
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
        Response(data = PersonOnProbation(Person(firstName = "Joey", lastName = "Tribbiani", identifiers = Identifiers(nomisNumber = prisonerNumber)), false)),
      )
      whenever(nomisGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(Response(data = emptyList()))
    }

    it("gets prisoner ID from Person service") {
      getImageMetadataForPersonService.execute(hmppsId, filters)

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumberWithPrisonFilter(hmppsId, filters)
    }

    it("gets images details from NOMIS") {
      getImageMetadataForPersonService.execute(hmppsId, filters)

      verify(nomisGateway, VerificationModeFactory.times(1)).getImageMetadataForPerson(prisonerNumber)
    }

    it("returns metadata for a persons images") {
      val imageMetadataFromNomis =
        listOf(
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

      val response = getImageMetadataForPersonService.execute(hmppsId, filters)

      response.data.shouldBe(imageMetadataFromNomis)
    }

    it("returns a not found error when person cannot be found in Person service") {
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )

      val response = getImageMetadataForPersonService.execute(hmppsId, filters)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.NOMIS)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }

    it("returns the error from NOMIS when an error occurs") {
      whenever(nomisGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(
        Response(
          data = emptyList(),
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )

      val response = getImageMetadataForPersonService.execute(hmppsId, filters)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.NOMIS)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  })
