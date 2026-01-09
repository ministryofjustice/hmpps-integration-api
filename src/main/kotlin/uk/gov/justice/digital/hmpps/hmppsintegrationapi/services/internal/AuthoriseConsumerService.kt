package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.normalisePath
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Component
@Service
class AuthoriseConsumerService {
  fun doesConsumerHaveIncludesAccess(
    consumerConfig: ConsumerConfig?,
    requestedPath: String,
  ): Boolean {
    consumerConfig?.permissions()?.forEach {
      if (Regex(normalisePath(it)).matches(requestedPath)) {
        return true
      }
    }
    return false
  }

  fun doesConsumerHaveRoleAccess(
    consumerRolesInclude: List<String>,
    requestPath: String,
  ): Boolean {
    consumerRolesInclude.forEach {
      if (Regex(normalisePath(it)).matches(requestPath)) {
        return true
      }
    }
    return false
  }
}
