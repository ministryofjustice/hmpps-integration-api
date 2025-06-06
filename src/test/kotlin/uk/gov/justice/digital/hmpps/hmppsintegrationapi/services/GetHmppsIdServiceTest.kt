package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetHmppsIdService::class],
)
internal class GetHmppsIdServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val deliusGateway: NDeliusGateway,
  private val getHmppsIdService: GetHmppsIdService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val nomisNumber = persona.identifiers.nomisNumber!!
      val hmppsId = nomisNumber
      val filters = ConsumerFilters(listOf("ABC"))

      beforeEach {
        Mockito.reset(getPersonService)
      }

      it("Returns a hmpps id for the given id") {
        val person = Person(firstName = persona.firstName, lastName = persona.lastName, hmppsId = hmppsId)
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = person,
          ),
        )

        val result = getHmppsIdService.execute(nomisNumber, filters)
        result.shouldBe(Response(data = HmppsId(hmppsId)))
      }

      it("Returns a hmpps id when hmpps id is null but a nomis number is present") {
        val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = Identifiers(nomisNumber = nomisNumber))
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = person,
          ),
        )

        val result = getHmppsIdService.execute(nomisNumber, filters)
        result.shouldBe(Response(data = HmppsId(nomisNumber)))
      }

      it("Returns an error if getPersonWithPrisonFilter() returns an error") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "getPersonWithPrisonFilter error"))
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getHmppsIdService.execute(nomisNumber, filters)
        result.shouldBe(Response(data = null, errors = errors))
      }

      it("Returns an 404 if getPersonWithPrisonFilter() returns a person with no hmppsId or identifiers") {
        val personWithNoHmppsIdOrIdentifiers = Person(firstName = persona.firstName, lastName = persona.lastName, hmppsId = null)
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = personWithNoHmppsIdOrIdentifiers,
          ),
        )

        val result = getHmppsIdService.execute(nomisNumber, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }
    },
  )
