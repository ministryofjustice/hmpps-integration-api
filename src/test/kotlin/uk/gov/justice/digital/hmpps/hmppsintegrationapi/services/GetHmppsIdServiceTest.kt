package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetHmppsIdService::class],
)
internal class GetHmppsIdServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getHmppsIdService: GetHmppsIdService,
) : DescribeSpec(
    {
      val id = "A7777ZZ"
      val hmppsId = HmppsId(hmppsId = id)
      val prisonId = "ABC"
      val filters = ConsumerFilters(listOf("ABC"))

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>(prisonId, filters)).thenReturn(
          Response(data = null, errors = emptyList()),
        )

        whenever(getPersonService.identifyHmppsId(id)).thenReturn(
          GetPersonService.IdentifierType.NOMS,
        )

        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = id, identifiers = Identifiers(nomisNumber = id)),
          ),
        )

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(id)).thenReturn(
          Response(
            data = POSPrisoner(firstName = "Test", lastName = "Test", prisonerNumber = id, prisonId = prisonId),
          ),
        )

        whenever(getPersonService.getPersonFromNomis(id)).thenReturn(
          Response(
            data = POSPrisoner(firstName = "Test", lastName = "Test", prisonerNumber = id, prisonId = prisonId),
          ),
        )
      }

      it("Returns a hmpps id for the given id") {
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = hmppsId))
      }

      it("Returns a bad request when not valid nomis number") {
        whenever(getPersonService.identifyHmppsId(id)).thenReturn(
          GetPersonService.IdentifierType.UNKNOWN,
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST))))
      }

      it("Returns a 404 when valid nomis number but no hmppsId is found in either service") {
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )
        whenever(getPersonService.getPersonFromNomis(id)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))))
      }

      it("Returns an error when getPersonService.execute() returns a non 404 error") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = null, errors = errors))
      }

      it("Returns an error when getPersonService.getPersonFromNomis() returns any error") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = Person(firstName = "Qui-gon", lastName = "Jin"),
          ),
        )
        whenever(getPersonService.getPersonFromNomis(id)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = null, errors = errors))
      }

      it("Returns an error when prisonerOffenderSearchGateway.getPrisonOffender() returns any error") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(id)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = null, errors = errors))
      }

      it("Returns a 200 when valid nomis number but no 404 from getPersonService.execute and found in getPersonService.getPersonFromNomis") {
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = hmppsId))
      }

      it("Returns a 200 when valid nomis number but no hmppsid from getPersonService.execute and found in getPersonService.getPersonFromNomis") {
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = Person(firstName = "Qui-gon", lastName = "Jin"),
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = hmppsId))
      }

      it("Returns 404 when person is in an unapproved prison") {
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>(prisonId, filters)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }
    },
  )
