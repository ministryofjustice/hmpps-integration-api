@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonersService::class],
)
internal class GetPrisonersServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val request: HttpServletRequest,
  private val getPrisonersService: GetPrisonersService,
) : DescribeSpec(
    {

      val nomsNumber = "N1234PS"
      // val prisoner = POSPrisoner(firstName = "Jim", lastName = "Brown", dateOfBirth = LocalDate.of(1992, 12, 3), prisonerNumber = nomsNumber)

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)
      }

      it("returns an error when the person queried is not found and no prisonId filter is applied") {
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            errors =
              listOf(
                UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
              ),
            data = emptyList(),
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, ConsumerFilters(emptyList()))
        result?.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
          ),
        )
      }

      it("returns the person's data when queried and no prisonId filter is applied") {
        val people = listOf(Person(firstName = "Qui-gon", lastName = "Jin"), Person(firstName = "John", lastName = "Jin"))
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, ConsumerFilters(emptyList()))
        result?.data?.shouldBe(
          people,
        )
      }

      it("returns an error when theres a filter prisons property but no values") {
        val people = listOf(Person(firstName = "Qui-gon", lastName = "Jin"), Person(firstName = "John", lastName = "Jin"))
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            errors =
              listOf(
                UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
              ),
            data = emptyList(),
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, ConsumerFilters(prisons = listOf("")))
        result?.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"),
          ),
        )
      }

      it("returns an error when the person queried for is not in a prisonId matching their config") {
        val prisonIds = ConsumerFilters(prisons = listOf("FAKE_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerByCriteria("Qui-gon", "Jin", "1966-10-25", false, prisonIds.prisons)).thenReturn(
          Response(
            errors =
              listOf(
                UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
              ),
            data = emptyList(),
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, prisonIds)
        result?.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
          ),
        )
      }

      it("returns the persons data when the person queried for is in a prisonId matching their config") {
        val people = listOf(Person(firstName = "Qui-gon", lastName = "Jin", prisonId = "VALID_PRISON"), Person(firstName = "John", lastName = "Jin", prisonId = "VALID_PRISON"))
        val prisonIds = ConsumerFilters(prisons = listOf("VALID_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerByCriteria("Qui-gon", "Jin", "1966-10-25", false, prisonIds.prisons)).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, prisonIds)
        result?.data.shouldBe(
          people,
        )
      }
    },
  )
