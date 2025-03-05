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
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestOffence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetOffencesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/case/$deliusCrn/supervisions"
      val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)

      beforeEach {
        nDeliusApiMockServer.start()
        nDeliusApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetSupervisionsResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nDeliusApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nDeliusGateway.getOffencesForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns offences for the matching CRN") {
        val response = nDeliusGateway.getOffencesForPerson(deliusCrn)

        response.data.shouldBe(
          listOf(
            generateTestOffence(
              cjsCode = null,
              hoCode = "10501",
              courtDates =
                listOf(
                  LocalDate.parse("2009-07-07"),
                  LocalDate.parse("2009-07-07"),
                ),
              courtName = "Llandudno Magistrates Court",
              description = "Common assault and battery - 10501",
              endDate = null,
              startDate = LocalDate.parse("2009-03-31"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "05800",
              courtDates =
                listOf(
                  LocalDate.parse("2009-07-07"),
                  LocalDate.parse("2009-07-07"),
                ),
              courtName = "Llandudno Magistrates Court",
              description = "Other Criminal Damage (including causing explosion)  - 05800",
              endDate = null,
              startDate = LocalDate.parse("2009-03-31"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "12511",
              courtDates =
                listOf(
                  LocalDate.parse("2009-09-01"),
                  LocalDate.parse("2009-08-11"),
                ),
              courtName = "Llandudno Magistrates Court",
              description = "Threatening behaviour, fear or provocation of violence (Public Order Act 1986) - 12511",
              endDate = null,
              startDate = LocalDate.parse("2009-07-31"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "03900",
              courtDates =
                listOf(
                  LocalDate.parse("2009-09-01"),
                  LocalDate.parse("2009-08-11"),
                ),
              courtName = "Llandudno Magistrates Court",
              description = "Stealing from the person of another - 03900",
              endDate = null,
              startDate = LocalDate.parse("2009-08-14"),
              statuteCode = null,
            ),
            generateTestOffence(
              cjsCode = null,
              hoCode = "99902",
              courtDates =
                listOf(
                  LocalDate.parse("2009-09-01"),
                  LocalDate.parse("2009-08-11"),
                ),
              courtName = "Llandudno Magistrates Court",
              description = "Migrated Breach Offences",
              endDate = null,
              startDate = LocalDate.parse("1900-01-01"),
              statuteCode = null,
            ),
          ),
        )
      }

      it("returns an empty list if no offences are found") {
        nDeliusApiMockServer.stubForGet(
          path,
          """
          {
          "communityManager": {},
          "mappaDetail": null,
          "supervisions": [],
          "dynamicRisks": [],
          "personStatus": []
          }
          """,
        )

        val response = nDeliusGateway.getOffencesForPerson(deliusCrn)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nDeliusApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = nDeliusGateway.getOffencesForPerson(deliusCrn)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NDELIUS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
