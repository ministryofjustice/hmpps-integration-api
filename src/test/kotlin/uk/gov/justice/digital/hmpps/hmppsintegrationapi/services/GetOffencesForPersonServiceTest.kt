package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestOffence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetOffencesForPersonService::class],
)
internal class GetOffencesForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockBean val nDeliusGateway: NDeliusGateway,
  private val getOffencesForPersonService: GetOffencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"
    val nDeliusCRN = "X123456"
    val prisonOffence1 = generateTestOffence(description = "Prison offence 1")
    val prisonOffence2 = generateTestOffence(description = "Prison offence 2")
    val prisonOffence3 = generateTestOffence(description = "Prison offence 3")
    val probationOffence1 = generateTestOffence(description = "Probation offence 1", hoCode = "05800", statuteCode = null)
    val probationOffence2 = generateTestOffence(description = "Probation offence 2", hoCode = "05801", statuteCode = null)
    val probationOffence3 = generateTestOffence(description = "Probation offence 3", hoCode = "05802", statuteCode = null)

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(nDeliusGateway)
      Mockito.reset(probationOffenderSearchGateway)
      Mockito.reset(prisonerOffenderSearchGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(data = listOf(Person(firstName = "Chandler", lastName = "Bing", identifiers = Identifiers(nomisNumber = prisonerNumber)))),
      )

      whenever(probationOffenderSearchGateway.getPerson(pncId = pncId)).thenReturn(
        Response(data = Person(firstName = "Chandler", lastName = "ProbationBing", identifiers = Identifiers(deliusCrn = nDeliusCRN))),
      )

      whenever(nomisGateway.getOffencesForPerson(prisonerNumber)).thenReturn(
        Response(
          data = listOf(
            prisonOffence1,
            prisonOffence2,
            prisonOffence3,
          ),
        ),
      )

      whenever(nDeliusGateway.getOffencesForPerson(nDeliusCRN)).thenReturn(
        Response(
          data = listOf(
            probationOffence1,
            probationOffence2,
            probationOffence3,
          ),
        ),
      )
    }

    it("Doesn't return prison offences when Prison Offender Search couldn't find the person by PNC ID") {
      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(
          data = emptyList(),
          errors = emptyList(),
        ),
      )

      val result = getOffencesForPersonService.execute(pncId)

      result.shouldBe(Response(data = listOf(probationOffence1, probationOffence2, probationOffence3)))
    }

    it("Doesn't return probation offences when Probation Offender Search couldn't find the person by PNC ID") {
      whenever(probationOffenderSearchGateway.getPerson(pncId = pncId)).thenReturn(
        Response(
          data = null,
          errors = emptyList(),
        ),
      )

      val result = getOffencesForPersonService.execute(pncId)

      result.shouldBe(Response(data = listOf(prisonOffence1, prisonOffence2, prisonOffence3)))
    }

    it("retrieves prisoner ID from Prisoner Offender Search using a PNC ID") {
      getOffencesForPersonService.execute(pncId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
    }

    it("retrieves nDelius CRN from Probation Offender Search using a PNC ID") {
      getOffencesForPersonService.execute(pncId)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(pncId = pncId)
    }

    it("retrieves offences from NOMIS using a prisoner number") {
      getOffencesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getOffencesForPerson(prisonerNumber)
    }

    it("retrieves offences from nDelius using a CRN") {
      getOffencesForPersonService.execute(pncId)

      verify(nDeliusGateway, VerificationModeFactory.times(1)).getOffencesForPerson(nDeliusCRN)
    }

    it("combines and returns offences from Nomis and nDelius") {
      val response = getOffencesForPersonService.execute(pncId)

      response.data.shouldBe(
        listOf(
          prisonOffence1,
          prisonOffence2,
          prisonOffence3,
          probationOffence1,
          probationOffence2,
          probationOffence3,
        ),
      )
    }

    it("returns errors when person offences cannot be found by PNC ID") {
      listOf(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApi.PROBATION_OFFENDER_SEARCH, UpstreamApi.NDELIUS, UpstreamApi.NOMIS).forEach {
        whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = it,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val response = getOffencesForPersonService.execute(pncId)

        response.errors.shouldHaveSize(1)
      }
    }
  },
)
