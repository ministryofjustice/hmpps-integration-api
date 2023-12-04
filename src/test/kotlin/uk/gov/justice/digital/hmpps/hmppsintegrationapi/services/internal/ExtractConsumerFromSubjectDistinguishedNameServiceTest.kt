package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthorisationFailedException

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ExtractConsumerFromSubjectDistinguishedNameService::class],
)
internal class ExtractConsumerFromSubjectDistinguishedNameServiceTest(
  private val extractConsumerFromSubjectDistinguishedNameService: ExtractConsumerFromSubjectDistinguishedNameService,
) : DescribeSpec(
  {

    it("retrieves only the Common Name from the Subject Distinguished Name") {
      val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=some.client.org"
      val result = extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)

      result.shouldBe("some.client.org")
    }

    it("It matches only the Common Name and no other attributes") {
      val subjectDistinguishedName = "C=CN,ST=CN,L=CN,O=CN,CN=some.client.org"
      val result = extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)

      result.shouldBe("some.client.org")
    }

    describe("When an invalid Subject Distinguished Name was supplied") {
      it("Throws an exception when it is empty") {
        val subjectDistinguishedName = ""

        shouldThrow<AuthorisationFailedException> {
          extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)
        }
      }

      it("Throws an exception when it is null") {
        val subjectDistinguishedName = null

        shouldThrow<AuthorisationFailedException> {
          extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)
        }
      }

      it("Throws an exception when there is no Common Name (CN)") {
        val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office"

        shouldThrow<AuthorisationFailedException> {
          extractConsumerFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)
        }
      }
    }
  },
)