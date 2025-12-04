package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.core.util.ObjectMapperFactory
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.core.models.SwaggerParseResult
import kotlin.test.Test

/**
 * Republishes parts of multiple API specs.
 *
 * This would help to support a declarative proxy style of External API.
 * In this model, to expose an upstream API externally we would...
 *
 * 1. Copy the upstream API spec into src/main/resources
 * 2. Create an endpoint proxy subclass that maps external endpoint paths to the upstream API
 * 3. Declare which upstream endpoints should or shouldn't be included
 * 4. Specify access controls
 *
 * The proxy subclass would simply forward on any matching requests, and return
 * the upstream response. It would also take care of handling and External API policies
 * such as converting hmppsIds to the format required upstream.
 */
class ApiSpecRepublisher {
  val specRoot = "src/test/resources/openapi-specs"
  val externalSpec = OpenAPI()

  fun init() {
    externalSpec.specVersion = SpecVersion.V31
    externalSpec.info = Info()
    externalSpec.info.title = "HMPPS External API"
    externalSpec.info.version = "2.0"
    externalSpec.info.description = "The HMPPS External API is an external API for HMPPS."
    externalSpec.info.summary = "External API Spec summary"
    externalSpec.components = Components()
    externalSpec.components.schemas = mutableMapOf()
  }

  /**
   * Republish part of an upstream API spec.
   */
  fun republish(upstreamSpec: String, upstreamPrefix: String, externalPrefix: String, tag: String) {

    val spec = OpenAPIParser().readLocation("${specRoot}/$upstreamSpec", emptyList(), ParseOptions())!!

    for (path in spec.openAPI.paths.keys) {
      if (path.startsWith(upstreamPrefix)) {
        val externalPath = path.replace(upstreamPrefix, externalPrefix)
        val pathSpec = spec.openAPI.paths[path]!!

        replaceTags(pathSpec, tag)

        addSchemaRefs(pathSpec, spec)

        addPathToOutputSpec(externalPath, pathSpec)
      }
    }
  }

  private fun addPathToOutputSpec(externalPath: String, pathSpec: PathItem) {
    externalSpec.path(externalPath, pathSpec)
  }

  /**
   * Replace the tags in the republished spec.
   */
  private fun replaceTags(pathSpec: PathItem, tag: String) {
    pathSpec.get?.tags = listOf(tag)
    pathSpec.post?.tags = listOf(tag)
    pathSpec.put?.tags = listOf(tag)
    pathSpec.delete?.tags = listOf(tag)
    pathSpec.patch?.tags = listOf(tag)
  }

  /**
   * Find references to other schemas and add them to the output spec.
   */
  private fun addSchemaRefs(
    pathSpec: PathItem,
    spec: SwaggerParseResult,
  ) {
    pathSpec.get?.responses?.forEach {
      it.value.content.values.forEach {
        addSchemaRef(it, spec)
      }
    }
    pathSpec.post?.requestBody?.content?.values?.forEach {
      addSchemaRef(it, spec)
    }
    pathSpec.put?.requestBody?.content?.values?.forEach {
      addSchemaRef(it, spec)
    }
  }

  private fun addSchemaRef(
    type: MediaType?,
    spec: SwaggerParseResult,
  ) {
    val ref = type?.schema?.`$ref`
    if (ref == null) {
      return
    }
    val refName = ref.substring(ref.lastIndexOf("/") + 1)
    addRef(refName, spec)
  }

  private fun addRef(refName: String, spec: SwaggerParseResult) {
    if (refName in externalSpec.components.schemas) {
      return // Already in the external spec, no need to add again
    }

    log("Adding ref $refName")

    val schema = spec.openAPI.components.schemas[refName]
    externalSpec.components.schemas[refName] = schema

    addChildSchemas(schema, spec)
  }

  private fun addChildSchemas(
    schema: Schema<*>?,
    spec: SwaggerParseResult,
  ) {
    for (prop in schema?.properties?.keys ?: emptyList()) {
      val propVal = schema?.properties[prop]

      val propType = propVal?.types
      if (propType?.first() == "array") {
        if (propVal.items?.`$ref` != null) {
          val refType = propVal.items?.`$ref`
          val refName = refType!!.substring(refType.lastIndexOf("/") + 1)
          log("  ${prop} -> array of ${refName}")
          addRef(refName, spec)
        }
      } else {
        if (propVal?.`$ref` != null) {
          val refType = propVal.`$ref`
          val refName = refType!!.substring(refType.lastIndexOf("/") + 1)
          log("  ${prop} -> ${refName}")
          addRef(refName, spec)
        }
      }

    }
  }

  fun log(text: String) {
//    println(text)
  }

  fun asJson() = ObjectMapperFactory.createJson31().writeValueAsString(externalSpec)
}

class ApiSpecRepublishTest {

  @Test
  fun `POC for api spec republishing`() {
    val repub = ApiSpecRepublisher()
    repub.init()

    repub.republish("activities.json", "/integration-api/", "/v2/activities/", "Activities")
    repub.republish("plp.json", "/inductions/", "/v2/plp/inductions/", "Inductions")
    repub.republish("plp.json", "/action-plans/", "/v2/plp/action-plans/", "Action Plans")

    println(repub.asJson())
  }
}
