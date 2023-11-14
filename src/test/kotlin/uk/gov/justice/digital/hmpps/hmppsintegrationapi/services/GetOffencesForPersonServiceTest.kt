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
  @MockBean val getPersonService: GetPersonService,
  @MockBean val nDeliusGateway: NDeliusGateway,
  private val getOffencesForPersonService: GetOffencesForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"
    val nDeliusCRN = "X123456"
    val prisonOffence1 = generateTestOffence(description = "Prison offence 1")
    val prisonOffence2 = generateTestOffence(description = "Prison offence 2")
    val prisonOffence3 = generateTestOffence(description = "Prison offence 3")
    val probationOffence1 = generateTestOffence(description = "Probation offence 1", hoCode = "05800", statuteCode = null)
    val probationOffence2 = generateTestOffence(description = "Probation offence 2", hoCode = "05801", statuteCode = null)
    val probationOffence3 = generateTestOffence(description = "Probation offence 3", hoCode = "05802", statuteCode = null)
    val personFromProbationOffenderSearch = Person(
      firstName = "Chandler",
      lastName = "ProbationBing",
      identifiers = Identifiers(deliusCrn = nDeliusCRN, nomisNumber = prisonerNumber),
    )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nomisGateway)
      Mockito.reset(nDeliusGateway)

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
        Response(
          data = personFromProbationOffenderSearch,
        ),
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

    it("Returns prison and probation offences given a hmppsId") {
      whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
        Response(
          data = personFromProbationOffenderSearch,
        ),
      )

      val result = getOffencesForPersonService.execute(hmppsId)

      result.shouldBe(Response(data = listOf(prisonOffence1, prisonOffence2, prisonOffence3, probationOffence1, probationOffence2, probationOffence3)))
    }

    it("retrieves a person using a Hmpps ID") {
      getOffencesForPersonService.execute(hmppsId)

      verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
    }

    it("retrieves offences from NOMIS using a prisoner number") {
      getOffencesForPersonService.execute(hmppsId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getOffencesForPerson(prisonerNumber)
    }

    it("retrieves offences from nDelius using a CRN") {
      getOffencesForPersonService.execute(hmppsId)

      verify(nDeliusGateway, VerificationModeFactory.times(1)).getOffencesForPerson(nDeliusCRN)
    }

    it("combines and returns offences from Nomis and nDelius") {
      val response = getOffencesForPersonService.execute(hmppsId)

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

    describe("when an upstream API returns an error when looking up a person from a Hmpps ID") {
      beforeEach {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = null,
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
              UpstreamApiError(
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )
      }

      it("records upstream API errors") {
        val response = getOffencesForPersonService.execute(hmppsId)
        response.errors.shouldHaveSize(2)
      }

      it("does not get offences from Nomis") {
        getOffencesForPersonService.execute(hmppsId)
        verify(nomisGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = prisonerNumber)
      }

      it("does not get offences from nDelius") {
        getOffencesForPersonService.execute(hmppsId)
        verify(nDeliusGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = nDeliusCRN)
      }
    }

    it("records errors when it cannot find offences for a person") {
      whenever(nDeliusGateway.getOffencesForPerson(id = nDeliusCRN)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      whenever(nomisGateway.getOffencesForPerson(id = prisonerNumber)).thenReturn(
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

      val response = getOffencesForPersonService.execute(hmppsId)
      response.errors.shouldHaveSize(2)
    }
  },
)
