package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.context.ApplicationContext
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

/**
 * Utility for identifying which upstream APIs are used by which endpoints.
 */
class ControllerGatewayMapper {
  companion object {
    const val PACKAGE_NAME = "uk.gov.justice.digital.hmpps.hmppsintegrationapi"
    const val GATEWAYS = "gateways"
  }

  /**
   * Returns a map of controllers (ExtAPI endpoints) to gateways (upstream endpoints).
   *
   * This identifies the upstream service gateways used by each downstream API endpoint.
   */

  private fun getParamList(clazz: KClass<*>): Set<KClass<*>> =
    clazz
      .takeIf {
        isInPackage(clazz)
      }?.constructors
      ?.map { c ->
        c.parameters
          .map { p ->
            p.type.classifier as KClass<*>
          }.flatMap { listOf(it) + getParamList(it) }
      }.orEmpty()
      .flatten()
      .toSet()

  private fun isInPackage(clazz: KClass<*>): Boolean = clazz.qualifiedName?.contains(PACKAGE_NAME) == true

  private fun toName(clazz: KClass<*>): String = clazz.javaObjectType.name.replace("$PACKAGE_NAME.", "")

  fun getControllerGatewayMapping(context: ApplicationContext): Map<String, Set<String>> =
    context
      .getBeansWithAnnotation(RestController::class.java)
      .values
      .filter { isInPackage(ClassUtils.getUserClass(it).kotlin) }
      .associate {
        toName(ClassUtils.getUserClass(it).kotlin) to
          getParamList(it::class)
            .filter { it.qualifiedName?.contains("$PACKAGE_NAME.$GATEWAYS") == true }
            .map { toName(it) }
            .toSet()
      }
}
