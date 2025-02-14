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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitorRestrictionsService::class],
)
class GetVisitorRestrictionsServiceTest(
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getVisitorRestrictionsService: GetVisitorRestrictionsService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val prisonId = "ABC"
      val contactId = 123456L
      val nomisNumber = "Z99999ZZ"
      val filters = ConsumerFilters(null)

      beforeEach {
        Mockito.reset(personalRelationshipsGateway)
        Mockito.reset(getPersonService)

//      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters)).thenReturn(
//        Response(data = null),
//      )
      }
      it("gets a person using a Hmpps ID") {
        getVisitorRestrictionsService.execute(hmppsId, contactId, filters)

        verify(getPersonService, VerificationModeFactory.times(1)).getPrisoner(hmppsId = hmppsId, filters = filters)
      }

      it("returns an error from person service when prisoner is not found") {
        val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH, description = "not found."))
        whenever(getPersonService.getPrisoner(hmppsId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getVisitorRestrictionsService.execute(hmppsId, contactId, filters)
        result.data.shouldBe(emptyList())
        result.errors.shouldBe(errors)
      }

      it("returns null when a person in an unapproved prison") {
        val consumerFilters = ConsumerFilters(prisons = listOf("ABC"))
        val wrongPrisonId = "XYZ"

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(wrongPrisonId, consumerFilters)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
        )

        val response = getVisitorRestrictionsService.execute(hmppsId, contactId, filters)

        response.data.shouldBe(null)
        response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }
    },
  )
