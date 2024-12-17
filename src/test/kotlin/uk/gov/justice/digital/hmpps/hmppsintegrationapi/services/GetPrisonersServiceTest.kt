@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonersService::class],
)
internal class GetPrisonersServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getPrisonersService: GetPrisonersService,
) : DescribeSpec(
    {

      val nomsNumber = "N1234PS"
      // val prisoner = POSPrisoner(firstName = "Jim", lastName = "Brown", dateOfBirth = LocalDate.of(1992, 12, 3), prisonerNumber = nomsNumber)

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)
      }

      it("returns an error when the person queried is not found") {
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            errors =
              listOf(
                UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
              ),
            data = emptyList(),
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25")
        result?.errors.shouldBe(
          listOf(
            UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"),
          ),
        )
      }

      it("returns the person's data when queried") {
        val people = listOf(Person(firstName = "Qui-gon", lastName = "Jin"), Person(firstName = "John", lastName = "Jin"))
        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(
            data = people,
          ),
        )

        val result = getPrisonersService.execute("Qui-gon", "Jin", "1966-10-25")
        result?.data?.shouldBe(
          people,
        )
      }
    },
  )
