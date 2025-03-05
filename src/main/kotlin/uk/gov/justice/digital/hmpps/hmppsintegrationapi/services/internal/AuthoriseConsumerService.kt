package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Component
@Service
class AuthoriseConsumerService {
  fun doesConsumerHaveIncludesAccess(
    consumerConfig: ConsumerConfig?,
    requestedPath: String,
  ): Boolean {
    consumerConfig?.include?.forEach {
      if (Regex(it).matches(requestedPath)) {
        return true
      }
    }
    return false
  }

  fun doesConsumerHaveRoleAccess(
    consumerRolesConfigPaths: List<String>?,
    requestPath: String,
  ): Boolean {
    consumerRolesConfigPaths?.forEach {
      if (Regex(it).matches(requestPath)) {
        return true
      }
    }
    return false
  }
}
