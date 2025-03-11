package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
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

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetNameForPersonService::class],
)
internal class GetPersonNameServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  private val getNameForPersonService: GetNameForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val filters = ConsumerFilters(null)

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(data = Person(firstName = "Qui-gon", lastName = "Jin")),
        )
      }

      it("gets person name for hmpps Id calls getPersonWithPrisonFilter") {
        getNameForPersonService.execute(hmppsId, filters)

        verify(getPersonService, VerificationModeFactory.times(1)).getPersonWithPrisonFilter(hmppsId, filters)
      }

      it("returns a person name") {
        val response = getNameForPersonService.execute(hmppsId, filters)

        response.data.shouldBe(PersonName(firstName = "Qui-gon", lastName = "Jin"))
      }

      it("returns the upstream error when an error occurs") {
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(
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

        val response = getNameForPersonService.execute(hmppsId, filters)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PROBATION_OFFENDER_SEARCH)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
