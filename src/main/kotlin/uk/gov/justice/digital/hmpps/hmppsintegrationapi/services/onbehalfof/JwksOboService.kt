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

  enum class KeyStatus {
    NEW,
    LOADED,
    FAILED,
  }

  private var keyStatus: KeyStatus = KeyStatus.NEW

  private val keyCache: MutableMap<String, Key> = mutableMapOf()

  override fun extractUsername(token: String): String? {
    loadJwks()

    try {
      val jwt =
        Jwts
          .parser()
          .keyLocator { header -> keyCache[header["kid"]] }
          .build()
          .parseSignedClaims(token)

      log.info("Parsed signed JWT")

      if (!jwt.payload.contains(usernameClaim)) {
        log.warn("Valid JWT does not have required claim: $usernameClaim")
        return null
      }
      return jwt.payload[usernameClaim].toString()
    } catch (e: JwtException) {
      log.error("Unable to parse JWT: " + e.message)
      return null
    }
  }

  fun loadJwks() {
    if (keyStatus() == KeyStatus.LOADED) {
      log.debug("Using cached JWKS")
      return
    }

    val jwkParser = Jwks.parser().build()

    val jsonContent =
      try {
        jwks
          .openStream()
          .bufferedReader()
          .use { reader ->
            jacksonObjectMapper().readTree(reader)
          }
      } catch (e: Exception) {
        keyStatus = KeyStatus.FAILED
        log.error("Unable to load and parse JWKs from $jwks", e)
        return
      }

    log.debug(jsonContent.toString())

    keyCache.putAll(
      jsonContent["keys"].associate {
        it["kid"].asText() to jwkParser.parse(it.toString()).toKey()
      },
    )

    keyStatus = KeyStatus.LOADED
    log.info("Loaded {} public keys from {}}", keyCache.size, jwks.toString())
  }

  fun keyCount() = keyCache.size

  fun keyStatus() = keyStatus
}
