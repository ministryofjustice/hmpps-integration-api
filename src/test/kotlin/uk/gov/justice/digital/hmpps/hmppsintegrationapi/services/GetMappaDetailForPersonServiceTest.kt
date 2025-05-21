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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationOnlyPersona

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
      val personFromProbationOffenderSearch =
        Person(
          firstName = personInProbationOnlyPersona.firstName,
          lastName = personInProbationOnlyPersona.lastName,
          identifiers = personInProbationOnlyPersona.identifiers,
        )

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

      val deliusCrn = personFromProbationOffenderSearch.identifiers.deliusCrn!!
      val hmppsId = deliusCrn

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
        verify(getPersonService, times(1)).execute(hmppsId = hmppsId)
      }

      it("returns a 404 when Mappa Detail is null") {
        whenever(nDeliusGateway.getMappaDetailForPerson(id = deliusCrn)).thenReturn(
          Response(
            data =
              MappaDetail(
                level = null,
                levelDescription = null,
                category = null,
                categoryDescription = null,
                startDate = null,
                reviewDate = null,
                notes = null,
              ),
          ),
        )

        val result = getMappaDetailForPersonService.execute(hmppsId)
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        )
      }

      it("returns an error when getPersonService returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val response = getMappaDetailForPersonService.execute(hmppsId)
        verify(nDeliusGateway, times(0)).getOffencesForPerson(id = deliusCrn)
        response.errors.shouldBe(errors)
      }
    },
  )
