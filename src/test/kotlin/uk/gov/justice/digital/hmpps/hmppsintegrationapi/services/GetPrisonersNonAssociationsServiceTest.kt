package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NonAssociationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociationPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociations
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociationsResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonersNonAssociationsService::class],
)
internal class GetPrisonersNonAssociationsServiceTest(
  @MockitoBean val nonAssociationsGateway: NonAssociationsGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getPrisonersNonAssociationsService: GetPrisonersNonAssociationsService,
) : DescribeSpec(
    {
      beforeEach {
        Mockito.reset(nonAssociationsGateway)

        whenever(
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<NonAssociations?>(
            "MDI",
            filters = ConsumerFilters(null),
            upstreamServiceType = UpstreamApi.NON_ASSOCIATIONS,
          ),
        ).thenReturn(Response(data = null, errors = emptyList()))
      }

      val nonAssociations =
        NonAssociations(
          nonAssociations =
            listOf(
              NonAssociation(
                role = "something",
                reason = "Bad Dudes",
                roleDescription = "Bad Dudes",
                reasonDescription = "Bad Dudes",
                restrictionType = "Bad Dudes",
                restrictionTypeDescription = "Bad Dudes",
                comment = "Bad Dudes",
                authorisedBy = "Bad Dudes",
                whenCreated = "Bad Dudes",
                whenUpdated = "Bad Dudes",
                updatedBy = "Bad Dudes",
                isClosed = true,
                closedBy = "Bad Dudes",
                closedReason = "Bad Dudes",
                closedAt = "Bad Dudes",
                otherPrisonerDetails =
                  NonAssociationPrisonerDetails(
                    prisonerNumber = "ABC1234",
                    role = "Bad Dudes",
                    roleDescription = "Bad Dudes",
                    firstName = "Bad Dudes",
                    lastName = "Bad Dudes",
                    prisonId = "MDI",
                    prisonName = "A test prison",
                    cellLocation = "A cell location",
                  ),
                isOpen = true,
                id = 42,
              ),
            ),
        )
      val nonAssociationsResponse = NonAssociationsResponse(nonAssociations = nonAssociations, prisonId = "MDI")

      it("returns a valid response when provided a valid prisoner number") {
        whenever(nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber = "ABC1234")).thenReturn(Response(data = nonAssociationsResponse))

        val response = getPrisonersNonAssociationsService.execute("ABC1234", filters = ConsumerFilters(null))
        response.errors.shouldBeEmpty()
        response.data.shouldBe(nonAssociations)
      }

      it("return a 404 when prisoner not in prison ") {
      }

      it("return a 404 when prisoner not found") {
      }
    },
  )
