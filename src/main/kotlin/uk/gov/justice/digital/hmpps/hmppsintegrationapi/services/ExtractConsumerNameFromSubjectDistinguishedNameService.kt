package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

class ExtractConsumerNameFromSubjectDistinguishedNameService {
  fun execute(subjectDistinguishedName: String?): String {
    if (subjectDistinguishedName.isNullOrEmpty()) {
      throw CouldNotExtractSubjectDistinguishedNameException()
    }

    val match = Regex("^.*,CN=(.*)$").find(subjectDistinguishedName)!!

    return match.groupValues[1]
  }
}

class CouldNotExtractSubjectDistinguishedNameException : Throwable()
