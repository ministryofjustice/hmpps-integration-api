package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.CHECK_NDELIUS_WHEN_NO_CVL_LICENCES
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CreateAndVaryLicenceGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusLicenceCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusLicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusLicenceConditions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusSentenceLicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLicenceConditionService::class],
)
internal class GetLicenceConditionServiceTest(
  @MockitoBean val createAndVaryLicenceGateway: CreateAndVaryLicenceGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  private val getLicenceConditionService: GetLicenceConditionService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val hmppsId = persona.identifiers.deliusCrn!!
      val crn = hmppsId
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)
      val licences = listOf(Licence(id = "12345"))
      val conditions = listOf(LicenceCondition(condition = "MockCondition", category = "AP"))
      val nDeliusLicenceCondition =
        NDeliusLicenceConditions(
          sentences =
            listOf(
              NDeliusSentenceLicenceCondition(
                eventNumber = 123456,
                licenceConditions =
                  listOf(
                    NDeliusLicenceCondition(
                      mainCategory = NDeliusLicenceCategory("AP", description = "Approved Premises"),
                      subCategory = NDeliusLicenceCategory("CODE2", description = "Description 2"),
                    ),
                  ),
              ),
            ),
        )

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(createAndVaryLicenceGateway)
        Mockito.reset(nDeliusGateway)
        Mockito.reset(featureFlagConfig)

        whenever(featureFlagConfig.isEnabled(CHECK_NDELIUS_WHEN_NO_CVL_LICENCES)).thenReturn(false)
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(createAndVaryLicenceGateway.getLicenceSummaries(id = crn)).thenReturn(Response(licences))
        whenever(createAndVaryLicenceGateway.getLicenceConditions(id = 12345)).thenReturn(Response(conditions))
      }

      it("performs a search according to hmpps Id") {
        getLicenceConditionService.execute(hmppsId)
        verify(getPersonService, times(1)).execute(hmppsId = hmppsId)
      }

      it("should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.CVL,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getLicenceConditionService.execute("notfound")
        result.data.licences.shouldBe(emptyList())
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if create and vary licence gateway service returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.CVL,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(createAndVaryLicenceGateway.getLicenceSummaries(id = crn)).thenReturn(
          Response(
            data = emptyList(),
            errors = errors,
          ),
        )

        val result = getLicenceConditionService.execute(hmppsId = hmppsId)
        result.data.licences.shouldBe(emptyList())
        result.errors.shouldBe(errors)
      }

      it("should return licence condition from gateway") {
        val result = getLicenceConditionService.execute(hmppsId = hmppsId)
        result.data.licences
          .first()
          .conditions
          .first()
          .condition
          .shouldBe("MockCondition")
        result.errors.count().shouldBe(0)
      }

      it("should return licence conditions from NDelius when nothing returned from CVL") {
        whenever(featureFlagConfig.isEnabled(CHECK_NDELIUS_WHEN_NO_CVL_LICENCES)).thenReturn(true)
        whenever(createAndVaryLicenceGateway.getLicenceSummaries(id = crn)).thenReturn(Response(emptyList()))
        whenever(nDeliusGateway.getLicenceConditionsForPerson(crn = crn)).thenReturn(Response(data = nDeliusLicenceCondition))
        val result = getLicenceConditionService.execute(hmppsId = hmppsId)
        result.data.licences
          .first()
          .conditions
          .first()
          .condition
          .shouldBe("Description 2")
        result.errors.count().shouldBe(0)
      }

      it("should return errors when NDelius Gateway returns errors") {
        whenever(featureFlagConfig.isEnabled(CHECK_NDELIUS_WHEN_NO_CVL_LICENCES)).thenReturn(true)
        whenever(createAndVaryLicenceGateway.getLicenceSummaries(id = crn)).thenReturn(Response(emptyList()))
        whenever(nDeliusGateway.getLicenceConditionsForPerson(crn = crn)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(type = Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NDELIUS)),
          ),
        )
        val result = getLicenceConditionService.execute(hmppsId = hmppsId)
        result.errors.count().shouldBe(1)
        result.errors shouldContain UpstreamApiError(type = Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NDELIUS)
      }
    },
  )
