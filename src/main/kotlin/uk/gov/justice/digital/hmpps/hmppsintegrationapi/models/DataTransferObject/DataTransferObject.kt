package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.DataTransferObject

interface DataTransferObject<T> {
  fun toDomain(): T
}
