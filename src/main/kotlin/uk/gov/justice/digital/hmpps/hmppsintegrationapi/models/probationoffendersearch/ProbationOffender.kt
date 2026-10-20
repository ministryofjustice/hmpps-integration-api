package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

interface ProbationOffender {
  fun deliusCrn(): String?

  fun redacted(): Any
}
