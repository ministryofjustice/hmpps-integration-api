package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class AppointmentDetails(
  val appointmentId: Long,
  val appointmentType: String,
  val prisonId: String,
  val appointmentName: String,
  val attendees: List<Attendee>,
  val category: AppointmentCategory,
  val customName: String?,
  val internalLocation: InternalLocation?,
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
)
