package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

open class ExternalHealthIndicator(url: String) : HealthIndicator {
  private val healthUrl = url

  override fun health(): Health {
    var healthStatus : Health.Builder = Health.up()
      .withDetail("healthurl", healthUrl)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
      .uri(URI.create(healthUrl))
      .build()

    val response = try {
      client.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (e : Exception){
      println("Failed to connect to HMPPS Auth")
      return healthStatus.down()
        .withDetail("error", e::class.simpleName)
        .build()
    }

    if (response.statusCode() != 200)
      return healthStatus.down()
        .withDetail("errorCode", response.statusCode())
        .build()

    return healthStatus.build()
  }
}