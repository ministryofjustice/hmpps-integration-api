package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetHmppsIdService::class],
)
internal class GetHmppsIdServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  private val getHmppsIdService: GetHmppsIdService,
) : DescribeSpec(
    {
      val id = "A7777ZZ"
      val hmppsId = HmppsId(hmppsId = id)

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.identifyHmppsId(id)).thenReturn(
          GetPersonService.IdentifierType.NOMS,
        )
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = id, identifiers = Identifiers(nomisNumber = id)),
          ),
        )
      }

      it("Returns a hmpps id for the given id") {
        val result = getHmppsIdService.execute(id)

        result.shouldBe(Response(data = hmppsId))
      }

      it("Returns a bad request when not valid nomis number") {
        whenever(getPersonService.identifyHmppsId(id)).thenReturn(
          GetPersonService.IdentifierType.UNKNOWN,
        )
        val result = getHmppsIdService.execute(id)
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
        val result = getHmppsIdService.execute(id)
        result.shouldBe(Response(data = HmppsId(null), errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))))
      }

      it("Returns a 200 when valid nomis number but no 404 from getPersonService.execute and found in getPersonService.getPersonFromNomis") {
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )
        whenever(getPersonService.getPersonFromNomis(id)).thenReturn(
          Response(
            data = POSPrisoner(firstName = "Test", lastName = "Test", prisonerNumber = id),
          ),
        )
        val result = getHmppsIdService.execute(id)
        result.shouldBe(Response(data = hmppsId))
      }

      it("Returns a 200 when valid nomis number but no hmppsid from getPersonService.execute and found in getPersonService.getPersonFromNomis") {
        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = Person(firstName = "Qui-gon", lastName = "Jin"),
          ),
        )
        whenever(getPersonService.getPersonFromNomis(id)).thenReturn(
          Response(
            data = POSPrisoner(firstName = "Test", lastName = "Test", prisonerNumber = id),
          ),
        )
        val result = getHmppsIdService.execute(id)
        result.shouldBe(Response(data = hmppsId))
      }
    },
  )
