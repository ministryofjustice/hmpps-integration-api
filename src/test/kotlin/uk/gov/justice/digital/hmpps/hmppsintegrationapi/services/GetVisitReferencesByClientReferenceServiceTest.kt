package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitReferences
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitReferencesByClientReferenceService::class],
)
class GetVisitReferencesByClientReferenceServiceTest(
  @Autowired val getVisitReferencesByClientReferenceService: GetVisitReferencesByClientReferenceService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val prisonVisitsGateway: PrisonVisitsGateway,
) : DescribeSpec({
    describe("GetVisitReferencesByClientReferenceService") {
      val clientReference = "client-ref-123"
      val filters = null
      val visitExternalSystemDetails = PVVistExternalSystemDetails(clientName = "BOB", clientVisitReference = "ABCABC")
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
          applicationReference = "dfs-wjs-abc",
          reference = "dfs-wjs-abc",
          sessionTemplateReference = "dfs-wjs-xyz",
          visitExternalSystemDetails = visitExternalSystemDetails,
        )
      val visitReferences = VisitReferences(listOf("visit-ref1", "visit-ref2"))

      beforeEach {
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Visit>(visitResponse.prisonId, null, UpstreamApi.MANAGE_PRISON_VISITS)).thenReturn(Response(data = null))
        whenever(prisonVisitsGateway.getVisitByReference(any())).thenReturn(Response(data = visitResponse))
      }

      it("returns visit references when found") {
        whenever(prisonVisitsGateway.getVisitReferencesByClientReference(clientReference)).thenReturn(Response(data = visitReferences))

        val result = getVisitReferencesByClientReferenceService.execute(clientReference, filters)

        result.data.shouldBe(visitReferences)
      }

      it("returns empty list when no visit references found") {
        whenever(prisonVisitsGateway.getVisitReferencesByClientReference(clientReference)).thenReturn(Response(data = VisitReferences(listOf())))

        val result = getVisitReferencesByClientReferenceService.execute(clientReference, filters)

        result.data!!.visitReferences.shouldBeEmpty()
      }

      it("returns errors when a visit is in a prison not available to the consumer") {
        whenever(prisonVisitsGateway.getVisitReferencesByClientReference(clientReference)).thenReturn(Response(data = visitReferences))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Visit>(visitResponse.prisonId, ConsumerFilters(listOf("IDM")), UpstreamApi.MANAGE_PRISON_VISITS)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))))

        val result = getVisitReferencesByClientReferenceService.execute(clientReference)

        result.errors.isNotEmpty()
      }
    }
  })
