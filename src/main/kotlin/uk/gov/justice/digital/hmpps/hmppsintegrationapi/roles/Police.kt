package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role

val police =
  role("police") {
    permissions {
      -"/v1/persons/[^/]*$"
      -"/v1/persons/.*/addresses"
      -"/v1/pnd/persons/.*/alerts"
      -"/v1/persons/.*/sentences"
      -"/v1/persons/.*/sentences/latest-key-dates-and-adjustments"
      -"/v1/persons/.*/risks/serious-harm"
      -"/v1/persons/.*/risks/dynamic"
      -"/v1/persons/.*/risks/mappadetail"
      -"/v1/persons/.*/licences/conditions"
      -"/v1/persons/.*/person-responsible-officer"
      -"/v1/persons/.*/status-information"
      -"/v1/hmpps/reference-data"
      -"/v1/status"
    }
    redactionPolicies(
      listOf(
        laoRedactionPolicy,
      ),
    )
    filters {
      alertCodes {
        -"HA"
        -"HA2"
        -"HS"
        -"RCC"
        -"RCS"
        -"RDP"
        -"RDV"
        -"REG"
        -"RKC"
        -"RLG"
        -"ROP"
        -"RPB"
        -"RPC"
        -"RRV"
        -"RSS"
        -"RST"
        -"RTP"
        -"RYP"
        -"SC"
        -"XA"
        -"XC"
        -"XCA"
        -"XCI"
        -"XCO"
        -"XCOL"
        -"XCOP"
        -"XCOR"
        -"XEL"
        -"XELH"
        -"XER"
        -"XHT"
        -"XILLENT"
        -"XIS"
        -"XR"
        -"XRF"
        -"XSA"
      }
      supervisionStatuses {
        -"PROBATION"
      }
    }
  }
