package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

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
      val filters = ConsumerFilters(listOf("ABC"))

      beforeEach {
        Mockito.reset(getPersonService)
      }

      it("Returns a hmpps id for the given id") {
        val person = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = id)
        whenever(getPersonService.getPersonWithPrisonFilter(id, filters)).thenReturn(
          Response(
            data = person,
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = hmppsId))
      }

      it("Returns an error if getPersonWithPrisonFilter() returns an error") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "getPersonWithPrisonFilter error"))
        whenever(getPersonService.getPersonWithPrisonFilter(id, filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.shouldBe(Response(data = null, errors = errors))
      }

      it("Returns an 404 if getPersonWithPrisonFilter() returns a person with no hmppsId") {
        val personWithoutPrisonId = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = null)
        whenever(getPersonService.getPersonWithPrisonFilter(id, filters)).thenReturn(
          Response(
            data = personWithoutPrisonId,
          ),
        )
        val result = getHmppsIdService.execute(id, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }
    },
  )
