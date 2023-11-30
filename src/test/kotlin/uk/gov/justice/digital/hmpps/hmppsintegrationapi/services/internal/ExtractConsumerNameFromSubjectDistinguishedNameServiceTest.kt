package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.CouldNotExtractSubjectDistinguishedNameException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ExtractConsumerNameFromSubjectDistinguishedNameService

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ExtractConsumerNameFromSubjectDistinguishedNameService::class],
)
internal class ExtractConsumerNameFromSubjectDistinguishedNameServiceTest(
  private val extractConsumerNameFromSubjectDistinguishedNameService: ExtractConsumerNameFromSubjectDistinguishedNameService,
) : DescribeSpec(
  {

    it("retrieves only the Common Name from the Subject Distinguished Name") {
      val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=some.client.org"
      val result = extractConsumerNameFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)

      result.shouldBe("some.client.org")
    }

    it("Throws an exception when the subject Distinguished name is an empty string") {
      val subjectDistinguishedName = ""

      shouldThrow<CouldNotExtractSubjectDistinguishedNameException> {
        extractConsumerNameFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)
      }
    }

    it("Throws an exception when the subject Distinguished name is null") {
      val subjectDistinguishedName = null

      shouldThrow<CouldNotExtractSubjectDistinguishedNameException> {
        extractConsumerNameFromSubjectDistinguishedNameService.execute(subjectDistinguishedName)
      }
    }
  },
)
