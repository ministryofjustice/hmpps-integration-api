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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
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

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, null)
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
          ),
        )
      }

      it("returns the person's data when queried and no prisonId filter is applied") {
        val people =
          listOf(
            POSPrisoner(firstName = "Qui-gon", lastName = "Jin"),
            POSPrisoner(firstName = "John", lastName = "Jin"),
          )
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, null)
        result.data.size.shouldBe(people.size)
        people
          .map { it.toPersonInPrison() }
          .forEachIndexed { i, prisoner: PersonInPrison ->
            result.data[i].firstName.shouldBe(prisoner.firstName)
            result.data[i].lastName.shouldBe(prisoner.lastName)
            result.data[i].dateOfBirth.shouldBe(prisoner.dateOfBirth)
          }
      }

      it("returns an error when theres a filter prisons property but no values") {
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            errors =
              listOf(
                UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
              ),
            data = emptyList(),
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, null)
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
          ),
        )
      }

      it("returns an error when the person queried for is not in a prisonId matching their config") {
        val prisonIds = ConsumerFilters(prisons = listOf("FAKE_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerDetails("Qui-gon", "Jin", "1966-10-25", false, prisonIds.prisons)).thenReturn(
          Response(
            errors =
              listOf(
                UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
              ),
            data = emptyList(),
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, prisonIds)
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
          ),
        )
      }

      it("returns the persons data when the person queried for is in a prisonId matching their config") {
        val people =
          listOf(
            POSPrisoner(firstName = "Qui-gon", lastName = "Jin"),
            POSPrisoner(firstName = "John", lastName = "Jin"),
          )
        val prisonIds = ConsumerFilters(prisons = listOf("VALID_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerDetails("Qui-gon", "Jin", "1966-10-25", false, prisonIds.prisons)).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, prisonIds)
        result.data.size.shouldBe(people.size)
        people
          .map { it.toPersonInPrison() }
          .forEachIndexed { i, prisoner: PersonInPrison ->
            result.data[i].firstName.shouldBe(prisoner.firstName)
            result.data[i].lastName.shouldBe(prisoner.lastName)
            result.data[i].dateOfBirth.shouldBe(prisoner.dateOfBirth)
          }
      }

      it("returns the no data when the query is valid but no data is found") {
        val people = emptyList<POSPrisoner>()

        val prisonIds = ConsumerFilters(prisons = listOf("VALID_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerDetails("Qui-gon", "Jin", "1966-10-25", false, prisonIds.prisons)).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25", false, prisonIds)
        result.data.size.shouldBe(0)
        result.data.shouldBe(emptyList())
      }
    },
  )
