package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerAlertsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAAlert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAAlertCode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAPaginatedAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PASort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPndAlerts
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAlertsForPersonService::class],
)
internal class GetAlertsForPersonServiceTest(
  @MockitoBean val prisonerAlertsGateway: PrisonerAlertsGateway,
  @MockitoBean val personService: GetPersonService,
  private val getAlertsForPersonService: GetAlertsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val filters = ConsumerFilters(null)
      val page = 1
      val perPage = 10
      val alert =
        PAAlert(
          alertUuid = UUID.randomUUID().toString(),
          prisonNumber = hmppsId,
          alertCode =
            PAAlertCode(
              alertTypeCode = "X",
              alertTypeDescription = "X",
              code = "XA",
              description = "Test Alert XA",
            ),
          activeFrom = LocalDate.now(),
          createdAt = LocalDateTime.now(),
          createdBy = "test_user",
          createdByDisplayName = "Test User",
          isActive = true,
        )
      val nonMatchingAlert =
        PAAlert(
          alertUuid = UUID.randomUUID().toString(),
          prisonNumber = hmppsId,
          alertCode =
            PAAlertCode(
              alertTypeCode = "X",
              alertTypeDescription = "X",
              code = "INVALID",
              description = "Invalid Alert",
            ),
          activeFrom = LocalDate.now(),
          createdAt = LocalDateTime.now(),
          createdBy = "test_user",
          createdByDisplayName = "Test User",
          isActive = true,
        )

      fun getPaginatedAlerts(alerts: List<PAAlert>) =
        PAPaginatedAlerts(
          content = alerts,
          totalElements = alerts.size.toLong(),
          totalPages = 1,
          first = true,
          last = true,
          size = perPage,
          number = page,
          sort =
            PASort(
              empty = false,
              sorted = false,
              unsorted = true,
            ),
          numberOfElements = alerts.size,
          pageable =
            PAPageable(
              offset = alerts.size.toLong(),
              sort =
                PASort(
                  empty = false,
                  sorted = false,
                  unsorted = true,
                ),
              unpaged = false,
              pageSize = perPage,
              paged = true,
              pageNumber = page,
            ),
          empty = false,
        )

      val paginatedAlerts = getPaginatedAlerts(listOf(alert, nonMatchingAlert))

      beforeEach {

        mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        every { roles["full-access"] } returns testRoleWithPndAlerts

        Mockito.reset(prisonerAlertsGateway)
        Mockito.reset(personService)

        whenever(personService.getNomisNumberWithFiltering(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
        whenever(prisonerAlertsGateway.getPrisonerAlertsForCodes(hmppsId, page, perPage)).thenReturn(
          Response(
            data = paginatedAlerts,
          ),
        )
        whenever(prisonerAlertsGateway.getPrisonerAlertsForCodes(hmppsId, page, perPage)).thenReturn(
          Response(
            data = paginatedAlerts,
          ),
        )
      }

      afterEach {
        unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
      }

      it("gets a nomis number from getPersonService") {
        getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)

        verify(personService, times(1)).getNomisNumberWithFiltering(hmppsId = hmppsId, filters)
      }

      it("gets alerts using a prisoner number") {
        val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
        verify(prisonerAlertsGateway, times(1)).getPrisonerAlertsForCodes(hmppsId, page, perPage, emptyList())
        response.data.shouldBe(paginatedAlerts.toPaginatedAlertsFilterApplied())
      }

      it("gets a nomis number from getPersonService - getAlerts") {
        getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)

        verify(personService, times(1)).getNomisNumberWithFiltering(hmppsId = hmppsId, filters)
      }

      it("gets alerts using a prisoner number - getAlerts") {
        val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
        verify(prisonerAlertsGateway, times(1)).getPrisonerAlertsForCodes(hmppsId, page, perPage)
        response.data.shouldBe(paginatedAlerts.toPaginatedAlertsFilterApplied())
      }

      describe("when an upstream API returns an error when looking up nomis number by a Hmmps Id") {
        it("records upstream API errors when failed prison check call") {
          val errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
          whenever(personService.getNomisNumberWithFiltering(hmppsId = hmppsId, filters = filters)).thenReturn(
            Response(
              data = null,
              errors = errors,
            ),
          )

          val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          response.errors.shouldBe(errors)
        }

        it("failed to get prisoners nomis number") {
          val errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(personService.getNomisNumberWithFiltering(hmppsId, filters)).thenReturn(Response(data = NomisNumber(), errors = emptyList()))

          val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          response.errors.shouldBe(errors)
        }

        it("does not get alerts from prison alerts gateway") {
          whenever(personService.getNomisNumberWithFiltering(hmppsId, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))))

          getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          verify(prisonerAlertsGateway, times(0)).getPrisonerAlertsForCodes(hmppsId, page, perPage)
        }
      }

      describe("when an upstream API returns an error when looking up nomis number by a Hmmps Id - getAlerts") {
        it("records upstream API errors when failed prison check call") {
          val errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
          whenever(personService.getNomisNumberWithFiltering(hmppsId = hmppsId, filters = filters)).thenReturn(
            Response(
              data = null,
              errors = errors,
            ),
          )

          val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          response.errors.shouldBe(errors)
        }

        it("failed to get prisoners nomis number") {
          val errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(personService.getNomisNumberWithFiltering(hmppsId, filters)).thenReturn(Response(data = NomisNumber(), errors = emptyList()))

          val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          response.errors.shouldBe(errors)
        }

        it("does not get alerts from prison alerts gateway") {
          whenever(personService.getNomisNumberWithFiltering(hmppsId, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))))

          getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          verify(prisonerAlertsGateway, times(0)).getPrisonerAlertsForCodes(hmppsId, page, perPage)
        }
      }

      it("records errors when prisoner alerts gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_ALERTS,
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              description = "Prisoner alerts error",
            ),
          )
        whenever(prisonerAlertsGateway.getPrisonerAlertsForCodes(hmppsId, page, perPage)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val response = getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
        response.errors.shouldBe(errors)
      }

      describe("getAlertsForPnd") {

        it("calls the gateway with a list of alertCodes") {
          val filters = ConsumerFilters(alertCodes = testRoleWithPndAlerts.filters?.alertCodes)
          whenever(prisonerAlertsGateway.getPrisonerAlertsForCodes(hmppsId, page, perPage, filters.alertCodes!!)).thenReturn(
            Response(
              data = paginatedAlerts,
            ),
          )
          whenever(personService.getNomisNumberWithFiltering(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
          getAlertsForPersonService.getAlerts(hmppsId, ConsumerFilters(alertCodes = testRoleWithPndAlerts.filters?.alertCodes), page, perPage)
          verify(prisonerAlertsGateway, times(1)).getPrisonerAlertsForCodes(hmppsId, page, perPage, testRoleWithPndAlerts.filters?.alertCodes!!)
        }

        it("calls the gateway without a list of alertCodes") {
          getAlertsForPersonService.getAlerts(hmppsId, filters, page, perPage)
          verify(prisonerAlertsGateway, times(1)).getPrisonerAlertsForCodes(hmppsId, page, perPage, emptyList())
        }
      }
    },
  )
