package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
@Service
class AuthoriseConsumerService {
  fun execute(subjectDistinguishedName: String, consumerPathConfig: Map<String, List<String>>, requestedPath: String, req: HttpServletRequest): Boolean {
    val consumer = extractConsumerFromSubjectDistinguishedNameService(subjectDistinguishedName)

    println("consumer: $consumer")
    println("requestedPath: $requestedPath")

    consumerPathConfig[consumer]?.forEach {
      if (Regex(it).matches(requestedPath)) {
        req.setAttribute("clientName", consumer)
        return true
      }
    }

    return false
  }

  fun extractConsumerFromSubjectDistinguishedNameService(subjectDistinguishedName: String?): String? {
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
