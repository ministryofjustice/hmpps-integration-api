package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCommunityOffenderManagerForPersonService::class],
)
class GetCommunityOffenderManagerForPersonServiceTest(
  @MockitoBean val nDeliusGateway: NDeliusGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getCommunityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val deliusCrn = "X224466"
      val filter = null

      val person =
        Person(
          firstName = "Sam",
          lastName = "Smith",
          identifiers = Identifiers(deliusCrn = deliusCrn),
          religion = "Agnostic",
          raceCode = "W1",
          nationality = "Egyptian",
        )
      val communityOffenderManager = CommunityOffenderManager(name = PersonResponsibleOfficerName(forename = "Michael", surname = "Green"), email = "email@email.com", telephoneNumber = "07471234567", team = PersonResponsibleOfficerTeam(code = "Code2", description = "Service description", email = "email2@email2.com", telephoneNumber = "07170987654"))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(nDeliusGateway)

        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filter)).thenReturn(Response(person))
        whenever(nDeliusGateway.getCommunityOffenderManagerForPerson(crn = deliusCrn)).thenReturn(Response(communityOffenderManager))
      }

      it("performs a search according to hmpps Id") {
        getCommunityOffenderManagerForPersonService.execute(hmppsId, filter)
        verify(getPersonService, times(1)).getPersonWithPrisonFilter(hmppsId = hmppsId, filter)
      }

      it("Returns a community offender manager for person given a hmppsId") {
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId, filter)).thenReturn(
          Response(
            data = person,
          ),
        )

        val result = getCommunityOffenderManagerForPersonService.execute(hmppsId, filter)
        result.shouldBe(Response(data = communityOffenderManager))
      }

      it("should return a list of errors if getPersonService returns errors") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getPersonWithPrisonFilter(hmppsId = "NOT_FOUND", filter)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getCommunityOffenderManagerForPersonService.execute("NOT_FOUND", filter)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("Should return null if nDelius gateway returns 404") {
        whenever(nDeliusGateway.getCommunityOffenderManagerForPerson(crn = deliusCrn)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(UpstreamApi.NDELIUS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )

        val result = getCommunityOffenderManagerForPersonService.execute(hmppsId, filter)
        result.data.shouldBe(null)
        result.errors.shouldBeEmpty()
      }

      it("Should return errors if nDelius gateway returns non 404 errors") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NDELIUS,
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              description = "Error returned by nDelius gateway",
            ),
          )
        whenever(nDeliusGateway.getCommunityOffenderManagerForPerson(crn = deliusCrn)).thenReturn(
          Response(
            data = null,
            errors = errors,
          ),
        )

        val result = getCommunityOffenderManagerForPersonService.execute(hmppsId, filter)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
