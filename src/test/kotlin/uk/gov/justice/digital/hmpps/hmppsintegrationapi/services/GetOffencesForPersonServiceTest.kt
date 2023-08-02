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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDate

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
    val prisonOffence1 = Offence(cjsCode = "RR12345", description = "Prision Offence 1", startDate = LocalDate.parse("2020-02-03"), endDate = LocalDate.parse("2020-03-03"), courtDate = LocalDate.parse("2020-04-03"), statuteCode = "RR12")
    val prisonOffence2 = Offence(cjsCode = "RR54321", description = "Prision Offence 2", startDate = LocalDate.parse("2021-03-04"), endDate = LocalDate.parse("2021-04-04"), courtDate = LocalDate.parse("2021-05-04"), statuteCode = "RR54")
    val prisonOffence3 = Offence(cjsCode = "RR24680", description = "Prision Offence 3", startDate = LocalDate.parse("2022-04-05"), endDate = LocalDate.parse("2022-05-05"), courtDate = LocalDate.parse("2022-06-05"), statuteCode = "RR24")
    val probationOffence1 = Offence(cjsCode = null, description = "Probation Offence 1", startDate = null, endDate = null, courtDate = null, statuteCode = null)
    val probationOffence2 = Offence(cjsCode = null, description = "Probation Offence 2", startDate = null, endDate = null, courtDate = null, statuteCode = null)
    val probationOffence3 = Offence(cjsCode = null, description = "Probation Offence 3", startDate = null, endDate = null, courtDate = null, statuteCode = null)

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

    it("records errors when offences cannot be found in NOMIS") {
      whenever(nomisGateway.getOffencesForPerson(prisonerNumber)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getOffencesForPersonService.execute(pncId)

      response.errors.shouldHaveSize(1)
    }

    it("returns errors when offences cannot be found in nDelius") {
      whenever(nDeliusGateway.getOffencesForPerson(nDeliusCRN)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getOffencesForPersonService.execute(pncId)

      response.errors.shouldHaveSize(1)
    }
  },
)
