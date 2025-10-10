package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoDynamicRiskRedactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoMappaDetailRedactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoPersonLicencesRedactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoStatusInformationRedactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

@Configuration
class RedactionPolicyConfig(
  private val loaChecker: AccessFor,
) {
  @Bean
  fun laoMappaDetailsRedactionPolicy() =
    redactionPolicy("lao-mappa-details") {
      responseRedactions {
        delegate(LaoMappaDetailRedactor(loaChecker)) {
          paths {
            -"/v1/persons/[^/]+/risks/mappadetail"
          }
        }
      }
    }

  @Bean
  fun laoDynamicRiskRedactionPolicy() =
    redactionPolicy("lao-dynamic-risk") {
      responseRedactions {
        delegate(LaoDynamicRiskRedactor(loaChecker)) {
          paths {
            -"/v1/persons/[^/]+/risks/dynamic"
          }
        }
      }
    }

  @Bean
  fun laoPersonLicencesRedactionPolicy() =
    redactionPolicy("lao-person-licences") {
      responseRedactions {
        delegate(LaoPersonLicencesRedactor(loaChecker)) {
          paths {
            -"/v1/persons/[^/]+/licences/conditions"
          }
        }
      }
    }

  @Bean
  fun laoStatusInformationRedactionPolicy() =
    redactionPolicy("lao-status-information") {
      responseRedactions {
        delegate(LaoStatusInformationRedactor(loaChecker)) {
          paths {
            -"/v1/persons/[^/]+/status-information"
          }
        }
      }
    }

  @Bean
  fun globalRedactions(policies: List<RedactionPolicy>): Map<String, RedactionPolicy> = policies.associateBy { it.name ?: "redaction-policy" }
}
