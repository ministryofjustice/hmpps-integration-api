@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
internal class GetPersonServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getPersonService: GetPersonService,
) : DescribeSpec(
    {
      val hmppsId = "2003/13116M"

      val nomsNumber = "N1234PS"
      val invalidNomsNumber = "N1234PSX"
      val prisoner = POSPrisoner(firstName = "Jim", lastName = "Brown", dateOfBirth = LocalDate.of(1992, 12, 3), prisonerNumber = nomsNumber)
      val wrongPrisonId = "XYZ"
      val prisonId = "ABC"
      val filters = ConsumerFilters(listOf(prisonId))
      val blankConsumerFilters = ConsumerFilters(null)
      val personOnProbation = PersonOnProbation(Person(firstName = "Sam", lastName = "Person", identifiers = Identifiers(nomisNumber = nomsNumber)), underActiveSupervision = true)
      val prisonerWithPrisonId = POSPrisoner(firstName = "Sam", lastName = "Person", prisonId = prisonId)

      beforeEach {
        Mockito.reset(prisonerOffenderSearchGateway)
        Mockito.reset(probationOffenderSearchGateway)

        whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
          Response(data = listOf(POSPrisoner(firstName = "Qui-gon", lastName = "Jin", prisonerNumber = "A1234AA"))),
        )
        whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
          Response(data = PersonOnProbation(Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = "A1234AA")), underActiveSupervision = true)),
        )
        whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber))
          .thenReturn(Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND))))
        whenever(probationOffenderSearchGateway.getPerson(id = invalidNomsNumber)).thenReturn(
          Response(data = PersonOnProbation(Person(firstName = "Sam", lastName = "Person", identifiers = Identifiers(nomisNumber = null)), underActiveSupervision = true)),
        )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = "A1234AA")).thenReturn(
          Response(data = POSPrisoner(firstName = "Sam", lastName = "Mills")),
        )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = nomsNumber)).thenReturn(Response(data = prisoner))
      }

      it("gets a person from Probation Offender Search") {
        getPersonService.execute(hmppsId)

        verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(hmppsId)
      }

      it("gets a person from Prison Offender Search when hmpps id is noms number and not found in probation search") {
        val response = getPersonService.execute(nomsNumber)
        verify(probationOffenderSearchGateway).getPerson(nomsNumber)
        verify(prisonerOffenderSearchGateway).getPrisonOffender(nomsNumber)
        with(requireNotNull(response.data)) {
          assertThat(firstName).isEqualTo(prisoner.firstName)
          assertThat(lastName).isEqualTo(prisoner.lastName)
          assertThat(dateOfBirth).isEqualTo(prisoner.dateOfBirth)
          assertThat(nomsNumber).isEqualTo(prisoner.prisonerNumber)
        }
      }

      it("returns a person") {
        val personFromProbationOffenderSearch = PersonOnProbation(Person("Molly", "Mob"), underActiveSupervision = true)

        whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(
          Response(personFromProbationOffenderSearch),
        )

        val result = getPersonService.execute(hmppsId)

        result.data.shouldBe(personFromProbationOffenderSearch)
      }

      it("returns null when a person isn't found in probation offender search") {
        whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(Response(data = null))

        val result = getPersonService.execute(hmppsId)
        val expectedResult = null

        result.data.shouldBe(expectedResult)
      }

      it("returns a person with both probation and prison data when prison data exists") {
        val personFromProbationOffenderSearch = PersonOnProbation(Person("Paula", "First", identifiers = Identifiers(nomisNumber = "A1234AA")), underActiveSupervision = true)
        val personFromPrisonOffenderSearch = POSPrisoner("Sam", "Mills")

        whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(
          Response(data = personFromProbationOffenderSearch),
        )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender("A1234AA")).thenReturn(
          Response(data = personFromPrisonOffenderSearch),
        )

        val result = getPersonService.getCombinedDataForPerson(hmppsId)
        val expectedResult = result.data

        result.data.shouldBe(expectedResult)
        result.errors shouldBe emptyList()
      }

      it("returns errors when unable to retrieve prison data and data when probation data is available") {
        val personFromProbationOffenderSearch = PersonOnProbation(Person("Paula", "First", identifiers = Identifiers(nomisNumber = "A1234AA")), underActiveSupervision = true)

        whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(Response(data = personFromProbationOffenderSearch))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender("A1234AA")).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"))))

        val result = getPersonService.getCombinedDataForPerson(hmppsId)
        result.data shouldBe OffenderSearchResponse(prisonerOffenderSearch = null, probationOffenderSearch = personFromProbationOffenderSearch)
        result.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISONER_OFFENDER_SEARCH)
        result.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        result.errors
          .first()
          .description
          .shouldBe("MockError")
      }

      it("returns a prisoner when valid hmppsId is provided") {
        val validHmppsId = "G2996UX"
        val person = PersonInPrison(firstName = "Sam", lastName = "Mills", category = null, csra = null, receptionDate = null, status = null, prisonId = null, prisonName = null, cellLocation = null)
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = "G2996UX")).thenReturn(
          Response(data = POSPrisoner(firstName = "Sam", lastName = "Mills")),
        )

        val result = getPersonService.getPrisoner(validHmppsId, blankConsumerFilters)

        result.data.shouldBeTypeOf<PersonInPrison>()
        result.data!!.firstName.shouldBe(person.firstName)
        result.data!!.lastName.shouldBe(person.lastName)
        result.errors.shouldBe(emptyList())
      }

      it("returns null when prisoner is not found") {
        val zeroHitHmppsId = "G2996UX"
        val prisonResponse: Response<POSPrisoner?> = Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))

        whenever(prisonerOffenderSearchGateway.getPrisonOffender("G2996UX")).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))))
        val result = getPersonService.getPrisoner(zeroHitHmppsId, blankConsumerFilters)

        result.data.shouldBe(null)
        result.errors.shouldBe(prisonResponse.errors)
      }

      it("returns error when invalid hmppsId is provided") {
        val invalidHmppsId = "invalid_id"
        val expectedError = UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.BAD_REQUEST, "Invalid HMPPS ID: $invalidHmppsId")
        val result = getPersonService.getPrisoner(invalidHmppsId, blankConsumerFilters)

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(expectedError))
      }

      it("returns error when nomis number is not found") {
        val hmppsIdInCrnFormat = "AB123123"
        val expectedError =
          listOf(
            UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "NOMIS number not found"),
            UpstreamApiError(causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"),
          )

        whenever(probationOffenderSearchGateway.getPerson(id = hmppsIdInCrnFormat)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "NOMIS number not found"))),
        )

        val result = getPersonService.getPrisoner(hmppsIdInCrnFormat, blankConsumerFilters)

        result.data.shouldBe(null)
        result.errors.shouldBe(expectedError)
      }

      it("returns null when prisoner is found but not in approved prison") {
        val wrongPrisonHmppsId = "Z9999ZZ"
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(wrongPrisonHmppsId))
          .thenReturn(Response(data = POSPrisoner(firstName = "Test", lastName = "Person", prisonId = "XYZ")))

        val result = getPersonService.getPrisoner(wrongPrisonHmppsId, ConsumerFilters(prisons = listOf("ABC")))

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("returns prisoner when in approved prison") {
        val correctPrisonHmppsId = "Z9999ZZ"
        val prisonId = "XYZ"
        val posPrisoner = POSPrisoner(firstName = "Test", lastName = "Person", prisonId = prisonId)
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(correctPrisonHmppsId))
          .thenReturn(Response(data = posPrisoner))

        val result = getPersonService.getPrisoner(correctPrisonHmppsId, ConsumerFilters(prisons = listOf(prisonId)))

        result.data.shouldBeTypeOf<PersonInPrison>()
        result.data!!.firstName.shouldBe(posPrisoner.firstName)
        result.data!!.lastName.shouldBe(posPrisoner.lastName)
        result.errors.shouldBe(emptyList())
      }

      it("returns prisoner if no prison filter present") {
        val validHmppsId = "G2996UX"
        val person = PersonInPrison(firstName = "Sam", lastName = "Mills", category = null, csra = null, receptionDate = null, status = null, prisonId = null, prisonName = null, cellLocation = null)

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = validHmppsId)).thenReturn(
          Response(data = POSPrisoner(firstName = "Sam", lastName = "Mills")),
        )

        val result = getPersonService.getPrisoner(validHmppsId, ConsumerFilters(prisons = null))

        result.data.shouldBeTypeOf<PersonInPrison>()
        result.data!!.firstName.shouldBe(person.firstName)
        result.data!!.lastName.shouldBe(person.lastName)
        result.errors.shouldBe(emptyList())
      }

      it("returns null if no prisons in prison filter") {
        val validHmppsId = "Z9999ZZ"
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(validHmppsId))
          .thenReturn(Response(data = POSPrisoner(firstName = "Test", lastName = "Person", prisonId = "XYZ")))

        val result = getPersonService.getPrisoner(validHmppsId, ConsumerFilters(prisons = emptyList()))

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("does not return prisoners who are missing prison ID") {
        val validHmppsId = "Z9999ZZ"
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(validHmppsId))
          .thenReturn(Response(data = POSPrisoner(firstName = "Test", lastName = "Person")))

        val result = getPersonService.getPrisoner(validHmppsId, ConsumerFilters(prisons = listOf("ABC")))

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("returns prisoner if no consumer filters present") {
        val validHmppsId = "G2996UX"
        val person = PersonInPrison(firstName = "Sam", lastName = "Mills", category = null, csra = null, receptionDate = null, status = null, prisonId = null, prisonName = null, cellLocation = null)

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = validHmppsId)).thenReturn(
          Response(data = POSPrisoner(firstName = "Sam", lastName = "Mills")),
        )

        val result = getPersonService.getPrisoner(validHmppsId, null)

        result.data.shouldBeTypeOf<PersonInPrison>()
        result.data.shouldBe(person)
        result.errors.shouldBe(emptyList())
      }

      it("if not a hmpps is not a nomis number or crn then return a bad request") {
        val result = getPersonService.getPersonWithPrisonFilter(invalidNomsNumber, filters)

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)))
      }

      it("if filters are present, consumer filter check fails, return 404") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = POSPrisoner(firstName = "Sam", lastName = "Person", prisonId = wrongPrisonId)))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(wrongPrisonId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)

        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("if filters are present, we get data from probation offender search") {

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))
        whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
          Response(data = personOnProbation),
        )

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)

        result.data.shouldBe(personOnProbation)
      }

      it("if filters are present, probation offender search returns 404, get data from POS gateway") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonId, filters)).thenReturn(Response(data = null))
        whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
          Response(data = null, errors = errors),
        )

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)

        val person = prisonerWithPrisonId.toPerson()
        result.data.shouldNotBeNull()
        result.data!!.firstName.shouldBe(person.firstName)
        result.data!!.lastName.shouldBe(person.lastName)
        verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPrisonOffender(nomsNumber)
      }

      it("if filters are present, POS gateway returns 404") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, filters)

        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("if filters are null, we get data from probation offender search") {
        whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
          Response(data = personOnProbation),
        )

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, blankConsumerFilters)

        result.data.shouldBe(personOnProbation)
      }

      it("if filters are null, probation offender search returns 404, get data from POS gateway") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = prisonerWithPrisonId))
        whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
          Response(data = null, errors = errors),
        )

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, blankConsumerFilters)
        val person = prisonerWithPrisonId.toPerson()

        result.data.shouldNotBeNull()
        result.data!!.firstName.shouldBe(person.firstName)
        result.data!!.lastName.shouldBe(person.lastName)
        verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPrisonOffender(nomsNumber)
      }

      it("if filters are null, probation offender search returns 404, POS gateway returns 404") {
        val errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)).thenReturn(Response(data = null, errors = errors))
        whenever(probationOffenderSearchGateway.getPerson(id = nomsNumber)).thenReturn(
          Response(data = null, errors = errors),
        )

        val result = getPersonService.getPersonWithPrisonFilter(nomsNumber, blankConsumerFilters)

        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
