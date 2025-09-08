package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.mock.web.MockHttpServletResponse

object MockMvcExtensions {
  val objectMapper: ObjectMapper =
    jacksonObjectMapper()
      .registerKotlinModule()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .registerModule(JavaTimeModule())
      .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)

  fun writeAsJson(obj: Any): String = objectMapper.writeValueAsString(obj)

  inline fun <reified T> MockHttpServletResponse.contentAsJson(): T = objectMapper.registerModule(JavaTimeModule()).readValue<T>(this.contentAsString)
}
