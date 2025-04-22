package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSBodyMark
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisonerAlias
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPhysicalCharacteristicsForPersonService::class],
)
internal class GetPhysicalCharacteristicsForPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getPhysicalCharacteristicsForPersonService: GetPhysicalCharacteristicsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val nomisNumber = NomisNumber(hmppsId)
      val filters = null

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
              POSBodyMark(bodyPart = "Left Arm", comment = "Tribal band"),
              POSBodyMark(bodyPart = "Chest", comment = "Dragon"),
            ),
          scars =
            listOf(
              POSBodyMark(bodyPart = "Right Knee", comment = "Long, thin scar"),
            ),
          marks =
            listOf(
              POSBodyMark(bodyPart = "Left Cheek", comment = "Small mole"),
            ),
          youthOffender = false,
        )
      val physicalCharacteristics = posPrisoner.toPhysicalCharacteristics()

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonerOffenderSearchGateway)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(nomisNumber))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(Response(posPrisoner))
      }

      it("performs a search according to hmpps Id") {
        getPhysicalCharacteristicsForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)
      }

      it("should return physical charactertistics from gateway") {
        val result = getPhysicalCharacteristicsForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(physicalCharacteristics)
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = "notfound", filters = filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getPhysicalCharacteristicsForPersonService.execute(hmppsId = "notfound", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if a bad request is made to getPersonService") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.BAD_REQUEST,
            ),
          )
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = "badRequest", filters = filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getPhysicalCharacteristicsForPersonService.execute(hmppsId = "badRequest", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if personal relationships gateway returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getPhysicalCharacteristicsForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
