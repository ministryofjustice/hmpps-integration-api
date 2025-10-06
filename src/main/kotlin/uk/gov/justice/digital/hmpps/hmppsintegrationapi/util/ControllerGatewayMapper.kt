package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KParameter
import kotlin.reflect.javaType

/**
 * Utility for identifying which upstream APIs are used by which endpoints.
 */
class ControllerGatewayMapper {
  /**
   * Returns a map of controllers (ExtAPI endpoints) to gateways (upstream endpoints).
   *
   * This identifies the upstream service gateways used by each downstream API endpoint.
   */
  @OptIn(ExperimentalStdlibApi::class)
  fun getControllerGatewayMapping(context: ApplicationContext): MutableMap<String, Set<String>> {
    val mappings: MutableMap<String, Set<String>> = mutableMapOf()
    for (controller in context.getBeansWithAnnotation(RestController::class.java).values) {
      val gateways = mutableSetOf<String>()
      val controllerClassName = controller::class.qualifiedName?.replace("\$\$SpringCGLIB\$\$0", "")
      if ((controllerClassName == null) || !isController(controllerClassName)) {
        continue
      }
      val controllerClass = Class.forName(controllerClassName).kotlin
      for (parameter in controllerClass.constructors.first().parameters) {
        if (isService(parameter)) {
          val serviceClass = Class.forName(parameter.type.javaType.typeName).kotlin
          for (serviceParam in serviceClass.constructors
            .first()
            .parameters
            .map { it.type.toString() }) {
            if (isGateway(serviceParam)) {
              gateways.add(simpleName(serviceParam))
            }
          }
        }
      }
      mappings[simpleName(controllerClassName)] = gateways
    }
    return mappings
  }

  private fun isGateway(serviceParam: String): Boolean = serviceParam.contains("uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.")

  private fun isService(parameter: KParameter): Boolean = parameter.type.toString().contains("uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.")

  private fun isController(controllerClassName: String): Boolean = controllerClassName.contains("uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.")

  private fun simpleName(fullClassName: String?): String = fullClassName?.replace("uk.gov.justice.digital.hmpps.hmppsintegrationapi.", "") ?: ""
}
