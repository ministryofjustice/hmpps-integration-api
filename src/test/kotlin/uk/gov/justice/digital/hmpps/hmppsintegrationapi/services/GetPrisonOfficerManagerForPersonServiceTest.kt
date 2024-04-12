package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Prison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOfficerManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonOfficerManagerForPersonService::class],
)
class GetPrisonOfficerManagerForPersonServiceTest(
  @MockBean val managePOMCaseGateway: ManagePOMCaseGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getPrisonOfficerManagerForPersonService: GetPrisonOfficerManagerForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val nomisNumber = "Z99999ZZ"
      val person = Person(firstName = "Julianna", lastName = "Blake", identifiers = Identifiers(nomisNumber = nomisNumber))

      val prisonOfficerManager = PrisonOfficerManager(forename = "Paul", surname = "Smith", prison = Prison(code = "RED"))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(managePOMCaseGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(managePOMCaseGateway.getPrimaryPOMForNomisNumber(id = nomisNumber)).thenReturn(Response(prisonOfficerManager))
      }

      it("performs a search according to hmpps Id") {
        getPrisonOfficerManagerForPersonService.execute(hmppsId)
        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("Returns a prison offender manager for person given a hmppsId") {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = person,
          ),
        )
        val result = getPrisonOfficerManagerForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = prisonOfficerManager))
      }

      it("should return a list of errors if person not found") {
        whenever(getPersonService.execute(hmppsId = "NOT_FOUND")).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.MANAGE_POM_CASE,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getPrisonOfficerManagerForPersonService.execute("NOT_FOUND")
        result.data.shouldBe(PrisonOfficerManager())
        result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
