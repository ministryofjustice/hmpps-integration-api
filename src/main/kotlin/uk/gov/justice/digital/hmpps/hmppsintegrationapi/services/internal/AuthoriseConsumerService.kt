package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthoriseConsumerService(
  @Autowired private val extractConsumerFromSubjectDistinguishedNameService: ExtractConsumerFromSubjectDistinguishedNameService,
) {
  fun execute(subjectDistinguishedName: String, consumerPathConfig: Map<String, List<String>>, requestedPath: String): Boolean {
    val consumer = extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)

    println("consumer: $consumer")
    println("requestedPath: $requestedPath")

    consumerPathConfig[consumer]?.forEach {
      if (Regex(it).matches(requestedPath)) {
        return true
      }
    }

    return false
  }
}
