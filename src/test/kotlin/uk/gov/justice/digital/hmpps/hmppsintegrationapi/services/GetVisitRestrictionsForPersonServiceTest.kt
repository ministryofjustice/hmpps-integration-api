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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitRestrictionsForPersonService::class],
)
class GetVisitRestrictionsForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getVisitRestrictionsForPersonService: GetVisitRestrictionsForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val prisonId = "ABC"
    val filters = ConsumerFilters(null)
    val examplePersonVisitRestrictions =
      listOf(
        PersonVisitRestriction(restrictionId = 1, comment = "Restriction 1", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
        PersonVisitRestriction(restrictionId = 2, comment = "Restriction 2", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
      )
    val prisoner = POSPrisoner(firstName = "Meatball", lastName = "Man", prisonId = prisonId)

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(getPersonService)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters)).thenReturn(
        Response(data = null),
      )

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(Response(data = prisoner))
      whenever(nomisGateway.getOffenderVisitRestrictions(nomisNumber)).thenReturn(Response(data = examplePersonVisitRestrictions))
    }

    it("gets a person using a Hmpps ID") {
      getVisitRestrictionsForPersonService.execute(
        hmppsId,
        filters,
      )

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumber(hmppsId = hmppsId)
    }

    it("returns visitor restrictions for a valid HMPPS ID") {
      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response.data shouldBe (examplePersonVisitRestrictions)
    }

    it("returns null when a person in an unapproved prison") {
      val consumerFilters = ConsumerFilters(prisons = listOf("ABC"))
      val wrongPrisonId = "XYZ"
      val prisonerInWrongPrison = POSPrisoner(firstName = "Meatball", lastName = "Man", prisonId = wrongPrisonId)

      whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(Response(data = prisonerInWrongPrison))

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(wrongPrisonId, consumerFilters)).thenReturn(
        Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
      )

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, consumerFilters)

      response.data.shouldBe(null)
      response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    it("returns visit restrictions when requested from an approved prison") {
      val consumerFilters = ConsumerFilters(prisons = listOf("ABC"))
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, consumerFilters)).thenReturn(
        Response(data = null),
      )

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, consumerFilters)
      response.data.shouldBe(examplePersonVisitRestrictions)
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

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records upstream API errors when Nomis number is null") {
      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(
          data = NomisNumber(nomisNumber = null),
        ),
      )

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("records upstream API errors when prisonerOffenderSearchGateway.getPrisonOffender returns an error") {
      whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              ),
            ),
        ),
      )

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records upstream API errors when nomisGateway.getOffenderVisitRestrictions returns an error") {
      whenever(nomisGateway.getOffenderVisitRestrictions(nomisNumber)).thenReturn(
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

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
