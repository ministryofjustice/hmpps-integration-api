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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HealthAndMedicationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMCateringInstructions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMDietAndAllergy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMFoodAllergies
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMFoodAllergy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMHealthAndMedication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMMedicalDietaryRequirement
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMMedicalDietaryRequirements
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMPersonalisedDietaryRequirement
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMPersonalisedDietaryRequirements
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMReferenceDataValue
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CateringInstruction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Diet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HealthAndDiet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetHealthAndDietService::class],
)
internal class GetHealthAndDietServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val healthAndMedicationGateway: HealthAndMedicationGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getHealthAndDietService: GetHealthAndDietService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val nomsId = "A1234BB"
      val filters = RoleFilters(prisons = null)
      val prisoner = POSPrisoner(firstName = "Jim", lastName = "Brown", smoker = "Y", dateOfBirth = LocalDate.of(1992, 12, 3), prisonerNumber = nomsId, youthOffender = false)
      val healthAndMedicationResponse =
        HAMHealthAndMedication(
          dietAndAllergy =
            HAMDietAndAllergy(
              foodAllergies =
                HAMFoodAllergies(
                  value =
                    listOf(
                      HAMFoodAllergy(
                        value =
                          HAMReferenceDataValue(
                            id = "id",
                            code = "code",
                            description = "description",
                          ),
                        comment = "",
                      ),
                    ),
                  lastModifiedAt = "",
                  lastModifiedBy = "",
                  lastModifiedPrisonId = "MDI",
                ),
              medicalDietaryRequirements =
                HAMMedicalDietaryRequirements(
                  value =
                    listOf(
                      HAMMedicalDietaryRequirement(
                        value =
                          HAMReferenceDataValue(
                            id = "id",
                            code = "code",
                            description = "description",
                          ),
                        comment = "",
                      ),
                    ),
                  lastModifiedAt = "",
                  lastModifiedBy = "",
                  lastModifiedPrisonId = "MDI",
                ),
              personalisedDietaryRequirements =
                HAMPersonalisedDietaryRequirements(
                  value =
                    listOf(
                      HAMPersonalisedDietaryRequirement(
                        value =
                          HAMReferenceDataValue(
                            id = "id",
                            code = "code",
                            description = "description",
                          ),
                        comment = "",
                      ),
                    ),
                  lastModifiedAt = "",
                  lastModifiedBy = "",
                  lastModifiedPrisonId = "MDI",
                ),
              cateringInstructions =
                HAMCateringInstructions(
                  value = "catering-instructions",
                  lastModifiedAt = "",
                  lastModifiedBy = "",
                  lastModifiedPrisonId = "MDI",
                ),
            ),
        )

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonerOffenderSearchGateway)
        Mockito.reset(healthAndMedicationGateway)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(
          Response(
            data = NomisNumber(nomsId),
          ),
        )

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsId)).thenReturn(Response(data = prisoner))
        whenever(healthAndMedicationGateway.getHealthAndMedicationData(nomsId)).thenReturn(Response(data = healthAndMedicationResponse))
      }

      it("gets a person from getPersonService") {
        getHealthAndDietService.execute(hmppsId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumberWithPrisonFilter(hmppsId, filters)
      }

      it("calls health and medication gateway") {
        val response = getHealthAndDietService.execute(hmppsId, filters)
        verify(healthAndMedicationGateway, VerificationModeFactory.times(1)).getHealthAndMedicationData(nomsId)
      }

      it("calls prisoner search gateway") {
        val response = getHealthAndDietService.execute(hmppsId, filters)
        verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPrisonOffender(nomsNumber = nomsId)
      }

      it("returns health and diet information") {
        val healthAndDietResponse = HealthAndDiet(diet = healthAndMedicationResponse.dietAndAllergy.toDiet(), smoking = prisoner.smoker)
        val response = getHealthAndDietService.execute(hmppsId, filters)
        response.data.shouldBe(healthAndDietResponse)
      }

      it("returns an upstream 404 error when prisoner not found") {
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsId)).thenReturn(
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

        val response = getHealthAndDietService.execute(hmppsId, filters)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISONER_OFFENDER_SEARCH)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns data when health and medication gateway returns a 404, but health data will be empty") {
        whenever(healthAndMedicationGateway.getHealthAndMedicationData(nomsId)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.HEALTH_AND_MEDICATION,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getHealthAndDietService.execute(hmppsId, filters)
        response.errors.shouldHaveSize(0)
        response.data.shouldBe(
          HealthAndDiet(
            diet =
              Diet(
                foodAllergies = emptyList(),
                medicalDietaryRequirements = emptyList(),
                personalisedDietaryRequirements = emptyList(),
                cateringInstructions = CateringInstruction(value = null),
              ),
            smoking = prisoner.smoker,
          ),
        )
      }
    },
  )
