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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAdjudicationsForPersonService::class],
)
internal class GetAdjudicationsForPersonServiceTest(
  @MockitoBean val adjudicationsGateway: AdjudicationsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getAdjudicationsForPersonService: GetAdjudicationsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val prisonerNumber = "Z99999ZZ"
      val person = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = prisonerNumber))
      val adjudications = listOf(Adjudication(incidentDetails = IncidentDetailsDto(dateTimeOfIncident = "MockDate")))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(adjudicationsGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(adjudicationsGateway.getReportedAdjudicationsForPerson(id = prisonerNumber)).thenReturn(Response(adjudications))
      }

      it("performs a search according to hmpps Id") {
        getAdjudicationsForPersonService.execute(hmppsId)
        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("should return a list of errors if person not found") {
        whenever(getPersonService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.ADJUDICATIONS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getAdjudicationsForPersonService.execute("notfound")
        result.data.shouldBe(emptyList())
        result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("should return a list of errors if adjudication gateway service returns error") {
        whenever(adjudicationsGateway.getReportedAdjudicationsForPerson(id = prisonerNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.ADJUDICATIONS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getAdjudicationsForPersonService.execute(hmppsId = hmppsId)
        result.data.shouldBe(emptyList())
        result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("should return adjudications from gateway") {
        val result = getAdjudicationsForPersonService.execute(hmppsId = hmppsId)
        result.data.first().incidentDetails?.dateTimeOfIncident.shouldBe("MockDate")
        result.errors.count().shouldBe(0)
      }
    },
  )
