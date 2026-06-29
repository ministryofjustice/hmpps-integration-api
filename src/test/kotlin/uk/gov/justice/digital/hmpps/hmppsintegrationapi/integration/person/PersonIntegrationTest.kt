package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPrisonFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService.Companion.attributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.createUnsignedJwt
import java.io.File

class PersonIntegrationTest : IntegrationTestBase() {
  @Nested
  inner class GetPerson {
    val firstName = "Robert"
    val lastName = "Larsen"

    @BeforeEach
    fun setup() {
      val expectedRequest = attributeSearchRequest(firstName, lastName)

      prisonerOffenderSearchMockServer.stubForPost(
        "/attribute-search",
        jacksonObjectMapper().writeValueAsString(expectedRequest.toMap()),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearch.json",
        ).readText(),
      )
    }

    @Test
    fun `returns a list of persons using first name and last name as search parameters`() {
      val queryParams = "first_name=$firstName&last_name=$lastName"

      callApi("$basePath?$queryParams")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-name-search-response.json")))

      prisonerOffenderSearchMockServer.assertValidationPassed()
    }

    @Test
    fun `returns a list of persons using pnc number search with consumer filters`() {
      whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithPrisonFilters))
      val pncNumber = "2003/13116M"

      val expectedRequest = attributeSearchRequest(firstName, lastName, pncNumber, consumerFilters = testRoleWithPrisonFilters.filters!!)

      prisonerOffenderSearchMockServer.stubForPost(
        "/attribute-search",
        jacksonObjectMapper().writeValueAsString(expectedRequest.toMap()),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearch.json",
        ).readText(),
      )

      val queryParams = "first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber"

      callApi("$basePath?$queryParams")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-name-pnc-search-response.json")))

      prisonerOffenderSearchMockServer.assertValidationPassed()
    }

    @Test
    fun `sends the obo username to hmpps auth in a query param when obo username present in request`() {
      val queryParams = "first_name=$firstName&last_name=$lastName"
      callApiWithCN("$basePath?$queryParams", "obo-unsigned", oboValue = createUnsignedJwt())
      val uri = argumentCaptor<String>()
      verify(authSpy, atLeast(1)).getResponseBodySpec(eq(HttpMethod.POST), uri.capture(), anyMap(), eq(null))
      val authRequestStrings = uri.allValues
      // Gets an auth token for deliusGateway.getPersons and prisonerOffenderSearchGateway.attributeSearch passing the username
      assertThat(authRequestStrings.size).isEqualTo(2)
      authRequestStrings.forEach {
        assertThat(it).isEqualTo("/auth/oauth/token?grant_type=client_credentials&username=testName")
      }
    }

    @Test
    fun `does not send the obo username to hmpps auth in a query param when obo username present in request`() {
      val queryParams = "first_name=$firstName&last_name=$lastName"
      callApi("$basePath?$queryParams")
      val uri = argumentCaptor<String>()
      verify(authSpy, atLeast(1)).getResponseBodySpec(eq(HttpMethod.POST), uri.capture(), anyMap(), eq(null))
      val authRequestStrings = uri.allValues
      // Gets an auth token for deliusGateway.getPersons and prisonerOffenderSearchGateway.attributeSearch passing the username
      assertThat(authRequestStrings.size).isEqualTo(2)
      authRequestStrings.forEach {
        assertThat(it).isEqualTo("/auth/oauth/token?grant_type=client_credentials")
      }
    }

    @Test
    fun `returns a person from Prisoner Offender Search and Probation Offender Search`() {
      callApi("$basePath/$crn")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-offender-and-probation-search-response.json")))

      prisonerOffenderSearchMockServer.assertValidationPassed()
    }

    @Nested
    @DisplayName("And prisoner number was merged")
    inner class AndPrisonerNumberIsMerged {
      @Test
      fun `return a 303 redirect response containing merged into prisoner number, removed prisoner number and a redirect URL`() {
        prisonerOffenderSearchMockServer.stubFor(
          get(urlPathMatching("/prisoner/.*"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("""{ "status": 404, "error": "Not Found", "message": "Prisoner not found" }"""),
            ),
        )

        val file = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearchPrisonerNumberMerged.json")
        require(file.exists()) { "File not found at: ${file.absolutePath}" }
        val body = file.readText()
        prisonerOffenderSearchMockServer.stubFor(
          post(urlPathEqualTo("/attribute-search"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(body)),
        )

        callApi("$basePath/$nomsId")
          .andExpect(status().is3xxRedirection)
          .andExpect(header().string("Location", Matchers.containsString("/v1/persons/A1234AA")))
          .andExpect(content().string(""))

        // Need to look into the validation.request.body.schema.processingError causing issues on this test and associated DeactivateLocationIntegrationTest
        // prisonerOffenderSearchApiMockServer.assertValidationPassed()
      }
    }

    @Test
    fun `calls prisoner search with the redirected prisoner number when CRN is used and data returned`() {
      prisonerOffenderSearchMockServer.stubFor(
        get(urlPathMatching("/prisoner/$nomsIdFromProbation"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withHeader("Content-Type", "application/json")
              .withBody("""{ "status": 404, "error": "Not Found", "message": "Prisoner not found" }"""),
          ),
      )

      prisonerOffenderSearchMockServer.stubForGet(
        "/prisoner/A1234AA",
        File(
          "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
        ).readText(),
      )

      val file = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearchPrisonerNumberMergedFromCrn.json")
      val body = file.readText()
      prisonerOffenderSearchMockServer.stubFor(
        post(urlPathEqualTo("/attribute-search"))
          .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(body)),
      )

      callApi("$basePath/$crn")
        .andExpect(status().isOk)
    }
  }

  @Nested
  inner class GetImageMetadataForPerson {
    @Test
    fun `returns image metadata for a person`() {
      callApi("$basePath/$nomsId/images")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-image-meta-data")))
    }

    @Test
    fun `images endpoint return a 404 for person in wrong prison`() {
      callApiWithCN("$basePath/$nomsId/images", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `images name endpoint return a 404 when no prisons in filter`() {
      callApiWithCN("$basePath/$nomsId/images", noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/images")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetPersonName {
    @Test
    fun `returns person name details for a person`() {
      callApi("$basePath/$nomsId/name")
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
      {"data":{"firstName":"Matt","lastName":"Nolan"}}
    """,
          ),
        )
    }

    @Test
    fun `persons name endpoint return a 404 for person in wrong prison`() {
      callApiWithCN("$basePath/$nomsId/name", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `persons name endpoint return a 404 when no prisons in filter`() {
      callApiWithCN("$basePath/$nomsId/name", noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `persons name endpoint return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/name")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetCellLocation {
    @Test
    fun `returns person cell location if in prison`() {
      callApi("$basePath/$nomsId/cell-location")
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
  {"data":{"prisonCode":"MDI","prisonName":"HMP Leeds","cell":"A-1-002"}}
""",
          ),
        )
    }

    @Test
    fun `cell location return a 404 for person in wrong prison`() {
      callApiWithCN("$basePath/$nomsId/cell-location", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `cell location return a 404 when no prisons in filter`() {
      callApiWithCN("$basePath/$nomsId/cell-location", noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `cell location return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/cell-location")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetIEPLevel {
    val path = "$basePath/$nomsId/iep-level"

    @Test
    fun `returns a prisoners iep level`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json("""{"data":{"iepCode": "STD", "iepLevel": "Standard"}}"""))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/iep-level")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetNumberOfChildren {
    val path = "$basePath/$nomsId/number-of-children"

    @Test
    fun `returns a prisoner's number of children`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
          {
            "data": {
              "numberOfChildren": "string"
            }
          }
          """,
          ),
        )
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/number-of-children")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetPhysicalCharacteristics {
    val path = "$basePath/$nomsId/physical-characteristics"

    @Test
    fun `returns a prisoner's physical characteristics`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("physical-characteristics")))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/physical-characteristics")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetCareNeeds {
    val path = "$basePath/$nomsId/care-needs"

    @Test
    fun `returns a prisoner's care needs`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("personal-care-needs")))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/care-needs")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetLanguages {
    val path = "$basePath/$nomsId/languages"

    @Test
    fun `returns a prisoner's languages`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-languages")))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/languages")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetEducation {
    val path = "$basePath/$nomsId/education"

    @Test
    fun `returns a prisoner's education`() {
      plpMockServer.stubForGet(
        "/person/$nomsId/education",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetPrisonerEducationResponse.json",
        ).readText(),
      )
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-education")))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/education")
        .andExpect(status().isBadRequest)
    }
  }
}
