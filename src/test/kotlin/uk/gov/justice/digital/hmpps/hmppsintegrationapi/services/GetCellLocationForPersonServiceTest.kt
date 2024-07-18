package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CellLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCellLocationForPersonService::class],
)
internal class GetCellLocationForPersonServiceTest(
  @MockBean val getPersonService: GetPersonService,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getCellLocationForPersonService: GetCellLocationForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "0000/11111A"
      val nomisNumber = "A1234AA"

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.execute(hmppsId)).thenReturn(
          Response(data = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = nomisNumber))),
        )

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(
          Response(data = POSPrisoner(firstName = "Qui-gon", lastName = "Jin", inOutStatus = "IN", prisonId = "MDI", prisonName = "Moorland (HMP & YOI)", cellLocation = "6-2-006")),
        )
      }

      it("get cell location for person with hmpps Id") {
        getCellLocationForPersonService.execute(hmppsId)

        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns a person cell location") {
        val response = getCellLocationForPersonService.execute(hmppsId)

        response.data.shouldBe(CellLocation(cell = "6-2-006", prisonCode = "MDI", prisonName = "Moorland (HMP & YOI)"))
      }

      it("returns the upstream error when an error occurs") {
        whenever(getPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getCellLocationForPersonService.execute(hmppsId)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.PROBATION_OFFENDER_SEARCH)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
