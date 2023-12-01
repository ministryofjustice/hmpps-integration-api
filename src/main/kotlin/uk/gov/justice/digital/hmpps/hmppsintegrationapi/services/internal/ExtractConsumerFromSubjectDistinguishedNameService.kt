package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthorisationFailedException

class ExtractConsumerFromSubjectDistinguishedNameService {
  fun execute(subjectDistinguishedName: String?): String {
    if (subjectDistinguishedName.isNullOrEmpty()) {
      throw AuthorisationFailedException("Missing Subject Distinguished Name")
    }

    val match = Regex("^.*,CN=(.*)$")?.find(subjectDistinguishedName)

    if (match?.groupValues == null) {
      throw AuthorisationFailedException("Could not identify consumer")
    }

    return match.groupValues[1]
  }
}
