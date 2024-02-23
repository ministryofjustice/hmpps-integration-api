package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.caseNotes

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.CaseNotesApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CaseNotesGateway::class],
)
class CaseNotesGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val caseNotesGateway: CaseNotesGateway,
) : DescribeSpec(
  {

    val caseNotesApiMockServer = CaseNotesApiMockServer()
    beforeEach {
      caseNotesApiMockServer.start()

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("CaseNotes")).thenReturn(
        HmppsAuthMockServer.TOKEN,
      )
    }

    afterTest {
      caseNotesApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      caseNotesGateway.getCaseNotesForPerson(id = "123", CaseNoteFilter(hmppsId = ""))

      verify(hmppsAuthGateway, times(1))
        .getClientToken("CaseNotes")
    }

    it("upstream API returns an error, return error") {
      caseNotesApiMockServer.stubGetCaseNotes("123", "", "", HttpStatus.BAD_REQUEST)
      val response = caseNotesGateway.getCaseNotesForPerson(id = "123", CaseNoteFilter(hmppsId = ""))
      response.data.shouldBe(emptyList())
      response.errors[0].type.shouldBe(UpstreamApiError.Type.BAD_REQUEST)
    }

    it("returns caseNote") {
      val responseJson = """
       {
       "content": [
    {
      "caseNoteId": "131231",
      "offenderIdentifier": "A1234AB",
      "type": "POM",
      "typeDescription": "POM Notes",
      "subType": "GEN",
      "subTypeDescription": "General POM Note",
      "authorUserId": "SECURE_CASENOTE_USER_ID",
      "authorName": "Mikey Mouse",
      "text": "This is another case note",
      "locationId": "LEI",
      "amendments": [
        {
          "authorUserName": "SECURE_CASENOTE_USER",
          "authorUserId": "SECURE_CASENOTE_USER_ID",
          "additionalNoteText": "Amended case note"
        }
      ]
    },
    {
      "caseNoteId": "131232",
      "offenderIdentifier": "A1234AB",
      "type": "OBS",
      "typeDescription": "Observation",
      "subType": "GEN",
      "subTypeDescription": "General",
      "source": "INST",
      "authorUserId": "1231232",
      "authorName": "Mickey Mouse",
      "text": "Some Text",
      "locationId": "LEI",
      "amendments": []
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "first": true,
  "numberOfElements": 2,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "number": 0,
  "empty": false
       }
        """
      caseNotesApiMockServer.stubGetCaseNotes("123", "", responseJson, HttpStatus.OK)
      val response = caseNotesGateway.getCaseNotesForPerson(id = "123", CaseNoteFilter(hmppsId = ""))
      response.data.count().shouldBe(2)
      response.data.shouldExist { it -> it.caseNoteId == "131231" }
      response.data.shouldExist { it -> it.caseNoteId == "131232" }
    }

    it("returns generate filter parameters caseNote") {
      val filter = CaseNoteFilter("hmppsId", LocalDateTime.of(2024, 1, 2, 0, 0), LocalDateTime.of(2024, 1, 3, 0, 0), "mockLocation")
      val responseJson = """
       {
       "content": [
    {
      "caseNoteId": "131231",
      "offenderIdentifier": "A1234AB",
      "type": "POM",
      "typeDescription": "POM Notes",
      "subType": "GEN",
      "subTypeDescription": "General POM Note",
      "authorUserId": "SECURE_CASENOTE_USER_ID",
      "authorName": "Mikey Mouse",
      "text": "This is another case note",
      "locationId": "LEI",
      "amendments": [
        {
          "authorUserName": "SECURE_CASENOTE_USER",
          "authorUserId": "SECURE_CASENOTE_USER_ID",
          "additionalNoteText": "Amended case note"
        }
      ]
    },
    {
      "caseNoteId": "131232",
      "offenderIdentifier": "A1234AB",
      "type": "OBS",
      "typeDescription": "Observation",
      "subType": "GEN",
      "subTypeDescription": "General",
      "source": "INST",
      "authorUserId": "1231232",
      "authorName": "Mickey Mouse",
      "text": "Some Text",
      "locationId": "LEI",
      "amendments": []
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "first": true,
  "numberOfElements": 2,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "number": 0,
  "empty": false
       }
        """
      val params = "?locationId=mockLocation&startDate=2024-01-02&endDate=2024-01-03"
      caseNotesApiMockServer.stubGetCaseNotes("123", params, responseJson, HttpStatus.OK)
      val response = caseNotesGateway.getCaseNotesForPerson(id = "123", filter)
      response.data.count().shouldBe(2)
      response.data.shouldExist { it -> it.caseNoteId == "131231" }
      response.data.shouldExist { it -> it.caseNoteId == "131232" }
    }
  },
)
