package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

open class ExternalHealthIndicator(url: String) : HealthIndicator {
  private val healthUrl = url

  //Note, since we're overriding the default health here, we only have control over the overall health status.
  //Unfortunately returning heathStatus.down causes the overall status to be down whereas we only want to fail
  //that specific component.
  override fun health(): Health {
    var healthStatus: Health.Builder = Health.up()
      .withDetail("healthurl", healthUrl)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
      .uri(URI.create(healthUrl))
      .build()

    val response = try {
      client.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (e: Exception) {
      println("Failed to connect to health endpoint $healthUrl")
      return healthStatus.up()
        .withDetail("status", "This component is down")
        .withException(e)
        .build()
    }

    if (response.statusCode() != 200)
      return healthStatus.up()
        .withDetail("httpStatusCode", response.statusCode())
        .build()

    return healthStatus.build()
  }
}
