package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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
      val size = 10
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
          size = size,
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
              pageSize = size,
              paged = true,
              pageNumber = page,
            ),
          empty = false,
        )

      beforeEach {
        Mockito.reset(prisonerAlertsGateway)

        whenever(personService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
        whenever(prisonerAlertsGateway.getPrisonerAlerts(hmppsId, page, size)).thenReturn(
          Response(
            data = getPaginatedAlerts(listOf(alert, nonMatchingAlert)),
          ),
        )
      }

      it("gets a nomis number from getPersonService") {
        getAlertsForPersonService.execute(hmppsId, filters, page, size)

        verify(personService, times(1)).getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)
      }

      it("gets alerts from NOMIS using a prisoner number") {
        getAlertsForPersonService.execute(hmppsId, filters, page, size)

        verify(prisonerAlertsGateway, times(1)).getPrisonerAlerts(hmppsId, page, size)
      }

      describe("when an upstream API returns an error when looking up nomis number by a Hmmps Id") {
        it("records upstream API errors when failed prison check call") {
          val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
          whenever(personService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(
            Response(
              data = null,
              errors = err,
            ),
          )

          val response = getAlertsForPersonService.execute(hmppsId, filters, page, size)
          response.errors.shouldHaveSize(1)
          response.errors.shouldBe(err)
        }

        it("failed to get prisoners nomis number") {
          val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(personService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(), errors = emptyList()))

          val response = getAlertsForPersonService.execute(hmppsId, filters, page, size)
          response.errors.shouldBe(err)
        }

        it("does not get alerts from Nomis") {
          whenever(personService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))))

          getAlertsForPersonService.execute(hmppsId, filters, page, size)
          verify(prisonerAlertsGateway, times(0)).getPrisonerAlerts(hmppsId, page, size)
        }
      }

      it("records errors when it prisoner alerts gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_ALERTS,
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              description = "Prisoner alerts error",
            ),
          )
        whenever(prisonerAlertsGateway.getPrisonerAlerts(hmppsId, page, size)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val response = getAlertsForPersonService.execute(hmppsId, filters, page, size)
        response.errors.shouldBe(errors)
      }

      describe("getAlertsForPnd") {
        it("returns PND filtered data") {
          val response = getAlertsForPersonService.execute(hmppsId, filters, page, size, pndOnly = true)
          response.data?.content.shouldBe(alert)
        }

        it("returns an error when the alert code is not in the allowed list") {
          whenever(prisonerAlertsGateway.getPrisonerAlerts(hmppsId, page, size)).thenReturn(
            Response(
              data = getPaginatedAlerts(listOf(nonMatchingAlert)),
            ),
          )

          val response = getAlertsForPersonService.execute(hmppsId, filters, page, size, pndOnly = true)
          response.data.shouldBeNull()
          response.errors.shouldHaveSize(1)
        }
      }
    },
  )
