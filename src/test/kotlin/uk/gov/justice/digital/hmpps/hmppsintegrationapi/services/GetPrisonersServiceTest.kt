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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

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
      val persona = personInProbationAndNomisPersona
      val firstName = persona.firstName
      val lastName = persona.lastName
      val dateOfBirth = persona.dateOfBirth

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)
      }

      it("returns an error when the person queried is not found and no prisonId filter is applied") {
        val errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"))
        whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth.toString())).thenReturn(
          Response(
            data = emptyList(),
            errors,
          ),
        )

        val result = getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, null)
        result.errors.shouldBe(errors)
      }

      it("returns the person's data when queried and no prisonId filter is applied") {
        val people =
          listOf(
            POSPrisoner(firstName = firstName, lastName = lastName, youthOffender = false),
            POSPrisoner(firstName = "John", lastName = lastName, youthOffender = false),
          )
        whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth.toString())).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, null)
        result.data.shouldBe(people.map { it.toPersonInPrison() })
      }

      it("returns an error when theres a filter prisons property but no values") {
        val errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"))
        whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth.toString())).thenReturn(
          Response(
            data = emptyList(),
            errors,
          ),
        )

        val result = getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, null)
        result.errors.shouldBe(errors)
      }

      it("returns an error when the person queried for is not in a prisonId matching their config") {
        val prisonIds = RoleFilters(prisons = listOf("FAKE_PRISON"))
        val errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerDetails(firstName, lastName, dateOfBirth.toString(), false, prisonIds.prisons)).thenReturn(
          Response(
            data = emptyList(),
            errors,
          ),
        )

        val result = getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, prisonIds)
        result.errors.shouldBe(errors)
      }

      it("returns the persons data when the person queried for is in a prisonId matching their config") {
        val people =
          listOf(
            POSPrisoner(firstName = firstName, lastName = lastName, youthOffender = false),
            POSPrisoner(firstName = "John", lastName = lastName, youthOffender = false),
          )
        val prisonIds = RoleFilters(prisons = listOf("VALID_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerDetails(firstName, lastName, dateOfBirth.toString(), false, prisonIds.prisons)).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, prisonIds)
        result.data.shouldBe(people.map { it.toPersonInPrison() })
      }

      it("returns the no data when the query is valid but no data is found") {
        val people = emptyList<POSPrisoner>()
        val prisonIds = RoleFilters(prisons = listOf("VALID_PRISON"))
        whenever(request.getAttribute("filters")).thenReturn(prisonIds)
        whenever(prisonerOffenderSearchGateway.getPrisonerDetails(firstName, lastName, dateOfBirth.toString(), false, prisonIds.prisons)).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, prisonIds)
        result.data.shouldBe(emptyList())
      }
    },
  )
