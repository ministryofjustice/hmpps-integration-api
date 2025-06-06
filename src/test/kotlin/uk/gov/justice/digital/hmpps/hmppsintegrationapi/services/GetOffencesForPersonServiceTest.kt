package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestOffence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetOffencesForPersonService::class],
)
internal class GetOffencesForPersonServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  private val getOffencesForPersonService: GetOffencesForPersonService,
) : DescribeSpec(
    {
      val prisonOffence1 = generateTestOffence(description = "Prison offence 1")
      val prisonOffence2 = generateTestOffence(description = "Prison offence 2")
      val prisonOffence3 = generateTestOffence(description = "Prison offence 3")
      val probationOffence1 = generateTestOffence(description = "Probation offence 1", hoCode = "05800", statuteCode = null)
      val probationOffence2 = generateTestOffence(description = "Probation offence 2", hoCode = "05801", statuteCode = null)
      val probationOffence3 = generateTestOffence(description = "Probation offence 3", hoCode = "05802", statuteCode = null)
      val personFromProbationOffenderSearch =
        Person(
          firstName = personInProbationAndNomisPersona.firstName,
          lastName = personInProbationAndNomisPersona.lastName,
          identifiers = personInProbationAndNomisPersona.identifiers,
        )
      val nomisNumber = personFromProbationOffenderSearch.identifiers.nomisNumber!!
      val deliusCrn = personFromProbationOffenderSearch.identifiers.deliusCrn!!
      val hmppsId = nomisNumber
      val filters = null

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonApiGateway)
        Mockito.reset(nDeliusGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )
        whenever(prisonApiGateway.getOffencesForPerson(nomisNumber)).thenReturn(
          Response(
            data =
              listOf(
                prisonOffence1,
                prisonOffence2,
                prisonOffence3,
              ),
          ),
        )
        whenever(nDeliusGateway.getOffencesForPerson(deliusCrn)).thenReturn(
          Response(
            data =
              listOf(
                probationOffence1,
                probationOffence2,
                probationOffence3,
              ),
          ),
        )
      }

      it("Returns prison and probation offences given a hmppsId and no filters") {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        val result = getOffencesForPersonService.execute(hmppsId, filters)
        result.shouldBe(
          Response(data = listOf(prisonOffence1, prisonOffence2, prisonOffence3, probationOffence1, probationOffence2, probationOffence3)),
        )
      }

      it("gets a person using a Hmpps ID") {
        getOffencesForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).execute(hmppsId = hmppsId)
      }

      it("gets offences from NOMIS using a prisoner number") {
        getOffencesForPersonService.execute(hmppsId, filters)
        verify(prisonApiGateway, times(1)).getOffencesForPerson(nomisNumber)
      }

      it("gets offences from nDelius using a CRN") {
        getOffencesForPersonService.execute(hmppsId, filters)
        verify(nDeliusGateway, VerificationModeFactory.times(1)).getOffencesForPerson(deliusCrn)
      }

      it("combines and returns offences from Nomis and nDelius") {
        val response = getOffencesForPersonService.execute(hmppsId, filters)
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

      it("returns only offences from Nomis when prison filters present ") {
        val populatedFilters = ConsumerFilters(listOf("ABC"))
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, populatedFilters)).thenReturn(
          Response(
            data = personFromProbationOffenderSearch,
          ),
        )

        val response = getOffencesForPersonService.execute(hmppsId, populatedFilters)
        response.data.shouldBe(
          listOf(
            prisonOffence1,
            prisonOffence2,
            prisonOffence3,
          ),
        )
      }

      describe("when an upstream API returns an error when looking up a person from a Hmpps ID") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )

        beforeEach {
          whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
            Response(
              data = null,
              errors,
            ),
          )
        }

        it("records upstream API errors") {
          val response = getOffencesForPersonService.execute(hmppsId, filters)
          response.errors.shouldBe(errors)
        }

        it("does not get offences from Nomis") {
          getOffencesForPersonService.execute(hmppsId, filters)
          verify(prisonApiGateway, times(0)).getOffencesForPerson(id = nomisNumber)
        }

        it("does not get offences from nDelius") {
          getOffencesForPersonService.execute(hmppsId, filters)
          verify(nDeliusGateway, times(0)).getOffencesForPerson(id = deliusCrn)
        }
      }

      it("records errors when it cannot find offences for a person") {
        val deliusError =
          UpstreamApiError(
            causedBy = UpstreamApi.NDELIUS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          )
        val nomisError =
          UpstreamApiError(
            causedBy = UpstreamApi.PRISON_API,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          )
        whenever(nDeliusGateway.getOffencesForPerson(id = deliusCrn)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(deliusError),
          ),
        )
        whenever(prisonApiGateway.getOffencesForPerson(id = nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(nomisError),
          ),
        )

        val response = getOffencesForPersonService.execute(hmppsId, filters)
        response.errors.shouldBe(listOf(nomisError, deliusError))
      }
    },
  )
