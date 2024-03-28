package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
internal class GetPersonServiceTest(
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  private val getPersonService: GetPersonService,
) : DescribeSpec({
    val hmppsId = "2003/13116M"

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(probationOffenderSearchGateway)

      whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
        Response(data = listOf(Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = "A1234AA")))),
      )
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
        Response(data = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = "A1234AA"))),
      )
    }

    it("gets a person from Probation Offender Search") {
      getPersonService.execute(hmppsId)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(hmppsId)
    }

    it("returns a person") {
      val personFromProbationOffenderSearch = Person("Molly", "Mob")

      whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(
        Response(personFromProbationOffenderSearch),
      )

      val result = getPersonService.execute(hmppsId)

      val expectedResult = Person("Molly", "Mob")

      result.data.shouldBe(expectedResult)
    }

    it("returns null when a person isn't found in probation offender search") {
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(Response(data = null))

      val result = getPersonService.execute(hmppsId)
      val expectedResult = null

      result.data.shouldBe(expectedResult)
    }
  })
