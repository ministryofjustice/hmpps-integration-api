package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentDetails

data class ActivitiesAppointmentDetails(
  val appointmentSeriesId: Long,
  val appointmentId: Long,
  val appointmentType: String,
  val prisonCode: String,
  val appointmentName: String,
  val attendees: List<ActivitiesAttendee>,
  val category: ActivitiesAppointmentCategory,
  val customName: String?,
  val internalLocation: ActivitiesAppointmentInternalLocation?,
  val inCell: Boolean,
  val startDate: String,
  val startTime: String,
  val endTime: String?,
  val timeSlot: String,
  val isRepeat: Boolean,
  val sequenceNumber: Int,
  val maxSequenceNumber: Int,
  val isEdited: Boolean,
  val isCancelled: Boolean,
  val isExpired: Boolean,
  val createdTime: String,
  val updatedTime: String?,
  val cancelledTime: String?,
  val cancelledBy: String?,
) {
  fun toAppointmentDetails() =
    AppointmentDetails(
      appointmentId = this.appointmentId,
      appointmentType = this.appointmentType,
      prisonId = this.prisonCode,
      appointmentName = this.appointmentName,
      attendees = this.attendees.map { it.toAttendee() },
      category = this.category.toAppointmentCategory(),
      customName = this.customName,
      internalLocation = this.internalLocation?.toInternalLocation(),
      inCell = this.inCell,
      startDate = this.startDate,
      startTime = this.startTime,
      endTime = this.endTime,
      timeSlot = this.timeSlot,
      isRepeat = this.isRepeat,
      sequenceNumber = this.sequenceNumber,
      maxSequenceNumber = this.maxSequenceNumber,
      isEdited = this.isEdited,
      isCancelled = this.isCancelled,
      isExpired = this.isExpired,
    )
}
