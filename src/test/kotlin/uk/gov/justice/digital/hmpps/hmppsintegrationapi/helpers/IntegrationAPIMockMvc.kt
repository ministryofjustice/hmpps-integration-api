package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@AutoConfigureMockMvc
class IntegrationAPIMockMvc(
  @Autowired var mockMvc: MockMvc,
) {
  fun performAuthorised(path: String): MvcResult {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    return mockMvc.perform(MockMvcRequestBuilders.get(path).header("subject-distinguished-name", subjectDistinguishedName)).andReturn()
  }

  fun performAuthorisedWithCN(
    path: String,
    cn: String,
  ): MvcResult {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=$cn"
    return mockMvc.perform(MockMvcRequestBuilders.get(path).header("subject-distinguished-name", subjectDistinguishedName)).andReturn()
  }

  fun <T : Any> performAuthorisedPost(
    path: String,
    requestBody: T,
  ): MvcResult {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    val requestBuilder =
      MockMvcRequestBuilders
        .post(path)
        .header("subject-distinguished-name", subjectDistinguishedName)
        .content(asJsonString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
    return mockMvc.perform(requestBuilder).andReturn()
  }

  fun performUnAuthorised(path: String): MvcResult = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

  private fun asJsonString(obj: Any): String = jacksonObjectMapper().writeValueAsString(obj)
}
