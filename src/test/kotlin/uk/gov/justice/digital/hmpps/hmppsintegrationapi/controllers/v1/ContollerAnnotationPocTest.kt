package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ContollerAnnotationPocTest {

  @Test
  fun poc() {
    val sc = StatusController()
    val classLoader = sc.javaClass.classLoader

    val baseDir = File("src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/controllers/v1")
    assertTrue { baseDir.isDirectory }

    val paths = mutableSetOf<String>()

    for (f in baseDir.listFiles()) {
      if (f.isFile) {
        val simpleName = f.name.replace(".kt", "")
        val controllerClass = classLoader.loadClass("uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1." + simpleName)
        var baseUrl = ""
        for (controllerAnnotation in controllerClass.annotations) {
          if (controllerAnnotation is RequestMapping) {
            baseUrl = controllerAnnotation.value[0]
          }
        }
        for (method in controllerClass.declaredMethods) {
          for (annotation in method.annotations) {
            if (!(annotation is GetMapping)) {
              continue
            }
            if (annotation.value.size > 0) {
              var url = if (baseUrl.length > 0) {
                baseUrl + "/" + annotation.value[0]
              } else {
                annotation.value[0]
              }
              url = url.replace("//","/")
              paths += url
            }
          }
        }
      }
    }

    for (path in paths) {
      println(path)
    }
  }

}
