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

  private fun getParamList(clazz: KClass<*>): Set<KClass<*>> =
    clazz
      .takeIf {
        isInPackage(clazz, PACKAGE_NAME)
      }?.constructors
      ?.map { c ->
        c.parameters
          .map { p ->
            p.type.classifier as KClass<*>
          }.flatMap { listOf(it) + getParamList(it) }
      }.orEmpty()
      .flatten()
      .toSet()

  private fun isInPackage(
    clazz: KClass<*>,
    packagePath: String,
  ): Boolean = clazz.qualifiedName?.contains(packagePath) == true

  private fun toName(clazz: KClass<*>): String = clazz.javaObjectType.name.replace("$PACKAGE_NAME.", "")

  private fun associations(clazz: KClass<*>) = getParamList(clazz).filter { isInPackage(it, "$PACKAGE_NAME.$GATEWAYS") }.map { toName(it) }.toSet()

  fun getControllerGatewayMapping(context: ApplicationContext): Map<String, Set<String>> =
    context
      .getBeansWithAnnotation(RestController::class.java)
      .values
      .map { ClassUtils.getUserClass(it).kotlin }
      .filter { isInPackage(it, PACKAGE_NAME) }
      .associate { toName(it) to associations(it) }
}
