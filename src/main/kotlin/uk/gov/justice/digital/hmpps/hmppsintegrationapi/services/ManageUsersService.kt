package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway

@Service
class ManageUsersService(
  private val manageUsersGateway: ManageUsersGateway,
) {
  fun usernameExists(
    username: String,
    authSources: List<String>,
  ): Boolean {
    val usersResponse = manageUsersGateway.findUser(username, authSources)
    usersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} failed with error: ${it.type.name}")
    }
    return usersResponse.data?.content?.any { it.enabled && !it.locked } ?: false
  }
}
