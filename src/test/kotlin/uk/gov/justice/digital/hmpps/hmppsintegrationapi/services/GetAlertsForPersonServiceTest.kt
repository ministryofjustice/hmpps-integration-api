package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAlertsForPersonService::class],
)
internal class GetAlertsForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val personService: GetPersonService,
  private val getAlertsForPersonService: GetAlertsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val filters = ConsumerFilters(null)
      val alert = Alert(code = "XA", codeDescription = "Test Alert XA")
      val nonMatchingAlert = Alert(code = "INVALID", codeDescription = "Invalid Alert")

      beforeEach {
        Mockito.reset(nomisGateway)

        whenever(personService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
        whenever(nomisGateway.getAlertsForPerson(hmppsId)).thenReturn(
          Response(
            data =
              listOf(
                alert,
                nonMatchingAlert,
              ),
          ),
        )
      }

      it("gets a nomis number from getPersonService") {
        getAlertsForPersonService.execute(hmppsId, filters)

        verify(personService, VerificationModeFactory.times(1)).getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)
      }

      it("gets alerts from NOMIS using a prisoner number") {
        getAlertsForPersonService.execute(hmppsId, filters)

        verify(nomisGateway, VerificationModeFactory.times(1)).getAlertsForPerson(hmppsId)
      }

      describe("when an upstream API returns an error when looking up nomis number by a Hmmps Id") {

        it("records upstream API errors when failed prison check call") {
          val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
          whenever(personService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(
            Response(
              data = null,
              errors =
              err,
            ),
          )
          val response = getAlertsForPersonService.execute(hmppsId, filters)
          response.errors.shouldHaveSize(1)
          response.errors.shouldBe(err)
        }

        it("failed to get prisoners nomis number") {
          val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND))
          whenever(personService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(), errors = emptyList()))
          val response = getAlertsForPersonService.execute(hmppsId, filters)
          response.errors.shouldBe(err)
        }

        it("does not get alerts from Nomis") {
          whenever(personService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))))
          getAlertsForPersonService.execute(hmppsId, filters)
          verify(nomisGateway, VerificationModeFactory.times(0)).getAlertsForPerson(id = hmppsId)
        }
      }

      it("records errors when it cannot find alerts for a person") {
        whenever(nomisGateway.getAlertsForPerson(id = hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getAlertsForPersonService.execute(hmppsId, filters)
        response.errors.shouldHaveSize(1)
      }

      describe("getAlertsForPnd") {
        beforeEach {
          val deliusCrn = "X777776"
          val person =
            Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = hmppsId, deliusCrn = deliusCrn))

          whenever(personService.execute(hmppsId = deliusCrn)).thenReturn(Response(person))
          whenever(personService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        }

        it("records errors when it cannot find PND alerts for a person") {
          whenever(nomisGateway.getAlertsForPerson(id = hmppsId)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getAlertsForPersonService.getAlertsForPnd(hmppsId)
          response.errors.shouldHaveSize(1)
        }

        it("returns PND filtered data") {
          val response = getAlertsForPersonService.getAlertsForPnd(hmppsId)

          response.data.shouldHaveSize(1)
          response.data[0].code shouldBe "XA"
          response.data[0].codeDescription shouldBe "Test Alert XA"
        }

        it("returns an error when the alert code is not in the allowed list") {
          whenever(nomisGateway.getAlertsForPerson(hmppsId)).thenReturn(
            Response(
              data = listOf(nonMatchingAlert),
            ),
          )

          val response = getAlertsForPersonService.getAlertsForPnd(hmppsId)

          response.errors.shouldHaveSize(1)
          response.data.shouldHaveSize(0)
        }
      }
    },
  )
