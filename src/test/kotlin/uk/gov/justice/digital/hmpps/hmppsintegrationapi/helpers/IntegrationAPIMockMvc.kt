package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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

  fun performAuthorisedPut(path: String): MvcResult {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    return mockMvc.perform(MockMvcRequestBuilders.put(path).header("subject-distinguished-name", subjectDistinguishedName)).andReturn()
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

  fun <T : Any> performAuthorisedPostWithCN(
    path: String,
    cn: String,
    requestBody: T,
  ): MvcResult {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=$cn"
    val requestBuilder =
      MockMvcRequestBuilders
        .post(path)
        .header("subject-distinguished-name", subjectDistinguishedName)
        .content(asJsonString(requestBody))
        .contentType(MediaType.APPLICATION_JSON)
    return mockMvc.perform(requestBuilder).andReturn()
  }

  fun performUnAuthorised(path: String): MvcResult = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

  private fun asJsonString(obj: Any): String {
    val objectMapper = ObjectMapper()
    objectMapper.registerModule(JavaTimeModule())
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    return objectMapper.writeValueAsString(obj)
  }
}
