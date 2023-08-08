package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestOffence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NDeliusApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetOffencesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) :
  DescribeSpec(
    {
      val nDeliusApiMockServer = NDeliusApiMockServer()
      val crn = "X777776"

      beforeEach {
        nDeliusApiMockServer.start()
        nDeliusApiMockServer.stubGetOffencesForPerson(
          crn,
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetOffencesResponse.json").readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nDeliusApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nDeliusGateway.getOffencesForPerson(crn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns offences for the matching CRN") {
        val response = nDeliusGateway.getOffencesForPerson(crn)

        response.data.shouldBe(
          listOf(
            generateTestOffence(
              cjsCode = null,
              hoCode = "10501",
              courtDates = listOf(
                LocalDate.parse("2009-07-07"),
                LocalDate.parse("2009-07-07"),
              ),
              description = "Common assault and battery - 10501",
              endDate = null,
              startDate = LocalDate.parse("2009-03-31"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "05800",
              courtDates = listOf(
                LocalDate.parse("2009-07-07"),
                LocalDate.parse("2009-07-07"),
              ),
              description = "Other Criminal Damage (including causing explosion)  - 05800",
              endDate = null,
              startDate = LocalDate.parse("2009-03-31"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "12511",
              courtDates = listOf(
                LocalDate.parse("2009-09-01"),
                LocalDate.parse("2009-08-11"),
              ),
              description = "Threatening behaviour, fear or provocation of violence (Public Order Act 1986) - 12511",
              endDate = null,
              startDate = LocalDate.parse("2009-07-31"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "03900",
              courtDates = listOf(
                LocalDate.parse("2009-09-01"),
                LocalDate.parse("2009-08-11"),
              ),
              description = "Stealing from the person of another - 03900",
              endDate = null,
              startDate = LocalDate.parse("2009-08-14"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "99902",
              courtDates = listOf(
                LocalDate.parse("2009-09-01"),
                LocalDate.parse("2009-08-11"),
              ),
              description = "Migrated Breach Offences",
              endDate = null,
              startDate = LocalDate.parse("1900-01-01"),
              statuteCode = null,
            ),
          ),
        )
      }

      it("returns an empty list if no offences are found") {
        nDeliusApiMockServer.stubGetOffencesForPerson(
          crn,
          """
          { "supervisions": [] }
          """,
        )

        val response = nDeliusGateway.getOffencesForPerson(crn)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nDeliusApiMockServer.stubGetOffencesForPerson(crn, "", HttpStatus.NOT_FOUND)

        val response = nDeliusGateway.getOffencesForPerson(crn)

        response.errors.shouldHaveSize(1)
        response.errors.first().causedBy.shouldBe(UpstreamApi.NDELIUS)
        response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
