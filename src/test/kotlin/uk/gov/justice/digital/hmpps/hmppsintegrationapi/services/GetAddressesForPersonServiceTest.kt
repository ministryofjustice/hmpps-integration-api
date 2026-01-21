package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAddressesForPersonService::class],
)
internal class GetAddressesForPersonServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val personService: GetPersonService,
  @MockitoBean val deliusGateway: NDeliusGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A5553AA"
      val nomisNumber = "A5553AA"
      val filters = null

      val deliusAddress =
        Address(
          country = "UK",
          county = "Middlesex",
          locality = "Some Locality",
          name = "Delius Address",
          noFixedAddress = false,
          number = "123",
          postcode = "AB1 2CD",
          street = "Delius Street",
          town = "Delius Town",
          notes = "Some notes about the address",
          startDate = null,
          endDate = null,
        )

      val nomisAddress =
        Address(
          country = "UK",
          county = "Middlesex",
          locality = "Some Locality",
          name = "Nomis Address",
          noFixedAddress = false,
          number = "123",
          postcode = "AB1 2CD",
          street = "Nomis Street",
          town = "Nomis Town",
          notes = "Some notes about the address",
          startDate = null,
          endDate = null,
        )

      beforeEach {
        Mockito.reset(prisonApiGateway)
        Mockito.reset(personService)
        Mockito.reset(deliusGateway)
        Mockito.reset(featureFlag)
      }

      it("Person service error → Return person service error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.PRISON_API,
              description = "Mock error from person service",
            ),
          )
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBe(errors)
      }

      it("No supervision status filter, Nomis number, Delius success, Nomis success → Merge responses") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(nomisAddress, deliusAddress))
      }

      it("filter contains only a PROBATION SupervisionStatus - only return Delius response") {

        val filters = ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PROBATION.name))

        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(deliusAddress))
      }

      it("filter contains only a PRISON SupervisionStatus - only return Prison response") {

        val filters = ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PRISONS.name))

        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(nomisAddress))
      }

      it("filter contains both a PRISON and PROBATION SupervisionStatus - only return Prison response") {

        val filters = ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PROBATION.name, SupervisionStatus.PRISONS.name))
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(nomisAddress, deliusAddress))
      }

      it("Nomis number, Delius success, Nomis 404 → Ideally return just Delius response") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PRISON_API,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(deliusAddress))
      }

      it("Nomis number, Delius 404, nomis success → Return just NOMIS") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.NDELIUS,
                ),
              ),
          ),
        )
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(nomisAddress))
      }

      it("Nomis number, Delius success, nomis non-404 error → Return NOMIS error") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(listOf(deliusAddress)))
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.PRISON_API,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.PRISON_API)))
      }

      it("Nomis number, Delius 404, nomis any error (incl. 404) → Return just NOMIS") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.NDELIUS,
                ),
              ),
          ),
        )
        whenever(prisonApiGateway.getAddressesForPerson(nomisNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.PRISON_API,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.PRISON_API)))
      }

      it("Nomis number, Delius non-404 error → Return Delius response") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(nomisNumber)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.NDELIUS,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.NDELIUS)))
      }

      it("No nomis number, delius success → return Delius") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(null)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(listOf(deliusAddress)))

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(deliusAddress))
      }

      it("No nomis number, delius any error → return Delius") {
        whenever(personService.getNomisNumber(hmppsId, filters)).thenReturn(Response(NomisNumber(null)))
        whenever(deliusGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.NDELIUS,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId, filters)
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.NDELIUS)))
      }
    },
  )
