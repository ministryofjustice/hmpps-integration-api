package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.mock.web.MockHttpServletResponse
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue

object MockMvcExtensions {
  val objectMapper: JsonMapper =
    JsonMapper
      .builder()
      .addModule(KotlinModule.Builder().build())
      .enable(SerializationFeature.INDENT_OUTPUT)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
      .build()

  fun writeAsJson(obj: Any): String = objectMapper.writeValueAsString(obj)

  inline fun <reified T> MockHttpServletResponse.contentAsJson(): T = objectMapper.readValue<T>(this.contentAsString)
}
