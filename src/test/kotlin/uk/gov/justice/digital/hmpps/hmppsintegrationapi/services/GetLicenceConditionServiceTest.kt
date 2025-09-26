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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CreateAndVaryLicenceGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLicenceConditionService::class],
)
internal class GetLicenceConditionServiceTest(
  @MockitoBean val createAndVaryLicenceGateway: CreateAndVaryLicenceGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getLicenceConditionService: GetLicenceConditionService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val hmppsId = persona.identifiers.deliusCrn!!
      val crn = hmppsId
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)
      val licences = listOf(Licence(id = "12345"))
      val conditions = listOf(LicenceCondition(condition = "MockCondition", category = "AP"))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(createAndVaryLicenceGateway)

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
    },
  )
