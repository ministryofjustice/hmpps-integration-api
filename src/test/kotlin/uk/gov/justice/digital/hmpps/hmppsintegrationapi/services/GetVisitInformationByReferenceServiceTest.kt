package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVistExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitInformationByReferenceService::class],
)
class GetVisitInformationByReferenceServiceTest(
  @Autowired val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val prisonVisitsGateway: PrisonVisitsGateway,
) : DescribeSpec({
    val visitReference = "123456"
    val visitResponse =
      PVVisit(
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
      )

    beforeEach {
      Mockito.reset(consumerPrisonAccessService)
      Mockito.reset(prisonVisitsGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Visit>("MDI", null, UpstreamApi.MANAGE_PRISON_VISITS)).thenReturn(
        Response(data = null),
      )

      whenever(prisonVisitsGateway.getVisitByReference(visitReference)).thenReturn(
        Response(data = visitResponse),
      )
    }

    it("returns a 404 not found status in the case of not finding a visit by reference") {
      whenever(prisonVisitsGateway.getVisitByReference(visitReference)).thenReturn(
        Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Visit not found"))),
      )
      val response = getVisitInformationByReferenceService.execute(visitReference)

      response.errors.isNotEmpty()
      response.errors.contains(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Visit not found"))
    }

    it("returns a 404 not found status in the case of the prisonId returned by the visit query not being in the consumers profile") {
      val filters = ConsumerFilters(prisons = listOf("WRONG-PRISON-BRO"))
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Visit>("MDI", filters, UpstreamApi.MANAGE_PRISON_VISITS)).thenReturn(
        Response(data = null),
      )
      val response = getVisitInformationByReferenceService.execute(visitReference, filters)

      response.errors.isNotEmpty()
      response.errors.contains(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Visit not found"))
    }

    it("returns a 500 in the case of a failed visit query") {
      whenever(prisonVisitsGateway.getVisitByReference(visitReference)).thenReturn(
        Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR))),
      )

      val response = getVisitInformationByReferenceService.execute(visitReference)
      response.errors.isNotEmpty()
      response.errors.contains(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
    }

    it("returns a 200 status in the case of a successful visit query") {
      val response = getVisitInformationByReferenceService.execute(visitReference)

      response.data.shouldBe(visitResponse.toVisit())
    }
  })
