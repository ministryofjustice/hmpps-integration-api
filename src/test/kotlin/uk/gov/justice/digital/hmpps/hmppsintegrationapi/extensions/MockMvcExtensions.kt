package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.mock.web.MockHttpServletResponse

object MockMvcExtensions {
  val objectMapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()

  inline fun <reified T> MockHttpServletResponse.contentAsJson(): T = objectMapper.registerModule(JavaTimeModule()).readValue<T>(this.contentAsString)
}
