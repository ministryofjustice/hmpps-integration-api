package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

data class NomisPersonAccount(val cash: Number? = null,
                              val currency: String? = null,
                              val damageObligations: Number? = null,
                              val savings: Number? = null,
                              val spends: Number? = null,
                              val nomisNumber: String? = null,
                              val hmppsId: String? = null)
