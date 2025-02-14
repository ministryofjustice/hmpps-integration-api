package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
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
      val filters = ConsumerFilters(null)

      beforeEach {
        Mockito.reset(personalRelationshipsGateway)
        Mockito.reset(getPersonService)

//      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters)).thenReturn(
//        Response(data = null),
//      )
      }
      it("gets a person using a Hmpps ID") {
        getVisitorRestrictionsService.execute(prisonId, hmppsId, contactId, filters)

        verify(getPersonService, VerificationModeFactory.times(1)).getPrisoner(hmppsId = hmppsId, filters = filters)
      }
    },
  )
