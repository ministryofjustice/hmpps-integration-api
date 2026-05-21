package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Jwks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.security.Key

class JwksOboService(
  val jwks: URL = URI.create("https://login.microsoftonline.com/common/discovery/v2.0/keys").toURL(),
  val usernameClaim: String = "unique_name",
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
    } catch (e: UnsupportedJwtException) {
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

    log.info(jsonContent.toString())

    keyCache.putAll(
      jsonContent["keys"].associate {
        it["kid"].asText() to jwkParser.parse(it.toString()).toKey()
      },
    )

    log.info("Loaded {} public keys from {}}", keyCache.size, jwks.toString())
  }
}
