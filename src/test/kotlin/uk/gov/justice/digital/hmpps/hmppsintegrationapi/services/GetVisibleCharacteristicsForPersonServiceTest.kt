package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisibleCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.BodyMark
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisonerAlias
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisibleCharacteristicsForPersonService::class],
)
internal class GetVisibleCharacteristicsForPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getVisibleCharacteristicsForPersonService: GetVisibleCharacteristicsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val person = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = hmppsId))
      val filters = null
      val visibleCharacteristics =
        VisibleCharacteristics(
          heightCentimetres = 180,
          weightKilograms = 85,
          hairColour = "Brown",
          rightEyeColour = "Blue",
          leftEyeColour = "Blue",
          facialHair = "Beard",
          shapeOfFace = "Oval",
          build = "Muscular",
          shoeSize = 10,
          tattoos =
            listOf(
              BodyMark(bodyPart = "Left Arm", comment = "Tribal band"),
              BodyMark(bodyPart = "Chest", comment = "Dragon"),
            ),
          scars =
            listOf(
              BodyMark(bodyPart = "Right Knee", comment = "Long, thin scar"),
            ),
          marks =
            listOf(
              BodyMark(bodyPart = "Left Cheek", comment = "Small mole"),
            ),
        )

      val posPrisoner =
        POSPrisoner(
          firstName = "Obi-Wan",
          lastName = "Kenobi",
          middleNames = "Ben",
          dateOfBirth = LocalDate.parse("1975-02-28"),
          gender = "Male",
          ethnicity = "White",
          aliases =
            listOf(
              POSPrisonerAlias(firstName = "Ben", lastName = "Kenobi"),
              POSPrisonerAlias(firstName = "Obi", lastName = "Wan"),
            ),
          prisonerNumber = "A1234BC",
          pncNumber = "2003/11985X",
          bookingId = "123456",
          maritalStatus = "Married",
          croNumber = "03/11985X",
          prisonId = "MDI",
          prisonName = "Moorland (HMP & YOI)",
          cellLocation = "A-1-001",
          inOutStatus = "IN",
          category = "C",
          csra = "High",
          receptionDate = "2023-01-01",
          status = "ACTIVE IN",
          heightCentimetres = 180,
          weightKilograms = 85,
          hairColour = "Brown",
          rightEyeColour = "Blue",
          leftEyeColour = "Blue",
          facialHair = "Beard",
          shapeOfFace = "Oval",
          build = "Muscular",
          shoeSize = 10,
          tattoos =
            listOf(
              BodyMark(bodyPart = "Left Arm", comment = "Tribal band"),
              BodyMark(bodyPart = "Chest", comment = "Dragon"),
            ),
          scars =
            listOf(
              BodyMark(bodyPart = "Right Knee", comment = "Long, thin scar"),
            ),
          marks =
            listOf(
              BodyMark(bodyPart = "Left Cheek", comment = "Small mole"),
            ),
        )

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonerOffenderSearchGateway)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(person))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(Response(posPrisoner))
      }

      it("performs a search according to hmpps Id") {
        getVisibleCharacteristicsForPersonService.execute(hmppsId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
      }

      it("should return visible charactertistics from gateway") {
        val result = getVisibleCharacteristicsForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(visibleCharacteristics)
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if person not found") {
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "notfound", filters = filters)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getVisibleCharacteristicsForPersonService.execute(hmppsId = "notfound", filters)
        result.data.shouldBe(null)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("should return a list of errors if a bad request is made to getPersonService") {
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "badRequest", filters = filters)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.BAD_REQUEST,
                ),
              ),
          ),
        )
        val result = getVisibleCharacteristicsForPersonService.execute(hmppsId = "badRequest", filters)
        result.data.shouldBe(null)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
      }

      it("should return a list of errors if personal relationships gateway returns error") {
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getVisibleCharacteristicsForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
