package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class SearchAppointmentsService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    appointmentSearchRequest: AppointmentSearchRequest,
    filters: ConsumerFilters?,
  ): Response<List<AppointmentDetails>?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<AppointmentDetails>>(prisonId, filters, UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val appointmentResponse = activitiesGateway.getAppointments(prisonId, appointmentSearchRequest)
    if (appointmentResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = appointmentResponse.errors,
      )
    }
    return Response(data = appointmentResponse.data?.map { it.toAppointmentDetails() }, errors = appointmentResponse.errors)
  }
}
