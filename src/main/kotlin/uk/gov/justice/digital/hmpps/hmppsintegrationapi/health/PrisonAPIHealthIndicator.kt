package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.net.ConnectException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

//Dependency injected as it's a bean
//This will add an entry to /health
@Component("prisonapi")
class PrisonAPIHealthIndicator : ExternalHealthIndicator("http://localhost:8081/health/ping")