package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.FutureVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitorSupport

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetFutureVisitsService::class],
)
class GetFutureVisitsServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val prisonVisitsGateway: PrisonVisitsGateway,
  private val getFutureVisitsService: GetFutureVisitsService,
) : DescribeSpec({
    val hmppsId = "G6980GG"
    val nomisNumber = "F6980FF"
    val person = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = hmppsId, identifiers = Identifiers(nomisNumber = nomisNumber))
    val personWithoutNomisNumber = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = hmppsId)
    val futureVisitResponse =
      listOf(
        FutureVisit(
          prisonerId = "PrisonerId",
          prisonId = "MDI",
          prisonName = "Some Prison",
          visitRoom = "Room",
          visitType = "Type",
          visitStatus = "Status",
          outcomeStatus = "Outcome",
          visitRestriction = "Restriction",
          startTimestamp = "Start",
          endTimestamp = "End",
          createdTimestamp = "Created",
          modifiedTimestamp = "Modified",
          firstBookedDateTime = "First",
          visitors = emptyList(),
          visitNotes = emptyList(),
          visitContact = VisitContact(name = "Name", telephone = "Telephone", email = "Email"),
          visitorSupport = VisitorSupport(description = "Description"),
          applicationReference = "dfs-wjs-abc",
          reference = "dfs-wjs-abc",
          sessionTemplateReference = "dfs-wjs-xyz",
        ),
      )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(prisonVisitsGateway)

      whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters = null)).thenReturn(
        Response(data = person),
      )

      whenever(prisonVisitsGateway.getFutureVisits(nomisNumber)).thenReturn(
        Response(data = futureVisitResponse),
      )
    }

    it("returns a 200 status in the case of a successful query") {
      val response = getFutureVisitsService.execute(hmppsId, filters = null)
      response.data.shouldBe(futureVisitResponse)
      response.errors.shouldBeEmpty()
    }

    it("returns an errors if getPersonWithPrisonFilter returns an error") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS, description = "Person with prison filters error"))
      whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters = null)).thenReturn(
        Response(data = null, errors = errors),
      )

      val response = getFutureVisitsService.execute(hmppsId, filters = null)
      response.data.shouldBe(null)
      response.errors.shouldBe(errors)
    }

    it("returns a 404 if no Nomis number found on the person") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS, description = "No Nomis number found for $hmppsId"))
      whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filters = null)).thenReturn(
        Response(data = personWithoutNomisNumber),
      )

      val response = getFutureVisitsService.execute(hmppsId, filters = null)
      response.data.shouldBe(null)
      response.errors.shouldBe(errors)
    }

    it("returns an errors if gateway returns an error") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.MANAGE_PRISON_VISITS, description = "Gateway error"))
      whenever(prisonVisitsGateway.getFutureVisits(nomisNumber)).thenReturn(
        Response(data = null, errors = errors),
      )

      val response = getFutureVisitsService.execute(hmppsId, filters = null)
      response.data.shouldBe(null)
      response.errors.shouldBe(errors)
    }
  })
