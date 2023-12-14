package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
@Service
class AuthoriseConsumerService {
  fun execute(consumer: String, consumerPathConfig: Map<String, List<String>>, requestedPath: String): Boolean {
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
