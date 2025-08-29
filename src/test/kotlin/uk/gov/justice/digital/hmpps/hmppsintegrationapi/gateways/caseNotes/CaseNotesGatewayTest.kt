package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.caseNotes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes.CNSearchNotesRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes.CNTypeSubType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CaseNotesGateway::class],
)
class CaseNotesGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val caseNotesGateway: CaseNotesGateway,
) : DescribeSpec(
    {
      val objectMapper = jacksonObjectMapper()
      val id = "123"
      val pathNoParams = "/search/case-notes/$id"
      val caseNotesApiMockServer = ApiMockServer.create(UpstreamApi.CASE_NOTES)
      val caseNoteRequest = CNSearchNotesRequest(page = 1, size = 10)
      val caseNoteFilter = CaseNoteFilter(hmppsId = id)

      val jsonRequest = objectMapper.writeValueAsString(caseNoteRequest.toApiConformingMap())

      val responseJson = """
         {
            "content": [
              {
                "caseNoteId": $id,
                "offenderIdentifier": "A1234AA",
                "type": "KA",
                "typeDescription": "Key Worker",
                "subType": "KS",
                "subTypeDescription": "Key Worker Session",
                "creationDateTime": "2025-01-01T01:30:00Z",
                "occurrenceDateTime": "2017-10-31T01:30:00Z",
                "authorName": "John Smith",
                "authorUserId": "12345",
                "authorUsername": "USER1",
                "text": "This is some text",
                "locationId": "MDI",
                "sensitive": true,
                "amendments": [
                  {
                    "creationDateTime": "2018-12-01T13:45:00Z",
                    "authorUserName": "USER1",
                    "authorName": "Mickey Mouse",
                    "authorUserId": "12345",
                    "additionalNoteText": "Some Additional Text"
                  }
                ],
                "systemGenerated": true,
                "legacyId": 9007199254740991
              }
            ],
            "metadata": {
              "totalElements": 1,
              "page": 1,
              "size": 10
            },
            "hasCaseNotes": true
          }
        """
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
        caseNotesGateway.getCaseNotesForPerson(id = "123", caseNoteFilter)

        verify(hmppsAuthGateway, times(1))
          .getClientToken("CaseNotes")
      }

      it("upstream API returns an error, throw exception") {
        caseNotesApiMockServer.stubForPost(pathNoParams, jsonRequest, "", HttpStatus.BAD_REQUEST)
        val response =
          shouldThrow<WebClientResponseException> {
            caseNotesGateway.getCaseNotesForPerson(id = id, caseNoteFilter)
          }
        response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
      }

      it("upstream API returns an forbidden error, throw forbidden exception") {
        caseNotesApiMockServer.stubForPost(pathNoParams, jsonRequest, "", HttpStatus.FORBIDDEN)
        val response = caseNotesGateway.getCaseNotesForPerson(id = id, caseNoteFilter)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.FORBIDDEN)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.CASE_NOTES)
      }

      it("returns caseNote") {
        caseNotesApiMockServer.stubForPost(pathNoParams, jsonRequest, responseJson, HttpStatus.OK)
        val response = caseNotesGateway.getCaseNotesForPerson(id = id, caseNoteFilter)
        response.data
          ?.content!!
          .count()
          .shouldBe(1)
        response.data!!.content.shouldExist { it.caseNoteId == id }
      }

      it("requests specific caseNote types") {
        val caseNoteRequest = CNSearchNotesRequest(page = 1, size = 10, typeSubTypes = listOf(CNTypeSubType("KA"), CNTypeSubType("CAB")))
        val jsonRequest = objectMapper.writeValueAsString(caseNoteRequest.toApiConformingMap())
        caseNotesApiMockServer.stubForPost(pathNoParams, jsonRequest, responseJson, HttpStatus.OK)

        val response = caseNotesGateway.getCaseNotesForPerson(id = id, caseNoteFilter, listOf("KA", "CAB"))
        response.data
          ?.content!!
          .count()
          .shouldBe(1)
        response.data!!.content.shouldExist { it.caseNoteId == id }

        caseNotesApiMockServer.verify(
          postRequestedFor(urlEqualTo(pathNoParams))
            .withRequestBody(
              equalToJson(
                """
                {
                  "includeSensitive" : true,
                  "typeSubTypes" : [ {
                    "type" : "KA",
                    "subTypes" : []
                  },
                  {
                    "type" : "CAB",
                    "subTypes" : []
                  }],
                  "page" : 1,
                  "size" : 10
                }
                """.trimIndent(),
              ),
            ).withHeader("Content-Type", equalTo("application/json")),
        )
      }
    },
  )
