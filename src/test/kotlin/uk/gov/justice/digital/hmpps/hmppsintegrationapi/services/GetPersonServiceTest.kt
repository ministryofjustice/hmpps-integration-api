@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInNomisOnlyPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationOnlyPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
internal class GetPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
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

      val personaInProbationAndPrison = personInProbationAndNomisPersona
      val personOnProbation = personaInProbationAndPrison.run { PersonOnProbation(Person(firstName = firstName, lastName = lastName, identifiers = identifiers), underActiveSupervision = true) }
      val personOnProbationMissingNomisNumber = personaInProbationAndPrison.run { PersonOnProbation(Person(firstName = firstName, lastName = lastName), underActiveSupervision = true) }
      val prisoner = personaInProbationAndPrison.run { POSPrisoner(firstName = firstName, lastName = lastName, dateOfBirth = dateOfBirth, prisonerNumber = identifiers.nomisNumber, youthOffender = false) }
      val prisonerWithPrisonId = POSPrisoner(firstName = prisoner.firstName, lastName = prisoner.lastName, prisonId = prisonId, youthOffender = false)
      val prisonerWithWrongPrisonId = POSPrisoner(firstName = prisoner.firstName, lastName = prisoner.lastName, prisonId = wrongPrisonId, youthOffender = false)

      val personOnProbationOnly = personInProbationOnlyPersona.run { PersonOnProbation(Person(firstName = firstName, lastName = lastName, identifiers = identifiers), underActiveSupervision = true) }
      val personInPrisonOnly = personInNomisOnlyPersona.run { Person(firstName = firstName, lastName = lastName, identifiers = identifiers) }
      val prisonerInPrisonOnly = personInPrisonOnly.run { POSPrisoner(firstName = firstName, lastName = lastName, dateOfBirth = dateOfBirth, prisonerNumber = identifiers.nomisNumber, youthOffender = false) }

      val nomsNumber = prisoner.prisonerNumber!!
      val nomsNumberForPrisonerWithWrongPrisonId = "A1234AA"
      val crnNumber = personOnProbation.identifiers.deliusCrn!!
      val unknownCrnNumber = "X999999"
      val unknownNomsNumber = "X9999YZ"

      fun notFoundErrors(vararg upstreamApi: UpstreamApi) = upstreamApi.map { UpstreamApiError(causedBy = it, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "MockError") }.toList()

      fun givenPersonFoundInProbation(
        id: String = crnNumber,
        person: PersonOnProbation? = personOnProbation,
        errors: List<UpstreamApiError> = emptyList(),
      ) = whenever(deliusGateway.getPerson(id)).thenReturn(Response(data = person, errors = errors))

      fun givenPersonNotFoundInProbation(
        id: String = unknownCrnNumber,
        errors: List<UpstreamApiError> = notFoundErrors(UpstreamApi.NDELIUS),
      ) = whenever(deliusGateway.getPerson(id)).thenReturn(Response(data = null, errors = errors))

      fun givenPrisonerFound(
        nomisNumber: String = nomsNumber,
        posPrisoner: POSPrisoner = prisoner,
      ) = whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(Response(data = posPrisoner))

      fun givenPrisonerNotFound(nomisNumber: String = unknownNomsNumber) =
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber))
          .thenReturn(Response(data = null, errors = notFoundErrors(UpstreamApi.PRISONER_OFFENDER_SEARCH)))

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)
        Mockito.reset(deliusGateway)
        Mockito.reset(featureFlag)

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
          whenever(deliusGateway.getPerson(id = crnNumber)).thenReturn(Response(data = personOnProbation))

          val result = getPersonService.execute(crnNumber)
          verify(deliusGateway, times(1)).getPerson(crnNumber)
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
        describe("Given a person found in Probation, and hmppsId is CRN") {
          val hmppsId = crnNumber

          it("returns a person with both Probation and Prison data, when found in Prison") {
            val result = getPersonService.getCombinedDataForPerson(hmppsId)

            with(result.data.prisonerOffenderSearch) {
              this.shouldNotBeNull()
              firstName shouldBe prisoner.firstName
              lastName shouldBe prisoner.lastName
              dateOfBirth shouldBe prisoner.dateOfBirth
            }
            result.data.probationOffenderSearch shouldBe personOnProbation
            result.errors.shouldBeEmpty()
          }

          it("returns a person with Probation data and Prison error, when not found in Prison") {
            val nomsNumberNotFound = personOnProbation.identifiers.nomisNumber!!
            givenPrisonerNotFound(nomsNumberNotFound)
            val result = getPersonService.getCombinedDataForPerson(hmppsId)

            result.data shouldBe OffenderSearchResponse(prisonerOffenderSearch = null, probationOffenderSearch = personOnProbation)
            result.errors shouldBe notFoundErrors(UpstreamApi.PRISONER_OFFENDER_SEARCH)
          }
        }

        describe("Given a person found in Probation only, and hmppsId is CRN") {
          val person = personOnProbationOnly
          val crnNumber = person.identifiers.deliusCrn!!
          val hmppsId = crnNumber

          beforeEach {
            givenPersonFoundInProbation(id = hmppsId, person = person)
          }

          it("returns a person with Probation data, without error") {
            val result = getPersonService.getCombinedDataForPerson(hmppsId)

            result.data.probationOffenderSearch shouldBe person
            result.data.prisonerOffenderSearch.shouldBeNull()
            result.errors.shouldBeEmpty()
          }
        }

        describe("Given a person not found in Probation, and hmppsId is CRN") {
          val hmppsId = unknownCrnNumber

          beforeEach {
            givenPersonNotFoundInProbation(id = hmppsId)
          }

          it("returns not found error") {
            val result = getPersonService.getCombinedDataForPerson(hmppsId)

            result.errors shouldBe notFoundErrors(UpstreamApi.NDELIUS)
            result.data shouldBe OffenderSearchResponse(prisonerOffenderSearch = null, probationOffenderSearch = null)
            verify(prisonerOffenderSearchGateway, never()).getPrisonOffender(any())
          }
        }

        describe("Given a person not found in Probation, and hmppsId is noms number") {
          val hmppsId = nomsNumber
          val prisoner = prisonerInPrisonOnly
          val nomisNumber = prisoner.prisonerNumber!!

          beforeEach {
            givenPersonNotFoundInProbation(id = hmppsId)
          }

          it("returns a person with Prison data only, if found in Prison") {
            givenPrisonerFound(nomisNumber, prisoner)
            val result = getPersonService.getCombinedDataForPerson(hmppsId)

            result.data.probationOffenderSearch.shouldBeNull()
            result.errors.shouldBe(emptyList())
            with(result.data.prisonerOffenderSearch) {
              this.shouldNotBeNull()
              firstName shouldBe prisoner.firstName
              lastName shouldBe prisoner.lastName
              dateOfBirth shouldBe prisoner.dateOfBirth
            }
          }

          it("returns errors when prisoner is not found") {
            givenPrisonerNotFound()
            val result = getPersonService.getCombinedDataForPerson(hmppsId)

            result.errors shouldBe notFoundErrors(UpstreamApi.PRISONER_OFFENDER_SEARCH)
            with(result.data) {
              prisonerOffenderSearch.shouldBeNull()
              probationOffenderSearch.shouldBeNull()
            }
          }
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
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))
          whenever(deliusGateway.getPerson(id = nomsNumber)).thenReturn(Response(data = personOnProbation))

          val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)
          result.data.shouldBe(personOnProbation)
        }

        it("if filters are present, probation offender search returns 404, get data from POS gateway") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))
          whenever(deliusGateway.getPerson(id = nomsNumber)).thenReturn(
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
          whenever(deliusGateway.getPerson(id = nomsNumber)).thenReturn(
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
          whenever(deliusGateway.getPerson(id = nomsNumber)).thenReturn(
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
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = null, errors = errors))

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
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters = null)
          result.errors.shouldBe(errors)
        }

        it("Crn number passed in, filters present - POS returns prison id, return nomis number from probation") {
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters)
          result.data.shouldBe(NomisNumber(nomsNumber))
        }

        it("Crn number passed in, filters present - POS returns error, return error from POS") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters)
          result.errors.shouldBe(errors)
        }

        it("Crn number passed in, filters present - POS returns prison id, filter check failed - return 404") {
          val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(deliusGateway.getPerson(crnNumber)).thenReturn(Response(data = personOnProbation, errors = emptyList()))
          whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId, errors = emptyList()))
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Nothing>(prisonId, filters)).thenReturn(Response(data = null, errors = errors))

          val result = getPersonService.getNomisNumberWithPrisonFilter(crnNumber, filters)
          result.errors.shouldBe(errors)
        }
      }
    },
  )
