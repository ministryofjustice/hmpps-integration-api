package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
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
  @MockBean val nDeliusGateway: NDeliusGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getCommunityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val deliusCrn = "X224466"

      val person = Person(firstName = "Sam", lastName = "Smith", identifiers = Identifiers(deliusCrn = deliusCrn))

      val communityOffenderManager = CommunityOffenderManager(name = PersonResponsibleOfficerName(forename = "Michael", surname = "Green"), email = "email@email.com", telephoneNumber = "07471234567", team = PersonResponsibleOfficerTeam(code = "Code2", description = "Service description", email = "email2@email2.com", telephoneNumber = "07170987654"))

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(nDeliusGateway)

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
        whenever(nDeliusGateway.getCommunityOffenderManagerForPerson(id = deliusCrn)).thenReturn(Response(communityOffenderManager))
      }

      it("performs a search according to hmpps Id") {
        getCommunityOffenderManagerForPersonService.execute(hmppsId)
        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("Returns a community offender manager for person given a hmppsId") {
        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = person,
          ),
        )
        val result = getCommunityOffenderManagerForPersonService.execute(hmppsId)
        result.shouldBe(Response(data = communityOffenderManager))
      }

      it("should return a list of errors if person not found") {
        whenever(getPersonService.execute(hmppsId = "NOT_FOUND")).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NDELIUS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = getCommunityOffenderManagerForPersonService.execute("NOT_FOUND")
        result.data.shouldBe(CommunityOffenderManager())
        result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
