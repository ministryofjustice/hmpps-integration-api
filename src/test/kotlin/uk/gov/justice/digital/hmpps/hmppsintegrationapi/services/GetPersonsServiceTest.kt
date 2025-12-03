package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.ENHANCED_SEARCH_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPaginatedPrisoners
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSSort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonsService::class],
)
internal class GetPersonsServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val deliusGateway: NDeliusGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val getPersonsService: GetPersonsService,
) : DescribeSpec({
    val firstName = personInProbationAndNomisPersona.firstName
    val lastName = personInProbationAndNomisPersona.lastName
    val pncNumber = "2003/13116M"
    val dateOfBirth = personInProbationAndNomisPersona.dateOfBirth.toString()
    val prisoners = listOf(POSPrisoner(firstName = firstName, lastName = lastName, middleNames = "Gary", youthOffender = false))
    val paginatedPrisoners =
      POSPaginatedPrisoners(
        content = prisoners,
        totalElements = 1,
        totalPages = 1,
        first = true,
        last = true,
        size = 10,
        number = 0,
        sort =
          POSSort(
            empty = false,
            sorted = false,
            unsorted = false,
          ),
        numberOfElements = 1,
        pageable =
          POSPageable(
            offset = 0,
            sort =
              POSSort(
                empty = false,
                sorted = false,
                unsorted = false,
              ),
            pageSize = 10,
            pageNumber = 1,
            paged = true,
            unpaged = false,
          ),
        empty = false,
      )
    val prisonAttributeSearchResponse =
      Response<POSPaginatedPrisoners?>(
        data = paginatedPrisoners,
      )

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(deliusGateway)
      Mockito.reset(featureFlag)

      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(data = emptyList()))
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(data = emptyList()))
    }

    // Legacy search

    it("gets person(s) from Prisoner Offender Search") {
      getPersonsService.execute(firstName, lastName, null, dateOfBirth)

      verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth)
    }

    it("gets person(s) from Delius Gateway") {
      getPersonsService.execute(firstName, lastName, null, dateOfBirth)

      verify(deliusGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth)
    }

    it("defaults to not searching within aliases") {
      getPersonsService.execute(firstName, lastName, null, dateOfBirth)

      verify(deliusGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth, searchWithinAliases = false)
      verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth, searchWithinAliases = false)
    }

    it("allows searching within aliases") {
      whenever(
        deliusGateway.getPersons(firstName, lastName, null, dateOfBirth, searchWithinAliases = true),
      ).thenReturn(Response(data = emptyList()))
      whenever(
        prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth, searchWithinAliases = true),
      ).thenReturn(Response(data = emptyList()))

      getPersonsService.execute(firstName, lastName, null, dateOfBirth, true)

      verify(deliusGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth, true)
      verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth, true)
    }

    it("returns person(s)") {
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      val responseFromPrisonerOffenderSearch = Response(data = listOf(POSPrisoner(firstName = firstName, lastName = lastName, middleNames = "Gary", youthOffender = false)))

      whenever(
        deliusGateway.getPersons(firstName, lastName, null, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      whenever(
        prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth),
      ).thenReturn(responseFromPrisonerOffenderSearch)

      val result = getPersonsService.execute(firstName, lastName, null, dateOfBirth)
      val people = (responseFromPrisonerOffenderSearch.data.map { it.toPerson() } + responseFromProbationOffenderSearch.data)
      result.data.size.shouldBe(people.size)
      people
        .forEachIndexed { i, person: Person ->
          result.data[i].firstName.shouldBe(person.firstName)
          result.data[i].lastName.shouldBe(person.lastName)
          result.data[i].dateOfBirth.shouldBe(person.dateOfBirth)
        }
    }

    it("returns only probation person(s) if searched with a PNC") {
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      val responseFromPrisonerOffenderSearch = Response(data = listOf(POSPrisoner(firstName = firstName, lastName = lastName, middleNames = "Gary", youthOffender = false)))

      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(responseFromPrisonerOffenderSearch)

      val response = getPersonsService.execute(firstName, lastName, pncNumber, dateOfBirth)

      response.data.shouldBe(responseFromProbationOffenderSearch.data)
      verify(prisonerOffenderSearchGateway, times(0)).getPersons(firstName, lastName, dateOfBirth, true)
    }

    it("returns an empty list when no person(s) are found") {
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(emptyList()))
      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(emptyList()))

      val response = getPersonsService.execute(firstName, lastName, null, dateOfBirth)
      response.data.shouldBe(emptyList())
    }

    // End legacy search

    // Enhanced search

    it("enhanced search returns prisoner") {
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(prisonAttributeSearchResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters(prisons = listOf("MDI")))
      response.data.size.shouldBe(1)
      verify(prisonerOffenderSearchGateway, times(1)).attributeSearch(any())
    }

    it("enhanced search with prisons filter (no probation search) returns an error") {
      val error = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.TEST)
      val paginatedResponse = Response<POSPaginatedPrisoners?>(errors = listOf(error), data = null)
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(paginatedResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters(prisons = listOf("MDI")))
      response.data.shouldBeEmpty()
      response.errors.shouldBe(listOf(error))
    }

    it("enhanced search without prisons filter continues to call prison search (success) if probation search fails") {
      val error = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.TEST)
      val responseFromProbationOffenderSearch = Response(errors = listOf(error), data = emptyList<Person>())
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(prisonAttributeSearchResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters())
      response.data.size.shouldBe(1)
      response.errors.shouldBe(listOf(error))
    }

    it("enhanced search without prisons filter continues to call prison search (failure) if probation search fails") {
      val error = UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.TEST)
      val paginatedResponse = Response<POSPaginatedPrisoners?>(errors = listOf(error), data = null)
      val responseFromProbationOffenderSearch = Response(errors = listOf(error), data = emptyList<Person>())
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(paginatedResponse)
      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      val response = getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth, false, ConsumerFilters())
      response.data.shouldBeEmpty()
      response.errors.shouldBe(listOf(error, error))
    }

    it("does not do probation search for supervisionStatus == PRISON") {
      whenever(featureFlag.isEnabled(ENHANCED_SEARCH_ENABLED)).thenReturn(true)
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(prisonAttributeSearchResponse)

      val filters = ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PRISONS.name))

      getPersonsService.personAttributeSearch(firstName, lastName, null, dateOfBirth, true, filters)

      verifyNoInteractions(deliusGateway)
      verify(prisonerOffenderSearchGateway, times(1)).attributeSearch(any())
    }

    it("enhanced search returns an empty list when no person(s) are found") {
      whenever(featureFlag.isEnabled(ENHANCED_SEARCH_ENABLED)).thenReturn(true)
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(emptyList()))
      whenever(prisonerOffenderSearchGateway.attributeSearch(any())).thenReturn(
        Response<POSPaginatedPrisoners?>(
          data = paginatedPrisoners,
        ),
      )
      val response = getPersonsService.execute(firstName, lastName, null, dateOfBirth)
      response.data.shouldBe(emptyList())
    }
  })
