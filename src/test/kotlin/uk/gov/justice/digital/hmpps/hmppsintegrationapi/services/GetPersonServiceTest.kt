@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.REPLACE_PROBATION_SEARCH
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
internal class GetPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val deliusGateway: NDeliusGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val getPersonService: GetPersonService,
) : DescribeSpec(
    {
      val invalidNomsNumber = "N1234PSX"
      val prisonId = "ABC"
      val wrongPrisonId = "XYZ"
      val filters = ConsumerFilters(listOf(prisonId))
      val blankConsumerFilters = ConsumerFilters(null)

      val persona = personInProbationAndNomisPersona
      val personOnProbation = PersonOnProbation(Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers), underActiveSupervision = true)
      val personOnProbationMissingNomisNumber = PersonOnProbation(Person(firstName = persona.firstName, lastName = persona.lastName), underActiveSupervision = true)
      val prisoner = POSPrisoner(firstName = persona.firstName, lastName = persona.lastName, dateOfBirth = persona.dateOfBirth, prisonerNumber = persona.identifiers.nomisNumber, youthOffender = false)
      val prisonerWithPrisonId = POSPrisoner(firstName = prisoner.firstName, lastName = prisoner.lastName, prisonId = prisonId, youthOffender = false)
      val prisonerWithWrongPrisonId = POSPrisoner(firstName = prisoner.firstName, lastName = prisoner.lastName, prisonId = wrongPrisonId, youthOffender = false)

      val nomsNumber = prisoner.prisonerNumber!!
      val nomsNumberForPrisonerWithWrongPrisonId = "A1234AA"
      val crnNumber = personOnProbation.identifiers.deliusCrn!!

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)
        Mockito.reset(probationOffenderSearchGateway)
        Mockito.reset(deliusGateway)
        Mockito.reset(featureFlag)

        whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(true)

        whenever(deliusGateway.getPerson(id = crnNumber)).thenReturn(Response(data = personOnProbation))
        whenever(deliusGateway.getPerson(id = nomsNumber))
          .thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, UpstreamApiError.Type.ENTITY_NOT_FOUND))))
        whenever(deliusGateway.getPerson(id = invalidNomsNumber)).thenReturn(Response(data = personOnProbationMissingNomisNumber))

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = nomsNumber)).thenReturn(Response(data = prisoner))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = nomsNumberForPrisonerWithWrongPrisonId)).thenReturn(
          Response(data = prisonerWithWrongPrisonId),
        )
        whenever(prisonerOffenderSearchGateway.getPersons(prisoner.firstName, prisoner.lastName, prisoner.dateOfBirth.toString())).thenReturn(
          Response(data = listOf(prisonerWithPrisonId)),
        )

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, null)).thenReturn(Response(data = null))
      }

      describe("execute()") {
        it("returns a person from Probation Offender Search") {
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(id = crnNumber)).thenReturn(Response(data = personOnProbation))

          val result = getPersonService.execute(crnNumber)
          verify(probationOffenderSearchGateway, times(1)).getPerson(crnNumber)
          result.data.shouldBe(personOnProbation)
        }

        it("returns null when a person isn't found in probation offender search") {
          whenever(deliusGateway.getPerson(id = crnNumber)).thenReturn(Response(data = null))

          val result = getPersonService.execute(crnNumber)
          result.data.shouldBe(null)
        }

        it("gets a person from Prison Offender Search when hmpps id is noms number and not found in probation search") {
          val result = getPersonService.execute(nomsNumber)
          verify(deliusGateway).getPerson(nomsNumber)
          verify(prisonerOffenderSearchGateway).getPrisonOffender(nomsNumber)
          result.data.shouldNotBeNull()
          result.data.firstName.shouldBe(prisoner.firstName)
          result.data.lastName.shouldBe(prisoner.lastName)
          result.data.dateOfBirth.shouldBe(prisoner.dateOfBirth)
        }
      }

      describe("getCombinedDataForPerson()") {
        it("returns a person with both probation and prison data when prison data exists") {
          val result = getPersonService.getCombinedDataForPerson(crnNumber)
          result.data.prisonerOffenderSearch.shouldNotBeNull()
          result.data.prisonerOffenderSearch.firstName
            .shouldBe(prisoner.firstName)
          result.data.prisonerOffenderSearch.lastName
            .shouldBe(prisoner.lastName)
          result.data.prisonerOffenderSearch.dateOfBirth
            .shouldBe(prisoner.dateOfBirth)
          result.data.probationOffenderSearch.shouldBe(personOnProbation)
          result.errors.shouldBe(emptyList())
        }

        it("returns errors when unable to retrieve prison data and data when probation data is available") {
          val errors =
            listOf(
              UpstreamApiError(
                UpstreamApi.PRISONER_OFFENDER_SEARCH,
                UpstreamApiError.Type.ENTITY_NOT_FOUND,
                "MockError",
              ),
            )
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(id = crnNumber)).thenReturn(Response(data = personOnProbation))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors))

          val result = getPersonService.getCombinedDataForPerson(crnNumber)
          result.data.shouldBe(OffenderSearchResponse(prisonerOffenderSearch = null, probationOffenderSearch = personOnProbation))
          result.errors.shouldBe(errors)
        }
      }

      describe("getPrisoner()") {
        it("returns a prisoner when valid hmppsId is provided") {
          val validHmppsId = "G2996UX"
          val person = PersonInPrison(firstName = "Sam", lastName = "Mills", category = null, csra = null, receptionDate = null, status = null, prisonId = null, prisonName = null, cellLocation = null, youthOffender = false)
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = "G2996UX")).thenReturn(
            Response(data = POSPrisoner(firstName = "Sam", lastName = "Mills", youthOffender = false)),
          )

          val result = getPersonService.getPrisoner(validHmppsId, blankConsumerFilters)
          result.data.shouldBeTypeOf<PersonInPrison>()
          result.data.firstName.shouldBe(person.firstName)
          result.data.lastName.shouldBe(person.lastName)
          result.errors.shouldBe(emptyList())
        }

        it("returns null when prisoner is not found") {
          val errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors))

          val result = getPersonService.getPrisoner(nomsNumber, blankConsumerFilters)
          result.data.shouldBe(null)
          result.errors.shouldBe(errors)
        }

        it("returns error when invalid hmppsId is provided") {
          val invalidHmppsId = "invalid_id"
          val result = getPersonService.getPrisoner(invalidHmppsId, blankConsumerFilters)
          result.data.shouldBe(null)
          result.errors.shouldBe(
            listOf(
              UpstreamApiError(
                UpstreamApi.PRISON_API,
                UpstreamApiError.Type.BAD_REQUEST,
                "Invalid HMPPS ID: $invalidHmppsId",
              ),
            ),
          )
        }

        it("returns error when nomis number is not found") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
          whenever(deliusGateway.getPerson(id = crnNumber)).thenReturn(
            Response(data = null, errors),
          )

          val result = getPersonService.getPrisoner(crnNumber, blankConsumerFilters)
          result.data.shouldBe(null)
          result.errors.shouldBe(errors)
        }

        it("returns null when prisoner is found but not in approved prison") {
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithWrongPrisonId, errors = emptyList()))

          val result = getPersonService.getPrisoner(nomsNumber, filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
        }

        it("returns prisoner when in approved prison") {
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber))
            .thenReturn(Response(data = prisonerWithPrisonId))

          val result = getPersonService.getPrisoner(nomsNumber, filters)
          result.data.shouldBe(prisonerWithPrisonId.toPersonInPrison())
          result.errors.shouldBe(emptyList())
        }

        it("returns prisoner if no prison filter present") {
          val result = getPersonService.getPrisoner(nomsNumber, ConsumerFilters(prisons = null))
          result.data.shouldBe(prisoner.toPersonInPrison())
          result.errors.shouldBe(emptyList())
        }

        it("returns null if no prisons in prison filter") {
          val result = getPersonService.getPrisoner(nomsNumber, ConsumerFilters(prisons = emptyList()))
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
        }

        it("does not return prisoners who are missing prison ID") {
          val result = getPersonService.getPrisoner(nomsNumber, ConsumerFilters(prisons = listOf("ABC")))
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
        }

        it("returns prisoner if no consumer filters present") {
          val result = getPersonService.getPrisoner(nomsNumber, null)
          result.data.shouldBe(prisoner.toPersonInPrison())
          result.errors.shouldBe(emptyList())
        }
      }

      describe("getPersonWithPrisonFilter()") {
        it("if not a hmpps is not a nomis number or crn then return a bad request") {
          val result = getPersonService.getPersonWithPrisonFilter(invalidNomsNumber, filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST)))
        }

        it("if filters are present, consumer filter check fails, return 404") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = POSPrisoner(firstName = "Sam", lastName = "Person", prisonId = wrongPrisonId, youthOffender = false)))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(wrongPrisonId, filters)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(errors)
        }

        it("if filters are present, we get data from probation offender search") {
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))
          whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(Response(data = personOnProbation))

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)
          result.data.shouldBe(personOnProbation)
        }

        it("if filters are present, probation offender search returns 404, get data from POS gateway") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))
          whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
            Response(data = null, errors = errors),
          )

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)

          val person = prisonerWithPrisonId.toPerson()
          result.data.shouldNotBeNull()
          result.data.firstName.shouldBe(person.firstName)
          result.data.lastName.shouldBe(person.lastName)
          verify(prisonerOffenderSearchGateway, times(1)).getPrisonOffender(nomsNumber)
        }

        it("if filters are present, POS gateway returns 404") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(errors)
        }

        it("if filters are null, we get data from delius") {
          whenever(deliusGateway.getPerson(id = nomsNumber)).thenReturn(Response(data = personOnProbation))

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, blankConsumerFilters)
          result.data.shouldBe(personOnProbation)
        }

        it("if filters are null, probation offender search returns 404, get data from POS gateway") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
          whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
            Response(data = null, errors = errors),
          )

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, blankConsumerFilters)
          val person = prisonerWithPrisonId.toPerson()
          result.data.shouldNotBeNull()
          result.data.firstName.shouldBe(person.firstName)
          result.data.lastName.shouldBe(person.lastName)
          verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPrisonOffender(nomsNumber)
        }

        it("if filters are null, probation offender search returns 404, POS gateway returns 404") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))
          whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
            Response(data = null, errors = errors),
          )

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, blankConsumerFilters)
          result.data.shouldBe(null)
          result.errors.shouldBe(errors)
        }
      }

      describe("getNomisNumber") {
        it("Invalid hmppsId (not nomis or crn) passed in, return bad request") {
          val result = getPersonService.getNomisNumber(invalidNomsNumber)
          result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid HMPPS ID: $invalidNomsNumber")))
        }

        it("Nomis number passed in, return nomis number from POS") {
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))

          val result = getPersonService.getNomisNumber(nomsNumber)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Nomis number passed in, POS returns error, return error") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumber(nomsNumber)
          result.errors.shouldBe(errors)
        }

        it("Crn number passed in, return nomis number from probation") {
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))

          val result = getPersonService.getNomisNumber(crnNumber)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Crn number passed in - person from probation missing nomis number - return 404") {
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbationMissingNomisNumber, errors = emptyList()))

          val result = getPersonService.getNomisNumber(crnNumber)
          result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, "NOMIS number not found")))
        }

        it("Crn number passed in - person from probation returns error - return error from probation") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(crnNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumber(crnNumber)
          result.errors.shouldBe(errors)
        }
      }

      describe("getNomisNumberWithPrisonFilter") {
        it("Invalid hmppsId (not nomis or crn) passed in, return bad request") {
          val result = getPersonService.getNomisNumberWithPrisonFilter(invalidNomsNumber, filters = null)
          result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid HMPPS ID: $invalidNomsNumber")))
        }

        it("Nomis number passed in, filters null - return nomis number from POS") {
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))

          val result = getPersonService.getNomisNumberWithPrisonFilter(nomsNumber, filters = null)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Nomis number passed in, POS returns error, return error") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(nomsNumber, filters = null)
          result.errors.shouldBe(errors)
        }

        it("Nomis number passed in, filters present - return nomis number from POS") {
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))

          val result = getPersonService.getNomisNumberWithPrisonFilter(nomsNumber, filters)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Nomis number passed in, filters present, filter check failed - return 404") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithWrongPrisonId, errors = emptyList()))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(wrongPrisonId, filters)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(nomsNumber, filters)
          result.errors.shouldBe(errors)
        }

        it("Crn number passed in, filters null - return nomis number from probation") {
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters = null)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Crn number passed in - person from probation missing nomis number - return 404") {
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbationMissingNomisNumber, errors = emptyList()))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters = null)
          result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, "NOMIS number not found")))
        }

        it("Crn number passed in - person from probation returns error - return error from probation") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(crnNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters = null)
          result.errors.shouldBe(errors)
        }

        it("Crn number passed in, filters present - POS returns prison id, return nomis number from probation") {
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Crn number passed in, filters present - POS returns error, return error from POS") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters)
          result.errors.shouldBe(errors)
        }

        it("Crn number passed in, filters present - POS returns prison id, filter check failed - return 404") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(false)
          whenever(probationOffenderSearchGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>(prisonId, filters)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters)
          result.errors.shouldBe(errors)
        }
      }
    },
  )
