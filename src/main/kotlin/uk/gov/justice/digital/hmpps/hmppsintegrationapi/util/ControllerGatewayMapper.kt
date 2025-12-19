package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.UpstreamGateway
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Utility for identifying which upstream APIs are used by which endpoints.
 */
class ControllerGatewayMapper {
  companion object {
    const val BASE_PACKAGE_NAME = "uk.gov.justice.digital.hmpps.hmppsintegrationapi"
    const val GATEWAYS = "gateways"
  }

  private fun injectedDependencies(clazz: KClass<*>): Set<KClass<*>> =
    clazz
      .takeIf { isInPackage(clazz, BASE_PACKAGE_NAME) }
      ?.constructors
      ?.map { c -> allDependencies(c) }
      .orEmpty()
      .flatten()
      .toSet()

  private fun allDependencies(c: KFunction<Any>): List<KClass<*>> =
    c.parameters
      .map { p -> p.type.classifier as KClass<*> }
      .flatMap { listOf(it) + injectedDependencies(it) }

  private fun isInPackage(
    clazz: KClass<*>,
    packagePath: String,
  ): Boolean = clazz.qualifiedName?.contains(packagePath) == true

  private fun simpleName(clazz: KClass<*>): String = clazz.javaObjectType.name.replace("$BASE_PACKAGE_NAME.", "")

  private fun injectedGatewayNames(clazz: KClass<*>) = injectedDependencies(clazz).filter { isGateway(it) }.map { simpleName(it) }.toSet()

  private fun isGateway(klass: KClass<*>): Boolean = isInPackage(klass, "$BASE_PACKAGE_NAME.$GATEWAYS")

  private fun kClass(obj: Any): KClass<out Any> = ClassUtils.getUserClass(obj).kotlin

  private fun extApiRestControllers(context: ApplicationContext): List<KClass<out Any>> =
    context
      .getBeansWithAnnotation(RestController::class.java)
      .values
      .map { kClass(it) }
      .filter { isInPackage(it, BASE_PACKAGE_NAME) }

  fun extApiUpstreamGateways(context: ApplicationContext): List<UpstreamGateway> =
    context
      .getBeansWithAnnotation(Component::class.java)
      .values
      .filter { it.javaClass.interfaces.contains(UpstreamGateway::class.java) }
      .map { it as UpstreamGateway }
      .toList()

  fun getControllerGatewayMapping(context: ApplicationContext): Map<String, Set<String>> =
    extApiRestControllers(context)
      .associate { simpleName(it) to injectedGatewayNames(it) }
}
