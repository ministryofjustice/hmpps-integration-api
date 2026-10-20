package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationAccessGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthUsernames
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsPrisonRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsPrisonUser
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess

@Service
class ManageUsersService(
  private val manageUsersGateway: ManageUsersGateway,
  private val probationAccessGateway: ProbationAccessGateway,
) {
  fun getAuthUsernames(username: String): HmppsAuthUsernames {
    val usersResponse =
      manageUsersGateway.findUser(
        username,
        listOf(
          HmppsAuthSource.NOMIS.value,
          HmppsAuthSource.NDELIUS.value,
          HmppsAuthSource.AZUREAD.value,
        ),
      )
    usersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} failed with error: ${it.type.name}")
    }

    val bySource = mutableMapOf<String, String>()
    usersResponse.data
      ?.content
      ?.filter { it.enabled && !it.locked }
      ?.associateByTo(bySource, { it.source }, { it.username })
    return HmppsAuthUsernames(
      nomisUsername = bySource[HmppsAuthSource.NOMIS.value],
      deliusUsername = bySource[HmppsAuthSource.NDELIUS.value],
      azureAdUsername = bySource[HmppsAuthSource.AZUREAD.value],
    )
  }

  fun getActivePrisonUsersByEmail(emailAddress: String): List<HmppsPrisonUser> {
    val prisonUsersResponse = manageUsersGateway.getPrisonUsersByEmail(emailAddress)
    prisonUsersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} failed with error: ${it.type.name}")
    }
    return prisonUsersResponse.data?.filter { it.enabled } ?: emptyList()
  }

  fun hasPrisonRole(
    emailAddress: String,
    role: HmppsPrisonRole,
  ): Boolean {
    val users = getActivePrisonUsersByEmail(emailAddress)
    val roles =
      users.flatMap { prisonUser ->
        prisonUser.dpsRoleCodes
      }
    return roles.contains(role.name)
  }

  fun userCaseload(emailAddress: String): List<String> {
    val users = getActivePrisonUsersByEmail(emailAddress)
    val caseload =
      users
        .flatMap { user ->
          val response = manageUsersGateway.getPrisonUserCaseload(user.username)
          response.errors.forEach { err ->
            throw RuntimeException("Call to getPrisonUserCaseload in ${err.causedBy.name} failed with error: ${err.type.name}")
          }
          response.data?.caseloads ?: emptyList()
        }.mapNotNull { it.id }
    return caseload
  }

  fun getProbationCaseAccess(
    ndeliusUsername: String,
    crns: List<String>,
  ): UserAccess? {
    val userAccessResponse = probationAccessGateway.getProbationAccess(ndeliusUsername, crns)
    userAccessResponse.errors.forEach { err ->
      throw RuntimeException("Call to getProbationAccess in ${err.causedBy.name} failed with error: ${err.type.name}")
    }
    return userAccessResponse.data
  }
}
