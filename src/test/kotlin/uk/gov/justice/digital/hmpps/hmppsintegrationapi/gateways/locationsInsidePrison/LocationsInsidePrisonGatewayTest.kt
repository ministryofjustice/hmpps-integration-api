package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.locationsInsidePrison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [LocationsInsidePrisonGateway::class],
)
class LocationsInsidePrisonGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
) : DescribeSpec(
  {
    val locationsInsidePrisonApiMockServer = ApiMockServer.create(UpstreamApi.LOCATIONS_INSIDE_PRISON)

    beforeEach {
      locationsInsidePrisonApiMockServer.start()

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("LOCATIONS-INSIDE-PRISON")).thenReturn(
        HmppsAuthMockServer.TOKEN,
      )
    }

    afterTest {
      locationsInsidePrisonApiMockServer.stop()
    }

    describe("getLocationByKey") {
      val key = "123"
      val path = "/locations/key/$key"

      it("authenticates using HMPPS Auth with credentials") {
        locationsInsidePrisonGateway.getLocationByKey(key)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("LOCATIONS-INSIDE-PRISON")
      }

      it("returns location") {
        locationsInsidePrisonApiMockServer.stubForGet(
          path,
          """
          {
            "id": "2475f250-434a-4257-afe7-b911f1773a4d",
            "prisonId": "MDI",
            "code": "001",
            "pathHierarchy": "A-1-001",
            "locationType": "CELL",
            "localName": "Wing A",
            "comments": "Not to be used",
            "permanentlyInactive": false,
            "permanentlyInactiveReason": "Demolished",
            "capacity": {
              "maxCapacity": 2,
              "workingCapacity": 2
            },
            "oldWorkingCapacity": 1073741824,
            "certification": {
              "certified": true,
              "capacityOfCertifiedCell": 1
            },
            "usage": [
              {
                "usageType": "ADJUDICATION_HEARING",
                "capacity": 1073741824,
                "sequence": 1073741824
              }
            ],
            "accommodationTypes": [
              "CARE_AND_SEPARATION"
            ],
            "specialistCellTypes": [
              "ACCESSIBLE_CELL"
            ],
            "usedFor": [
              "CLOSE_SUPERVISION_CENTRE"
            ],
            "status": "ACTIVE",
            "convertedCellType": "HOLDING_ROOM",
            "otherConvertedCellType": "string",
            "active": true,
            "deactivatedByParent": false,
            "deactivatedDate": "2023-01-23T12:23:00",
            "deactivatedReason": "DAMAGED",
            "deactivationReasonDescription": "Window damage",
            "deactivatedBy": "string",
            "proposedReactivationDate": "2026-01-24",
            "planetFmReference": "2323/45M",
            "topLevelId": "57718979-573c-433a-9e51-2d83f887c11c",
            "level": 1,
            "leafLevel": false,
            "parentId": "57718979-573c-433a-9e51-2d83f887c11c",
            "parentLocation": "string",
            "inactiveCells": 1073741824,
            "numberOfCellLocations": 1073741824,
            "childLocations": [
              "string"
            ],
            "changeHistory": [
              {
                "transactionId": "019464e9-05da-77b3-810b-887e199d8190",
                "transactionType": "CAPACITY_CHANGE",
                "attribute": "Location Type",
                "oldValues": [
                  "Dry cell",
                  "Safe cell"
                ],
                "newValues": [
                  "Dry cell",
                  "Safe cell"
                ],
                "amendedBy": "user",
                "amendedDate": "2023-01-23T10:15:30"
              }
            ],
            "transactionHistory": [
              {
                "transactionId": "019464e9-05da-77b3-810b-887e199d8190",
                "transactionType": "CAPACITY_CHANGE",
                "prisonId": "MDI",
                "transactionDetail": "Working capacity changed from 0 to 1",
                "transactionInvokedBy": "STAFF_USER1",
                "txStartTime": "2025-04-25T09:34:24.374Z",
                "txEndTime": "2025-04-25T09:34:24.374Z",
                "transactionDetails": [
                  {
                    "locationId": "019483f5-fee7-7ed0-924c-3ee4b2b51904",
                    "locationKey": "BXI-1-1-001",
                    "attributeCode": "STATUS",
                    "attribute": "Location Type",
                    "amendedBy": "user",
                    "amendedDate": "2023-01-23T10:15:30",
                    "oldValues": [
                      "Dry cell",
                      "Safe cell"
                    ],
                    "newValues": [
                      "Dry cell",
                      "Safe cell"
                    ]
                  }
                ]
              }
            ],
            "lastModifiedBy": "string",
            "lastModifiedDate": "2025-04-25T09:34:24.374Z",
            "key": "MDI-A-1-001",
            "isResidential": true
          }
         """,
        )

        val result = locationsInsidePrisonGateway.getLocationByKey(key)
        result.data.shouldNotBeNull()
        result.data!!.id.shouldBe("2475f250-434a-4257-afe7-b911f1773a4d")
        result.data!!
          .usage
          .orEmpty()
          .size
          .shouldBe(1)
        result.data!!
          .changeHistory
          .orEmpty()
          .size
          .shouldBe(1)
        result.data!!
          .transactionHistory
          .orEmpty()
          .size
          .shouldBe(1)
      }

      it("should return bad request if bad request thrown") {
        locationsInsidePrisonApiMockServer.stubForGet(
          path,
          "",
          HttpStatus.BAD_REQUEST,
        )

        val result = locationsInsidePrisonGateway.getLocationByKey(key)
        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.BAD_REQUEST)))
      }
    }

    describe("getResidentialSummary") {
      val prisonId = "G6333VK"
      val path = "/locations/residential-summary/$prisonId"

      it("authenticates using HMPPS Auth with credentials") {
        locationsInsidePrisonGateway.getResidentialSummary(prisonId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("LOCATIONS-INSIDE-PRISON")
      }

      it("returns residential summary") {
        locationsInsidePrisonApiMockServer.stubForGet(
          path,
          """
            {
              "prisonSummary": {
                "prisonName": "string",
                "workingCapacity": 1073741824,
                "signedOperationalCapacity": 1073741824,
                "maxCapacity": 1073741824,
                "numberOfCellLocations": 1073741824
              },
              "topLevelLocationType": "Wings",
              "subLocationName": "Wings",
              "locationHierarchy": [
                {
                  "id": "c73e8ad1-191b-42b8-bfce-2550cc858dab",
                  "prisonId": "MDI",
                  "code": "001",
                  "type": "WING",
                  "localName": "Wing A",
                  "pathHierarchy": "A-1-001",
                  "level": 1
                }
              ],
              "parentLocation": {
                "id": "2475f250-434a-4257-afe7-b911f1773a4d",
                "prisonId": "MDI",
                "code": "001",
                "pathHierarchy": "A-1-001",
                "locationType": "CELL",
                "localName": "Wing A",
                "comments": "Not to be used",
                "permanentlyInactive": false,
                "permanentlyInactiveReason": "Demolished",
                "capacity": {
                  "maxCapacity": 2,
                  "workingCapacity": 2
                },
                "oldWorkingCapacity": 1073741824,
                "certification": {
                  "certified": true,
                  "capacityOfCertifiedCell": 1
                },
                "usage": [
                  {
                    "usageType": "ADJUDICATION_HEARING",
                    "capacity": 1073741824,
                    "sequence": 1073741824
                  }
                ],
                "accommodationTypes": [
                  "CARE_AND_SEPARATION"
                ],
                "specialistCellTypes": [
                  "ACCESSIBLE_CELL"
                ],
                "usedFor": [
                  "CLOSE_SUPERVISION_CENTRE"
                ],
                "status": "ACTIVE",
                "locked": false,
                "convertedCellType": "HOLDING_ROOM",
                "otherConvertedCellType": "string",
                "inCellSanitation": true,
                "deactivatedByParent": false,
                "deactivatedDate": "2023-01-23T12:23:00",
                "deactivatedReason": "DAMAGED",
                "deactivationReasonDescription": "Window damage",
                "deactivatedBy": "string",
                "proposedReactivationDate": "2026-01-24",
                "planetFmReference": "2323/45M",
                "topLevelId": "57718979-573c-433a-9e51-2d83f887c11c",
                "level": 1,
                "leafLevel": false,
                "parentId": "57718979-573c-433a-9e51-2d83f887c11c",
                "parentLocation": "string",
                "inactiveCells": 1073741824,
                "numberOfCellLocations": 1073741824,
                "childLocations": [
                  "string"
                ],
                "changeHistory": [
                  {
                    "transactionId": "019464e9-05da-77b3-810b-887e199d8190",
                    "transactionType": "CAPACITY_CHANGE",
                    "attribute": "Location Type",
                    "oldValues": [
                      "Dry cell",
                      "Safe cell"
                    ],
                    "newValues": [
                      "Dry cell",
                      "Safe cell"
                    ],
                    "amendedBy": "user",
                    "amendedDate": "2023-01-23T10:15:30"
                  }
                ],
                "transactionHistory": [
                  {
                    "transactionId": "019464e9-05da-77b3-810b-887e199d8190",
                    "transactionType": "CAPACITY_CHANGE",
                    "prisonId": "MDI",
                    "transactionDetail": "Working capacity changed from 0 to 1",
                    "transactionInvokedBy": "STAFF_USER1",
                    "txStartTime": "2025-04-25T13:39:56.692Z",
                    "txEndTime": "2025-04-25T13:39:56.692Z",
                    "transactionDetails": [
                      {
                        "locationId": "019483f5-fee7-7ed0-924c-3ee4b2b51904",
                        "locationKey": "BXI-1-1-001",
                        "attributeCode": "STATUS",
                        "attribute": "Location Type",
                        "amendedBy": "user",
                        "amendedDate": "2023-01-23T10:15:30",
                        "oldValues": [
                          "Dry cell",
                          "Safe cell"
                        ],
                        "newValues": [
                          "Dry cell",
                          "Safe cell"
                        ]
                      }
                    ]
                  }
                ],
                "lastModifiedBy": "string",
                "lastModifiedDate": "2025-04-25T13:39:56.692Z",
                "key": "MDI-A-1-001",
                "isResidential": true
              },
              "subLocations": [
                {
                  "id": "2475f250-434a-4257-afe7-b911f1773a4d",
                  "prisonId": "MDI",
                  "code": "001",
                  "pathHierarchy": "A-1-001",
                  "locationType": "CELL",
                  "localName": "Wing A",
                  "comments": "Not to be used",
                  "permanentlyInactive": false,
                  "permanentlyInactiveReason": "Demolished",
                  "capacity": {
                    "maxCapacity": 2,
                    "workingCapacity": 2
                  },
                  "oldWorkingCapacity": 1073741824,
                  "certification": {
                    "certified": true,
                    "capacityOfCertifiedCell": 1
                  },
                  "usage": [
                    {
                      "usageType": "ADJUDICATION_HEARING",
                      "capacity": 1073741824,
                      "sequence": 1073741824
                    }
                  ],
                  "accommodationTypes": [
                    "CARE_AND_SEPARATION"
                  ],
                  "specialistCellTypes": [
                    "ACCESSIBLE_CELL"
                  ],
                  "usedFor": [
                    "CLOSE_SUPERVISION_CENTRE"
                  ],
                  "status": "ACTIVE",
                  "locked": false,
                  "convertedCellType": "HOLDING_ROOM",
                  "otherConvertedCellType": "string",
                  "inCellSanitation": true,
                  "deactivatedByParent": false,
                  "deactivatedDate": "2023-01-23T12:23:00",
                  "deactivatedReason": "DAMAGED",
                  "deactivationReasonDescription": "Window damage",
                  "deactivatedBy": "string",
                  "proposedReactivationDate": "2026-01-24",
                  "planetFmReference": "2323/45M",
                  "topLevelId": "57718979-573c-433a-9e51-2d83f887c11c",
                  "level": 1,
                  "leafLevel": false,
                  "parentId": "57718979-573c-433a-9e51-2d83f887c11c",
                  "parentLocation": "string",
                  "inactiveCells": 1073741824,
                  "numberOfCellLocations": 1073741824,
                  "childLocations": [
                    "string"
                  ],
                  "changeHistory": [
                    {
                      "transactionId": "019464e9-05da-77b3-810b-887e199d8190",
                      "transactionType": "CAPACITY_CHANGE",
                      "attribute": "Location Type",
                      "oldValues": [
                        "Dry cell",
                        "Safe cell"
                      ],
                      "newValues": [
                        "Dry cell",
                        "Safe cell"
                      ],
                      "amendedBy": "user",
                      "amendedDate": "2023-01-23T10:15:30"
                    }
                  ],
                  "transactionHistory": [
                    {
                      "transactionId": "019464e9-05da-77b3-810b-887e199d8190",
                      "transactionType": "CAPACITY_CHANGE",
                      "prisonId": "MDI",
                      "transactionDetail": "Working capacity changed from 0 to 1",
                      "transactionInvokedBy": "STAFF_USER1",
                      "txStartTime": "2025-04-25T13:39:56.692Z",
                      "txEndTime": "2025-04-25T13:39:56.693Z",
                      "transactionDetails": [
                        {
                          "locationId": "019483f5-fee7-7ed0-924c-3ee4b2b51904",
                          "locationKey": "BXI-1-1-001",
                          "attributeCode": "STATUS",
                          "attribute": "Location Type",
                          "amendedBy": "user",
                          "amendedDate": "2023-01-23T10:15:30",
                          "oldValues": [
                            "Dry cell",
                            "Safe cell"
                          ],
                          "newValues": [
                            "Dry cell",
                            "Safe cell"
                          ]
                        }
                      ]
                    }
                  ],
                  "lastModifiedBy": "string",
                  "lastModifiedDate": "2025-04-25T13:39:56.693Z",
                  "key": "MDI-A-1-001",
                  "isResidential": true
                }
              ]
            }
            """,
        )
        val result = locationsInsidePrisonGateway.getResidentialSummary(prisonId)

        result.data.shouldNotBeNull()
        result.data!!
          .prisonSummary!!
          .workingCapacity
          .shouldBe(1073741824)
        result.data!!
          .subLocations.size
          .shouldBe(1)
        result.data!!.parentLocation.shouldNotBeNull()
        result.data!!.topLevelLocationType.shouldBe("Wings")
      }

      it("should return bad request if bad request thrown") {
        locationsInsidePrisonApiMockServer.stubForGet(
          path,
          "",
          HttpStatus.BAD_REQUEST,
        )

        val result = locationsInsidePrisonGateway.getResidentialSummary(prisonId)
        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.BAD_REQUEST)))
      }
    }

    describe("getResidentialHierarchy") {
      val prisonId = "G6333VK"
      val path = "/locations/prison/$prisonId/residential-hierarchy"

      it("authenticates using HMPPS Auth with credentials") {
        locationsInsidePrisonGateway.getResidentialHierarchy(prisonId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("LOCATIONS-INSIDE-PRISON")
      }

      it("should get residential hierarchy") {
        locationsInsidePrisonApiMockServer.stubForGet(
          path,
          """
          [
              {
                  "locationId": "01951953-2f87-7f26-a803-e8157a95e5b5",
                  "locationType": "WING",
                  "locationCode": "A",
                  "fullLocationPath": "A",
                  "localName": "Mki-a",
                  "level": 1,
                  "subLocations": [
                      {
                          "locationId": "01951953-8044-7ebb-ba4e-1d4116e20fda",
                          "locationType": "LANDING",
                          "locationCode": "1",
                          "fullLocationPath": "A-1",
                          "localName": "Mki-a-1",
                          "level": 2,
                          "subLocations": [
                              {
                                  "locationId": "0195195c-711a-7c13-869f-1347b27979e1",
                                  "locationType": "CELL",
                                  "locationCode": "03",
                                  "fullLocationPath": "A-1-03",
                                  "level": 3
                              }
                          ]
                      }
                  ]
              }
          ]
          """.trimIndent()
        )

        val result = locationsInsidePrisonGateway.getResidentialHierarchy(prisonId)
        result.data.shouldNotBeNull()
        val topLevelLocations = result.data.orEmpty()
        topLevelLocations.size.shouldBe(1)
        topLevelLocations[0].locationId.shouldBe("01951953-2f87-7f26-a803-e8157a95e5b5")
        topLevelLocations[0].locationType.shouldBe("WING")
        topLevelLocations[0].level.shouldBe(1)
        val secondLevelLocations = topLevelLocations[0].subLocations.orEmpty()
        secondLevelLocations.size.shouldBe(1)
        secondLevelLocations[0].locationType.shouldBe("LANDING")
        secondLevelLocations[0].locationId.shouldBe("01951953-8044-7ebb-ba4e-1d4116e20fda")
        secondLevelLocations[0].level.shouldBe(2)
        val thirdLevelLocations = secondLevelLocations[0].subLocations.orEmpty()
        thirdLevelLocations.size.shouldBe(1)
        thirdLevelLocations[0].locationId.shouldBe("0195195c-711a-7c13-869f-1347b27979e1")
        thirdLevelLocations[0].locationType.shouldBe("CELL")
        thirdLevelLocations[0].level.shouldBe(3)
      }

      it("should return bad request if bad request thrown") {
        locationsInsidePrisonApiMockServer.stubForGet(
          path,
          "",
          HttpStatus.BAD_REQUEST,
        )

        val result = locationsInsidePrisonGateway.getResidentialHierarchy(prisonId)
        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.BAD_REQUEST)))
      }
    }
  })
