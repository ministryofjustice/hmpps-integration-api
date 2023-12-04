package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthenticationFailedException

@Service
class AuthoriseConsumerService(
  @Autowired private val extractConsumerFromSubjectDistinguishedNameService: ExtractConsumerFromSubjectDistinguishedNameService,
) {
  fun execute(
    subjectDistinguishedName: String,
    consumerPathConfig: Map<String, List<String>>,
    requestedPath: String,
  ): Boolean {
    val consumer = extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)
      ?: throw AuthenticationFailedException("Unable to identify consumer from subject-distinguished-name header")

    val consumerAllowedPaths = consumerPathConfig[consumer]

    if (consumerAllowedPaths.isNullOrEmpty()) {
      throw AuthenticationFailedException("Unable to find allowed paths for consumer $consumer")
    }

    consumerAllowedPaths.forEach {
      if (Regex(it).matches(requestedPath)) {
        return true
      }
    }

    throw AuthenticationFailedException("Requested path $requestedPath not authorised for $consumer")
  }
}
