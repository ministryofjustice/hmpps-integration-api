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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetMappaDetailForPersonService::class],
)
internal class GetMappaDetailForPersonServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  private val getMappaDetailForPersonService: GetMappaDetailForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val deliusCrn = "X123456"

      val personFromProbationOffenderSearch =
        Person(firstName = "Phoebe", lastName = "Buffay", identifiers = Identifiers(deliusCrn = deliusCrn))

      val mappaDetailForPerson =
        MappaDetail(
          level = 1,
          levelDescription = "string",
          category = 1,
          categoryDescription = "string",
          startDate = "string",
          reviewDate = "string",
          notes = "string",
        )

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(nDeliusGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        whenever(nDeliusGateway.getMappaDetailForPerson(deliusCrn)).thenReturn(
          Response(
            data = mappaDetailForPerson,
          ),
        )
      }

      it("Returns mappa detail for person given a hmppsId") {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        val result = getMappaDetailForPersonService.execute(hmppsId)

        result.shouldBe(Response(data = mappaDetailForPerson))
      }

      it("gets a person using a Hmpps ID") {
        getMappaDetailForPersonService.execute(hmppsId)

        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      describe("when an upstream API returns an error when looking up a person from a Hmpps ID") {
        beforeEach {
          whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
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
        }

        it("does not get mappa detail from delius") {
          getMappaDetailForPersonService.execute(hmppsId)
          verify(nDeliusGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = deliusCrn)
        }
      }
    },
  )
