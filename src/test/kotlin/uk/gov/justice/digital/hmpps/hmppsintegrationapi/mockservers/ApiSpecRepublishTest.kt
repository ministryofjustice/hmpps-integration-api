package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.core.util.ObjectMapperFactory
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.parser.core.models.ParseOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

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

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun init() {
    externalSpec.specVersion = SpecVersion.V31
    externalSpec.info = Info()
    externalSpec.info.title = "HMPPS External API"
    externalSpec.info.version = "2.0"
    externalSpec.info.description = "The HMPPS External API is an external API for HMPPS."
    externalSpec.components = Components()
    externalSpec.components.schemas = mutableMapOf()
  }

  /**
   * Republish part of an upstream API spec.
   */
  fun republish(
    upstreamSpecName: String,
    upstreamPrefix: String,
    externalPrefix: String,
    tag: String,
  ) {
    val parseResult = OpenAPIParser().readLocation("$specRoot/$upstreamSpecName", emptyList(), ParseOptions())!!
    val upstreamSpec = parseResult.openAPI

    for (path in upstreamSpec.paths.keys) {
      if (!path.startsWith(upstreamPrefix)) {
        continue
      }

      val externalPath = path.replace(upstreamPrefix, externalPrefix)
      val pathSpec = upstreamSpec.paths[path]!!

      replaceTags(pathSpec, tag)

      addSchemaRefs(pathSpec, upstreamSpec)

      addPathToOutputSpec(externalPath, pathSpec)
    }
  }

  private fun addPathToOutputSpec(
    externalPath: String,
    pathSpec: PathItem,
  ) {
    externalSpec.path(externalPath, pathSpec)
  }

  /**
   * Replace the tags in the republished spec.
   */
  private fun replaceTags(
    pathSpec: PathItem,
    tag: String,
  ) {
    val externalTags = listOf(tag)
    pathSpec.get?.tags = externalTags
    pathSpec.post?.tags = externalTags
    pathSpec.put?.tags = externalTags
    pathSpec.delete?.tags = externalTags
    pathSpec.patch?.tags = externalTags
    pathSpec.options?.tags = externalTags
    pathSpec.head?.tags = externalTags
  }

  /**
   * Find references to other schemas and add them to the output spec.
   */
  private fun addSchemaRefs(
    pathSpec: PathItem,
    spec: OpenAPI,
  ) {
    addRequestSchemaRefs(pathSpec.post, spec)
    addRequestSchemaRefs(pathSpec.put, spec)
    addResponseSchemaRefs(pathSpec.get?.responses, spec)
    addResponseSchemaRefs(pathSpec.post?.responses, spec)
  }

  private fun addRequestSchemaRefs(
    op: Operation?,
    spec: OpenAPI,
  ) {
    addSchemaRefs(op?.requestBody?.content?.values, spec)
  }

  private fun addResponseSchemaRefs(
    responses: Map<String, ApiResponse>?,
    spec: OpenAPI,
  ) {
    if (responses == null) return

    for (response in responses) {
      addSchemaRefs(response.value.content?.values, spec)
    }
  }

  private fun addSchemaRefs(
    contentTypes: MutableCollection<MediaType>?,
    spec: OpenAPI,
  ) {
    if (contentTypes == null) return

    for (content in contentTypes) {
      addSchemaRef(content, spec)
    }
  }

  private fun addSchemaRef(
    type: MediaType?,
    spec: OpenAPI,
  ) {
    val ref = type?.schema?.`$ref` ?: type?.schema?.items?.`$ref`
    if (ref != null) {
      addNamedRef(referenceSimpleName(ref), spec)
    }
  }

  private fun referenceSimpleName(ref: String): String = ref.substring(ref.lastIndexOf("/") + 1)

  private fun addNamedRef(
    refName: String,
    spec: OpenAPI,
  ) {
    if (refName in externalSpec.components.schemas) {
      return // Already in the external spec, no need to add again
    }

    log.debug("Adding ref $refName")

    val schema = spec.components.schemas[refName]
    externalSpec.components.schemas[refName] = schema

    addPropertySchemas(schema, spec)
  }

  /**
   * Add schemas referenced by properties of the specified schema.
   */
  private fun addPropertySchemas(
    schema: Schema<*>?,
    spec: OpenAPI,
  ) {
    for (prop in schema?.properties?.keys ?: emptyList()) {
      val propVal = schema?.properties[prop]
      val refName = propVal?.`$ref` ?: propVal?.items?.`$ref`
      if (refName == null) {
        continue
      }
      val simpleName = referenceSimpleName(refName)
      log.debug("  $prop -> $simpleName")
      addNamedRef(simpleName, spec)
    }
  }

  fun asJson() = ObjectMapperFactory.createJson31().writeValueAsString(externalSpec)

  fun pathCount() = externalSpec.paths.size

  fun schemaCount() = externalSpec.components.schemas.size
}

/**
 * Programatically overrides the level of the specified logger.
 */
private fun setLogLevel(
  targetClass: Class<ApiSpecRepublisher>,
  logLevel: String,
) {
  val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
  val logs = loggerContext.getLogger(targetClass)
  logs.level = Level.valueOf(logLevel)
}

class ApiSpecRepublishTest {
  @Test
  fun `POC for api spec republishing`() {
    setLogLevel(ApiSpecRepublisher::class.java, "INFO")

    val apiSpec = ApiSpecRepublisher()
    apiSpec.init()

    apiSpec.republish("activities.json", "/integration-api/", "/v2/activities/", tag = "Activities")
    apiSpec.republish("plp.json", "/inductions/", "/v2/plp/inductions/", tag = "Inductions")
    apiSpec.republish("plp.json", "/action-plans/", "/v2/plp/action-plans/", tag = "Action Plans")

    val specJson = apiSpec.asJson()

    println(specJson)

    assertEquals(27, apiSpec.pathCount())
    assertEquals(95, apiSpec.schemaCount())

    // Does it have the downstream spec metadata?
    assertContains(specJson, "The HMPPS External API is an external API for HMPPS.")

    // Does it contain endpoints from the Activities upstream spec?
    assertContains(specJson, "/v2/activities/scheduled-events/prison/{prisonCode}")

    // Does it contain endpoints from the PLP upstream spec?
    assertContains(specJson, "/v2/plp/action-plans/{prisonNumber}/reviews/schedule-status")

    // Does it contain data type schemas?
    assertContains(specJson, "captureIncentiveLevelWarning")
  }
}
