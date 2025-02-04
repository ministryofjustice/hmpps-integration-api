package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAddressesForPersonService::class],
)
internal class GetAddressesForPersonServiceTest(
  @MockitoBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val personService: GetPersonService,
  private val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "2003/13116M"
      val prisonerNumber = "A5553AA"
      val deliusCrn = "X777776"

      val person =
        Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = prisonerNumber, deliusCrn = deliusCrn))

      val personNoNomis =
        Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(deliusCrn = deliusCrn))

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
        Mockito.reset(probationOffenderSearchGateway)
        Mockito.reset(nomisGateway)
        Mockito.reset(personService)
      }

      it("Person service error → Return person service error") {
        val errors =
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.NOMIS,
              description = "Mock error from person service",
            ),
          )
        whenever(personService.execute(hmppsId)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )
        val result = getAddressesForPersonService.execute(hmppsId)
        result.errors.shouldBe(errors)
      }

      it("Nomis number, Delius success, Nomis success → Merge responses ") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(deliusAddress, nomisAddress))
      }

      it("Nomis number, Delius success, Nomis 404 → Ideally return just Delius response") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(data = listOf(deliusAddress)))
        whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(deliusAddress))
      }

      it("Nomis number, Delius 404, nomis success → Return just NOMIS") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                ),
              ),
          ),
        )
        whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(Response(data = listOf(nomisAddress)))

        val result = getAddressesForPersonService.execute(hmppsId)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(nomisAddress))
      }

      it("Nomis number, Delius success, nomis non-404 error → Return NOMIS error") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(listOf(deliusAddress)))
        whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.NOMIS)))
      }

      it("Nomis number, Delius 404, nomis any error (incl. 404) → Return just NOMIS") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                ),
              ),
          ),
        )
        whenever(nomisGateway.getAddressesForPerson(prisonerNumber)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.NOMIS,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.NOMIS)))
      }

      it("Nomis number, Delius non-404 error → Return Delius response") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)))
      }

      it("No nomis number, delius success → return Delius") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(personNoNomis))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(Response(listOf(deliusAddress)))

        val result = getAddressesForPersonService.execute(hmppsId)
        result.errors.shouldBeEmpty()
        result.data.shouldBe(listOf(deliusAddress))
      }

      it("No nomis number, delius any error → return Delius") {
        whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(personNoNomis))
        whenever(probationOffenderSearchGateway.getAddressesForPerson(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                ),
              ),
          ),
        )

        val result = getAddressesForPersonService.execute(hmppsId)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)))
      }
    },
  )
