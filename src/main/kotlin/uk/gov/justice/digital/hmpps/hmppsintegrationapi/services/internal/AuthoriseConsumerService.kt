package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Component
@Service
class AuthoriseConsumerService {
  fun execute(
    consumer: String,
    consumerPathConfig: Map<String, ConsumerConfig>,
    requestedPath: String,
  ): Boolean {
    println("consumer: $consumer")
    println("requestedPath: $requestedPath")

    consumerPathConfig[consumer]?.include?.forEach {
      if (Regex(it).matches(requestedPath)) {
        return true
      }
    }
    return false
  }
}
