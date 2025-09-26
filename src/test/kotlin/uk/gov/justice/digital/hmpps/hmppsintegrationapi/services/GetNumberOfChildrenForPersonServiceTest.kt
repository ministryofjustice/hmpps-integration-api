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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRNumberOfChildren
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetNumberOfChildrenForPersonService::class],
)
internal class GetNumberOfChildrenForPersonServiceTest(
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getNumberOfChildrenForPersonService: GetNumberOfChildrenForPersonService,
) : DescribeSpec(
    {
      val persona = personInProbationAndNomisPersona
      val hmppsId = persona.identifiers.nomisNumber!!
      val prisonerNumber = hmppsId
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)
      val filters = null
      val numberOfChildrenGatewayResponse = PRNumberOfChildren(numberOfChildren = "2", id = 1, active = true, createdTime = "now", createdBy = "me")
      val numberOfChildren = numberOfChildrenGatewayResponse.toNumberOfChildren()

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(personalRelationshipsGateway)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)).thenReturn(Response(person))
        whenever(personalRelationshipsGateway.getNumberOfChildren(prisonerNumber)).thenReturn(Response(numberOfChildrenGatewayResponse))
      }

      it("performs a search according to hmpps Id") {
        getNumberOfChildrenForPersonService.execute(hmppsId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
      }

      it("should return number of children from gateway") {
        val result = getNumberOfChildrenForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(numberOfChildren)
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if person not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "notfound", filters = filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getNumberOfChildrenForPersonService.execute(hmppsId = "notfound", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if a bad request is made to getPersonService") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
              type = UpstreamApiError.Type.BAD_REQUEST,
            ),
          )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "badRequest", filters = filters)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getNumberOfChildrenForPersonService.execute(hmppsId = "badRequest", filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if personal relationships gateway returns error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(personalRelationshipsGateway.getNumberOfChildren(prisonerNumber)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getNumberOfChildrenForPersonService.execute(hmppsId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
