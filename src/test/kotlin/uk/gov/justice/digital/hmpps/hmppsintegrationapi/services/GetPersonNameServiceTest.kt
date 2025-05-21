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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetNameForPersonService::class],
)
internal class GetPersonNameServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  private val getNameForPersonService: GetNameForPersonService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val hmppsId = persona.identifiers.nomisNumber!!
      val filters = ConsumerFilters(null)
      val person = Person(firstName = persona.firstName, lastName = persona.lastName)

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(data = person),
        )
      }

      it("gets person name for hmpps Id calls getPersonWithPrisonFilter") {
        getNameForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getPersonWithPrisonFilter(hmppsId, filters)
      }

      it("returns a person name") {
        val response = getNameForPersonService.execute(hmppsId, filters)
        response.data.shouldBe(PersonName(firstName = person.firstName, lastName = person.lastName))
      }

      it("returns the upstream error when an error occurs") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val response = getNameForPersonService.execute(hmppsId, filters)
        response.errors.shouldBe(errors)
      }
    },
  )
