package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.stereotype.Component

// Dependency injected as it's a bean
// This will add an entry to /health
@Component("hmppsauth")
class HmppsAuthAPIHealthIndicator : ExternalHealthIndicator("http://localhost:9090/auth/health")
