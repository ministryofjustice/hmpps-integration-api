package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Jwks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.security.Key

/**
 * An on-behalf-of service implementation that validates JWTs using JWKS.
 */
class JwksOboService(
  val jwks: URL,
  val usernameClaim: String,
) : OboService {
  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  val keyCache: MutableMap<String?, Key?> = mutableMapOf()

  override fun extractUsername(token: String): String? {
    loadJwks()

    try {
      val jwt =
        Jwts
          .parser()
          .keyLocator { header -> keyCache[header["kid"]] }
          .build()
          .parseSignedClaims(token)

      return jwt?.payload[usernameClaim]?.toString()
    } catch (e: JwtException) {
      log.error("Unable to parse JWT", e)
      return null
    }
  }

  fun loadJwks() {
    if (!keyCache.isEmpty()) {
      log.debug("Using cached JWKS")
      return
    }

    val jwkParser = Jwks.parser().build()

    val jsonContent =
      jwks
        .openStream()
        .bufferedReader()
        .use { reader ->
          jacksonObjectMapper().readTree(reader)
        }

    log.debug(jsonContent.toString())

    keyCache.putAll(
      jsonContent["keys"].associate {
        it["kid"].asText() to jwkParser.parse(it.toString()).toKey()
      },
    )

    log.info("Loaded {} public keys from {}}", keyCache.size, jwks.toString())
  }
}
