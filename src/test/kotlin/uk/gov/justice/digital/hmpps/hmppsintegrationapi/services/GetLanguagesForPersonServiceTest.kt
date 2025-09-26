package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSLanguage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisonerAlias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLanguagesForPersonService::class],
)
internal class GetLanguagesForPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getLanguagesForPersonService: GetLanguagesForPersonService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)
      val filters = null
      val languagesGatewayResponse =
        POSPrisoner(
          firstName = person.firstName,
          lastName = person.lastName,
          middleNames = "Middle Name",
          dateOfBirth = LocalDate.parse("2023-03-01"),
          gender = "Gender",
          ethnicity = "Ethnicity",
          prisonerNumber = "prisonerNumber",
          pncNumber = "pncNumber",
          croNumber = "croNumber",
          aliases =
            listOf(
              POSPrisonerAlias(firstName = "Alias First Name", lastName = "Alias Last Name"),
            ),
          youthOffender = false,
          languages =
            listOf(
              POSLanguage(
                type = "PRIM",
                code = "ENG",
                readSkill = "Y",
                writeSkill = "Y",
                speakSkill = "Y",
                interpreterRequested = true,
              ),
            ),
        )
      val languages = languagesGatewayResponse.toLanguages()

      val nomisNumber = person.identifiers.nomisNumber!!
      val hmppsId = nomisNumber

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(prisonerOffenderSearchGateway)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters)).thenReturn(Response(person))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(Response(languagesGatewayResponse))
      }

      it("performs a search according to hmpps Id") {
        getLanguagesForPersonService.execute(hmppsId, filters)
        verify(getPersonService, times(1)).getPersonWithPrisonFilter(hmppsId, filters)
      }

      it("should return a person's languages from gateway") {
        val result = getLanguagesForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(languages)
        result.errors.shouldBeEmpty()
      }

      it("should return an entity not found error if person found in person service but no nomis number set for them") {
        val personWithoutNomis = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = Identifiers(nomisNumber = null))
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(
          Response(data = personWithoutNomis),
        )

        val result = getLanguagesForPersonService.execute(hmppsId = hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }

      it("should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "notfound", filters = filters)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val result = getLanguagesForPersonService.execute(hmppsId = "notfound", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if a bad request is made to getPersonService") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.BAD_REQUEST,
            ),
          )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "badRequest", filters = filters)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val result = getLanguagesForPersonService.execute(hmppsId = "badRequest", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if prisoner offender search gateway returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val result = getLanguagesForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
