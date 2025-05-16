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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPersonalCareNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisonerAlias
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCareNeedsForPersonService::class],
)
internal class GetCareNeedsForPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getCareNeedsForPersonService: GetCareNeedsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val prisonerNumber = "Z99999ZZ"
      val person = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = prisonerNumber))
      val filters = null
      val careNeedsGatewayResponse =
        POSPrisoner(
          firstName = "First Name",
          lastName = "Last Name",
          middleNames = "Middle Name",
          dateOfBirth = LocalDate.parse("2023-03-01"),
          gender = "Gender",
          ethnicity = "Ethnicity",
          prisonerNumber = "prisonerNumber",
          pncNumber = "pncNumber",
          croNumber = "croNumber",
          aliases =
            listOf(
              POSPrisonerAlias(firstName = "Alias First Name", lastName = "Alias Last Name"),
            ),
          youthOffender = false,
          personalCareNeeds =
            listOf(
              POSPersonalCareNeed(
                problemType = "MATSTAT",
                problemCode = "ACCU9",
                problemStatus = "ON",
                problemDescription = "No Disability",
                commentText = "COMMENT",
                startDate = "2020-06-21",
                endDate = null,
              ),
            ),
        )
      val careNeeds = careNeedsGatewayResponse.toPersonalCareNeeds()

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonerOffenderSearchGateway)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(person))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(prisonerNumber)).thenReturn(Response(careNeedsGatewayResponse))
      }

      it("performs a search according to hmpps Id") {
        getCareNeedsForPersonService.execute(hmppsId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
      }

      it("should return a person's care needs from gateway") {
        val result = getCareNeedsForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(careNeeds)
        result.errors.count().shouldBe(0)
      }

      it("should return an entity not found error if person found in person service but no nomis number set for them") {
        val personWithoutNomis = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = null))
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(
          Response(data = personWithoutNomis),
        )
        val result = getCareNeedsForPersonService.execute(hmppsId = hmppsId, filters)
        result.data.shouldBe(null)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
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
        val result = getCareNeedsForPersonService.execute(hmppsId = "notfound", filters)
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
        val result = getCareNeedsForPersonService.execute(hmppsId = "badRequest", filters)
        result.data.shouldBe(null)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
      }

      it("should return a list of errors if prisoner offender search gateway returns error") {
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(prisonerNumber)).thenReturn(
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
        val result = getCareNeedsForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
