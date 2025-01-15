package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAlert
import java.time.LocalDate

class AlertTest :
  DescribeSpec(
    {
      describe("#toAlert") {
        it("maps one-to-one attributes to integration API attributes") {
          val alertFromNomis =
            NomisAlert(
              offenderNo = "A7777ZZ",
              alertType = "X",
              alertTypeDescription = "Security",
              alertCode = "XNR",
              alertCodeDescription = "Not For Release",
              comment = "IS91",
              dateCreated = LocalDate.parse("2022-08-01"),
              dateExpires = LocalDate.parse("2023-08-01"),
              expired = true,
              active = false,
            )

          val integrationApiAlert = alertFromNomis.toAlert()

          integrationApiAlert.shouldBe(
            Alert(
              offenderNo = alertFromNomis.offenderNo,
              type = alertFromNomis.alertType,
              typeDescription = alertFromNomis.alertTypeDescription,
              code = alertFromNomis.alertCode,
              codeDescription = alertFromNomis.alertCodeDescription,
              comment = alertFromNomis.comment,
              dateCreated = alertFromNomis.dateCreated,
              dateExpired = alertFromNomis.dateExpires,
              expired = alertFromNomis.expired,
              active = alertFromNomis.active,
            ),
          )
        }

        it("maps case where dateExpires is not populated") {
          val alertFromNomis =
            NomisAlert(
              offenderNo = "A7777ZZ",
              alertType = "X",
              alertTypeDescription = "Security",
              alertCode = "XNR",
              alertCodeDescription = "Not For Release",
              comment = "IS91",
              dateCreated = LocalDate.parse("2022-08-01"),
              expired = false,
              active = true,
            )

          val integrationApiAlert = alertFromNomis.toAlert()

          integrationApiAlert.shouldBe(
            Alert(
              offenderNo = alertFromNomis.offenderNo,
              type = alertFromNomis.alertType,
              typeDescription = alertFromNomis.alertTypeDescription,
              code = alertFromNomis.alertCode,
              codeDescription = alertFromNomis.alertCodeDescription,
              comment = alertFromNomis.comment,
              dateCreated = alertFromNomis.dateCreated,
              dateExpired = null,
              expired = alertFromNomis.expired,
              active = alertFromNomis.active,
            ),
          )
        }

        it("deals with NULL values") {
          val integrationApiAlert = NomisAlert().toAlert()

          integrationApiAlert.shouldBe(
            Alert(
              offenderNo = null,
              type = null,
              typeDescription = null,
              code = null,
              codeDescription = null,
              comment = null,
              dateCreated = null,
              dateExpired = null,
              expired = null,
              active = null,
            ),
          )
        }
      }
    },
  )
