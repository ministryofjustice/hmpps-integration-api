package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PersonLicences(
  @Schema(example = "2008/0545166T")
  val hmppsId: String,
  @Schema(example = "Z1234ZZ")
  val offenderNumber: String? = null,
  @Schema(
    example = """
    [
      {
        "status": "IN_PROGRESS",
        "typeCode": "AP",
        "createdDate": "2015-09-23",
        "approvedDate": "2015-09-24",
        "updatedDate": "2015-10-23",
        "conditions": [
          {
            "type": "Standard",
            "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
            "category": "Residence at a specific place",
            "condition": "You must reside at an approved address"
          },
          {
            "type": "Custom",
            "code": "b457e9c3-68c6-4f5e-9bd8-c36c5b7e70fb",
            "category": "Contact Restrictions",
            "condition": "You must not contact the victim"
          }
        ]
      },
      {
        "status": "SUBMITTED",
        "typeCode": "PSS",
        "createdDate": "2016-01-15",
        "approvedDate": "2016-01-16",
        "updatedDate": "2016-02-15",
        "conditions": [
          {
            "type": "Standard",
            "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
            "category": "Residence at a specific place",
            "condition": "You must reside at an approved address"
          }
        ]
      },
      {
        "status": "APPROVED",
        "typeCode": "PSS",
        "createdDate": "2016-01-15",
        "approvedDate": "2016-01-16",
        "updatedDate": "2016-02-15",
        "conditions": [
          {
            "type": "Standard",
            "code": "5a105297-dce1-4d18-b9ea-4195b46b7594",
            "category": "Residence at a specific place",
            "condition": "You must reside at an approved address"
          }
        ]
      },
      {
        "status": "ACTIVE",
        "typeCode": "AP",
        "createdDate": "2018-05-20",
        "approvedDate": "2018-05-21",
        "updatedDate": "2018-06-20",
        "conditions": [
          {
            "type": "Custom",
            "code": "c97f33e3-1e50-4b28-8f71-5de3e2fbbf79",
            "category": "Travel Restrictions",
            "condition": "You must not leave the country without permission"
          }
        ]
      },
      {
        "status": "VARIATION_IN_PROGRESS",
        "typeCode": "AP",
        "createdDate": "2019-07-01",
        "approvedDate": "2019-07-02",
        "updatedDate": "2019-08-01",
        "conditions": [
          {
            "type": "Rehabilitation",
            "code": "a5371dd7-7f54-42b9-9c3b-35f9e8f6f1e9",
            "category": "Rehabilitation Program",
            "condition": "You must participate in a rehabilitation program"
          }
        ]
      },
      {
        "status": "VARIATION_SUBMITTED",
        "typeCode": "AP_PSS",
        "createdDate": "2020-09-12",
        "approvedDate": "2020-09-13",
        "updatedDate": "2020-10-12",
        "conditions": [
          {
            "type": "Monitoring",
            "code": "e4a8b9cf-4a50-44a2-8a0c-5f38d2fae6f9",
            "category": "Electronic Monitoring",
            "condition": "You must comply with electronic monitoring"
          }
        ]
      },
      {
        "status": "VARIATION_APPROVED",
        "typeCode": "AP_PSS",
        "createdDate": "2021-11-23",
        "approvedDate": "2021-11-24",
        "updatedDate": "2021-12-23",
        "conditions": [
          {
            "type": "Substance Abuse",
            "code": "d4b8b1e8-9e4e-4f6a-a2df-b41e8f8e8e8e",
            "category": "Alcohol Restrictions",
            "condition": "You must not consume alcohol"
          }
        ]
      },
      {
        "status": "VARIATION_REJECTED",
        "typeCode": "PSS",
        "createdDate": "2022-01-01",
        "approvedDate": "2022-01-02",
        "updatedDate": "2022-02-01",
        "conditions": [
          {
            "type": "Association",
            "code": "f9e8f1c8-2f4a-4a6a-a1de-5e7e9f7d9e8f",
            "category": "Contact Restrictions",
            "condition": "You must not associate with known criminals"
          }
        ]
      }
    ]
  """,
  )
  val licences: List<Licence> = emptyList(),
)
