package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PLPIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns a persons integration schedule`() {
    callApi("$basePath/K5995YZ/plp-induction-schedule")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
           {"data": {
            "deadlineDate":"2019-08-24",
            "status":"SCHEDULED",
            "calculationRule": "NEW_PRISON_ADMISSION",
            "nomisNumber": "A1234BC",
            "systemUpdatedBy":"Alex Smith",
            "systemUpdatedAt":"2023-06-19T09:39:44Z"}}
      """,
        ),
      )
  }


  @Test
  fun `returns a persons review schedule`() {
    callApi("$basePath/K5995YZ/plp-review-schedule")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
           [
        {
        "reference": "91130fff-d6ce-4ead-8461-1507140a2ae0",
        "reviewDateFrom": "2024-11-11",
        "reviewDateTo": "2025-01-11",
        "status": "COMPLETED",
        "calculationRule": "BETWEEN_12_AND_60_MONTHS_TO_SERVE",
        "createdBy": "auser_gen",
        "createdByDisplayName": "Albert User",
        "createdAt": "2024-12-11T18:40:39.268Z",
        "createdAtPrison": "BXI",
        "updatedBy": "auser_gen",
        "updatedByDisplayName": "Albert User",
        "updatedAt": "2024-12-11T18:40:38.268Z",
        "updatedAtPrison": "BXI",
        "version": 4
        },
        {
        "reference": "91130fff-d6ce-4ead-8461-1507140a2ae0",
        "reviewDateFrom": "2024-11-11",
        "reviewDateTo": "2025-01-11",
        "status": "SCHEDULED",
        "calculationRule": "BETWEEN_12_AND_60_MONTHS_TO_SERVE",
        "createdBy": "auser_gen",
        "createdByDisplayName": "Albert User",
        "createdAt": "2024-12-11T18:40:39.256Z",
        "createdAtPrison": "BXI",
        "updatedBy": "auser_gen",
        "updatedByDisplayName": "Albert User",
        "updatedAt": "2024-12-11T18:40:36.256Z",
        "updatedAtPrison": "BXI",
        "version": 3
        },
        {
        "reference": "91130fff-d6ce-4ead-8461-1507140a2ae0",
        "reviewDateFrom": "2024-11-11",
        "reviewDateTo": "2025-01-11",
        "status": "EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY",
        "calculationRule": "BETWEEN_12_AND_60_MONTHS_TO_SERVE",
        "createdBy": "auser_gen",
        "createdByDisplayName": "Albert User",
        "createdAt": "2024-12-11T18:40:39.244Z",
        "createdAtPrison": "BXI",
        "updatedBy": "auser_gen",
        "updatedByDisplayName": "Albert User",
        "updatedAt": "2024-12-11T18:40:36.244Z",
        "updatedAtPrison": "BXI",
        "version": 2
        },
        {
        "reference": "91130fff-d6ce-4ead-8461-1507140a2ae0",
        "reviewDateFrom": "2024-11-11",
        "reviewDateTo": "2025-01-11",
        "status": "SCHEDULED",
        "calculationRule": "BETWEEN_12_AND_60_MONTHS_TO_SERVE",
        "createdBy": "auser_gen",
        "createdByDisplayName": "Albert User",
        "createdAt": "2024-12-11T18:40:39.229Z",
        "createdAtPrison": "BXI",
        "updatedBy": "auser_gen",
        "updatedByDisplayName": "Albert User",
        "updatedAt": "2024-12-11T18:40:35.229Z",
        "updatedAtPrison": "BXI",
        "version": 1
        },
        {
        "reference": "3eba9e8a-9144-408f-b4bf-2692aa4c2c97",
        "reviewDateFrom": "2024-11-11",
        "reviewDateTo": "2025-01-11",
        "status": "COMPLETED",
        "calculationRule": "BETWEEN_12_AND_60_MONTHS_TO_SERVE",
        "createdBy": "auser_gen",
        "createdByDisplayName": "Albert User",
        "createdAt": "2024-12-11T18:40:39.214Z",
        "createdAtPrison": "BXI",
        "updatedBy": "auser_gen",
        "updatedByDisplayName": "Albert User",
        "updatedAt": "2024-12-11T18:40:34.214Z",
        "updatedAtPrison": "BXI",
        "version": 2
        },
        {
        "reference": "3eba9e8a-9144-408f-b4bf-2692aa4c2c97",
        "reviewDateFrom": "2024-11-11",
        "reviewDateTo": "2025-01-11",
        "status": "SCHEDULED",
        "calculationRule": "BETWEEN_12_AND_60_MONTHS_TO_SERVE",
        "createdBy": "auser_gen",
        "createdByDisplayName": "Albert User",
        "createdAt": "2024-12-11T18:40:39.087Z",
        "createdAtPrison": "BXI",
        "updatedBy": "auser_gen",
        "updatedByDisplayName": "Albert User",
        "updatedAt": "2024-12-11T18:40:33.086Z",
        "updatedAtPrison": "BXI",
        "version": 1
        }
        ]
      """,
        ),
      )
  }
}
