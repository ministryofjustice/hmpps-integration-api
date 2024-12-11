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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CreateAndVaryLicenceGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

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
      val hmppsId = "1234/56789B"
      val crn = "Z99999ZZ"
      val person = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(deliusCrn = crn))
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
        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("should return a list of errors if person not found") {
        whenever(getPersonService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.CVL,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getLicenceConditionService.execute("notfound")
        result.data.licences.shouldBe(emptyList())
        result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("should return a list of errors if create and vary licence gateway service returns error") {
        whenever(createAndVaryLicenceGateway.getLicenceSummaries(id = crn)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.CVL,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getLicenceConditionService.execute(hmppsId = hmppsId)
        result.data.licences.shouldBe(emptyList())
        result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("should return licence condition from gateway") {
        val result = getLicenceConditionService.execute(hmppsId = hmppsId)
        result.data.licences.first().conditions.first().condition.shouldBe("MockCondition")
        result.errors.count().shouldBe(0)
      }
    },
  )
