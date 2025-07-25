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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVistExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetFutureVisitsService::class],
)
class GetFutureVisitsServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val prisonVisitsGateway: PrisonVisitsGateway,
  private val getFutureVisitsService: GetFutureVisitsService,
) : DescribeSpec({
    val persona = personInProbationAndNomisPersona
    val nomisNumber = persona.identifiers.nomisNumber!!
    val deliusCrn = persona.identifiers.deliusCrn!!
    val person = Person(firstName = persona.firstName, lastName = persona.lastName, hmppsId = nomisNumber, identifiers = persona.identifiers)
    val personWithoutNomisNumber = Person(firstName = persona.firstName, lastName = persona.lastName, hmppsId = deliusCrn)
    val futureVisitGatewayResponse =
      listOf(
        PVVisit(
          prisonerId = "PrisonerId",
          prisonId = "MDI",
          prisonName = "Some Prison",
          visitRoom = "Room",
          visitType = "Type",
          visitStatus = "Status",
          visitSubStatus = "AUTO_APPROVED",
          outcomeStatus = "Outcome",
          visitRestriction = "Restriction",
          startTimestamp = "Start",
          endTimestamp = "End",
          createdTimestamp = "Created",
          modifiedTimestamp = "Modified",
          firstBookedDateTime = "First",
          visitors = emptyList(),
          visitNotes = emptyList(),
          visitContact = PVVisitContact(name = "Name", telephone = "Telephone", email = "Email"),
          visitorSupport = PVVisitorSupport(description = "Description"),
          visitExternalSystemDetails =
            PVVistExternalSystemDetails(
              clientName = "client_name",
              clientVisitReference = "12345",
            ),
          applicationReference = "dfs-wjs-abc",
          reference = "dfs-wjs-abc",
          sessionTemplateReference = "dfs-wjs-xyz",
        ),
      )
    val futureVisitServiceResponse =
      listOf(
        Visit(
          prisonerId = "PrisonerId",
          prisonId = "MDI",
          prisonName = "Some Prison",
          visitRoom = "Room",
          visitType = "Type",
          visitStatus = "Status",
          visitSubStatus = "AUTO_APPROVED",
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
          visitExternalSystemDetails =
            VisitExternalSystemDetails(
              clientName = "client_name",
              clientVisitReference = "12345",
            ),
          applicationReference = "dfs-wjs-abc",
          reference = "dfs-wjs-abc",
          sessionTemplateReference = "dfs-wjs-xyz",
        ),
      )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(prisonVisitsGateway)

      whenever(getPersonService.getPersonWithPrisonFilter(nomisNumber, filters = null)).thenReturn(
        Response(data = person),
      )

      whenever(prisonVisitsGateway.getFutureVisits(nomisNumber)).thenReturn(
        Response(data = futureVisitGatewayResponse),
      )
    }

    it("returns a 200 status in the case of a successful query") {
      val response = getFutureVisitsService.execute(nomisNumber, filters = null)
      response.data.shouldBe(futureVisitServiceResponse)
      response.errors.shouldBeEmpty()
    }

    it("returns an errors if getPersonWithPrisonFilter returns an error") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISON_API, description = "Person with prison filters error"))
      whenever(getPersonService.getPersonWithPrisonFilter(nomisNumber, filters = null)).thenReturn(
        Response(data = null, errors = errors),
      )

      val response = getFutureVisitsService.execute(nomisNumber, filters = null)
      response.data.shouldBe(null)
      response.errors.shouldBe(errors)
    }

    it("returns a 404 if no Nomis number found on the person") {
      whenever(getPersonService.getPersonWithPrisonFilter(deliusCrn, filters = null)).thenReturn(
        Response(data = personWithoutNomisNumber),
      )

      val response = getFutureVisitsService.execute(deliusCrn, filters = null)
      response.data.shouldBe(null)
      response.errors.shouldBe(
        listOf(
          UpstreamApiError(
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            causedBy = UpstreamApi.PRISON_API,
            description = "No Nomis number found for $deliusCrn",
          ),
        ),
      )
    }

    it("returns an errors if gateway returns an error") {
      val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.MANAGE_PRISON_VISITS, description = "Gateway error"))
      whenever(prisonVisitsGateway.getFutureVisits(nomisNumber)).thenReturn(
        Response(data = null, errors = errors),
      )

      val response = getFutureVisitsService.execute(nomisNumber, filters = null)
      response.data.shouldBe(null)
      response.errors.shouldBe(errors)
    }
  })
