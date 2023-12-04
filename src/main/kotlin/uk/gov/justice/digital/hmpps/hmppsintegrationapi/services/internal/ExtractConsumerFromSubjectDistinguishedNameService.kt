package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

class ExtractConsumerFromSubjectDistinguishedNameService {
  fun execute(subjectDistinguishedName: String?): String? {
    if (subjectDistinguishedName.isNullOrEmpty()) {
      return null
    }

    val match = Regex("^.*,CN=(.*)$").find(subjectDistinguishedName)

    if (match?.groupValues == null) {
      return null
    }

    return match.groupValues[1]
  }
}
