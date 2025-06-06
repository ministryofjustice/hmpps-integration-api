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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociationPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociations
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
      val prisonId = "MDI"
      val prisonerNumber = "ABC1234"
      val filters = null
      val nonAssociations =
        NonAssociations(
          nonAssociations =
            listOf(
              NonAssociation(
                role = "something",
                reason = "string",
                roleDescription = "string",
                reasonDescription = "string",
                restrictionType = "string",
                restrictionTypeDescription = "string",
                comment = "string",
                authorisedBy = "string",
                whenCreated = "string",
                whenUpdated = "string",
                updatedBy = "string",
                isClosed = true,
                closedBy = "string",
                closedReason = "string",
                closedAt = "string",
                otherPrisonerDetails =
                  NonAssociationPrisonerDetails(
                    prisonerNumber = prisonerNumber,
                    role = "string",
                    roleDescription = "string",
                    firstName = "string",
                    lastName = "string",
                    prisonId = "MDI",
                    prisonName = "A test prison",
                    cellLocation = "A cell location",
                  ),
                isOpen = true,
                id = 42,
              ),
            ),
        )

      beforeEach {
        Mockito.reset(nonAssociationsGateway)

        whenever(
          consumerPrisonAccessService.checkConsumerHasPrisonAccess<NonAssociations?>(
            prisonId,
            filters,
            upstreamServiceType = UpstreamApi.NON_ASSOCIATIONS,
          ),
        ).thenReturn(Response(data = null, errors = emptyList()))
        whenever(nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber)).thenReturn(Response(data = nonAssociations))
      }

      it("returns a valid response when provided a valid prisoner number") {
        val response = getPrisonersNonAssociationsService.execute(prisonerNumber, prisonId, filters = filters)
        response.errors.shouldBeEmpty()
        response.data.shouldBe(nonAssociations)
      }

      it("return a 404 when prisoner not in prison ") {
        val wrongPrisonId = "XYZ"
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NON_ASSOCIATIONS,
              description = "No prison associated with prisoner",
            ),
          )

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<NonAssociations?>(wrongPrisonId, ConsumerFilters(listOf("MDI")), upstreamServiceType = UpstreamApi.NON_ASSOCIATIONS)).thenReturn(
          Response(data = null, errors = errors),
        )

        val response = getPrisonersNonAssociationsService.execute(prisonerNumber, wrongPrisonId, filters = ConsumerFilters(listOf("MDI")))
        response.errors.shouldBe(errors)
        response.data.shouldBe(null)
      }

      it("return a 404 when prisoner not found") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NON_ASSOCIATIONS,
              description = "Not found",
            ),
          )
        whenever(nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber)).thenReturn(Response(data = null, errors = errors))

        val response = getPrisonersNonAssociationsService.execute(prisonerNumber, prisonId, filters = filters)
        response.errors.shouldBe(errors)
        response.data.shouldBe(null)
      }
    },
  )
