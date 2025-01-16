package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetTransactionForPersonService::class],
)
internal class GetTransactionForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getTransactionForPersonService: GetTransactionForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val prisonId = "ABC"
    val clientUniqueRef = "client_unique_ref"
    val filters = ConsumerFilters(null)
    val exampleTransaction = Transaction("204564839-3", Type(code = "spends", desc = "Spends desc"), "Spends account code", 12345, "2016-10-21")

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nomisGateway)

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(
        nomisGateway.getTransactionForPerson(
          prisonId,
          nomisNumber,
          clientUniqueRef,
        ),
      ).thenReturn(
        Response(
          data = exampleTransaction,
        ),
      )
    }

    it("gets a person using a Hmpps ID") {
      getTransactionForPersonService.execute(
        hmppsId,
        prisonId,
        clientUniqueRef,
        filters,
      )

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumber(hmppsId = hmppsId)
    }

    it("gets a transaction from NOMIS using a unique client reference number") {
      getTransactionForPersonService.execute(
        hmppsId,
        prisonId,
        clientUniqueRef,
        filters,
      )

      verify(nomisGateway, VerificationModeFactory.times(1)).getTransactionForPerson(
        prisonId,
        nomisNumber,
        clientUniqueRef,
      )
    }

    it("returns a transaction given a Hmpps ID") {
      val result =
        getTransactionForPersonService.execute(
          hmppsId,
          prisonId,
          clientUniqueRef,
          filters,
        )

      result.data.shouldBe(exampleTransaction)
    }

    it("records upstream API errors") {
      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )
      val response =
        getTransactionForPersonService.execute(
          hmppsId,
          prisonId,
          clientUniqueRef,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("records upstream API errors when hmppsID is invalid") {
      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )
      val response =
        getTransactionForPersonService.execute(
          hmppsId,
          prisonId,
          clientUniqueRef,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records upstream API errors when getTransactionForPerson returns errors") {
      whenever(
        nomisGateway.getTransactionForPerson(
          prisonId,
          nomisNumber,
          clientUniqueRef,
        ),
      ).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )
      val response =
        getTransactionForPersonService.execute(
          hmppsId,
          prisonId,
          clientUniqueRef,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("returns null when a transaction is requested from an unapproved prison") {
      val wrongPrisonId = "XYZ"
      val result =
        getTransactionForPersonService.execute(
          hmppsId,
          wrongPrisonId,
          clientUniqueRef,
          ConsumerFilters(prisons = listOf("ABC")),
        )

      result.data.shouldBe(null)
      result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    it("returns a transaction when requested from an approved prison") {
      val result =
        getTransactionForPersonService.execute(
          hmppsId,
          prisonId,
          clientUniqueRef,
          ConsumerFilters(prisons = listOf(prisonId)),
        )

      result.data.shouldBe(exampleTransaction)
    }
  })
