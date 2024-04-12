package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonableAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.OffenderProfile
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.OtherIds

@ActiveProfiles("test")
@JsonTest
class GetProtectedCharacteristicsServiceTest() {
  lateinit var service: GetProtectedCharacteristicsService
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway = mock()
  private val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway = mock()
  val nomisGateway: NomisGateway = mock()
  val hmppsId: String = "mockId"

  var mockOffender: Offender = Offender("John", "Smith", otherIds = OtherIds(nomsNumber = "mockNomsNumber"), age = 35, gender = "Male", offenderProfile = OffenderProfile(sexualOrientation = "Unknown", ethnicity = "British", nationality = "British", religion = "None", disabilities = emptyList()))
  var mockPrisonOffender: POSPrisoner = POSPrisoner("John", "Smith", maritalStatus = "Widowed", bookingId = "bookingId")
  var mockReasonableAdjustment: ReasonableAdjustment = ReasonableAdjustment(treatmentCode = "abc")

  @BeforeEach
  fun setUp() {
    Mockito.reset(probationOffenderSearchGateway)
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(nomisGateway)
    service = GetProtectedCharacteristicsService(probationOffenderSearchGateway, prisonerOffenderSearchGateway, nomisGateway)
  }

  @Test
  fun `Probation offender search return errors, return error`() {
    whenever(probationOffenderSearchGateway.getOffender(hmppsId)).thenReturn(Response<Offender?>(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PROBATION_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"))))

    val result = service.execute(hmppsId)

    verifyNoInteractions(prisonerOffenderSearchGateway)
    verifyNoInteractions(nomisGateway)
    result.data.shouldBeNull()
    result.errors.shouldHaveSize(1)
    result.errors.first().causedBy.shouldBe(UpstreamApi.PROBATION_OFFENDER_SEARCH)
    result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    result.errors.first().description.shouldBe("MockError")
  }

  @Test
  fun `Probation offender search return no nomsNumber, return only probation data`() {
    val mockOffender: Offender = Offender("John", "Smith", otherIds = OtherIds(), age = 35, gender = "Male", offenderProfile = OffenderProfile(sexualOrientation = "Unknown", ethnicity = "British", nationality = "British", religion = "None", disabilities = emptyList()))
    whenever(probationOffenderSearchGateway.getOffender(hmppsId)).thenReturn(Response<Offender?>(data = mockOffender, errors = emptyList()))

    val result = service.execute(hmppsId)

    verifyNoInteractions(prisonerOffenderSearchGateway)
    verifyNoInteractions(nomisGateway)
    result.data.shouldNotBeNull()
    result.errors.shouldHaveSize(0)
    result.data!!.age.shouldBe(35)
    result.data!!.gender.shouldBe("Male")
    result.data!!.sexualOrientation.shouldBe("Unknown")
    result.data!!.ethnicity.shouldBe("British")
    result.data!!.nationality.shouldBe("British")
    result.data!!.religion.shouldBe("None")
    result.data!!.disabilities.shouldHaveSize(0)
    result.data!!.maritalStatus.shouldBeNull()
    result.data!!.reasonableAdjustments.shouldHaveSize(0)
  }

  @Test
  fun `Prisoner no booking, return data from probation and prison search`() {
    val mockPrisonOffender: POSPrisoner = POSPrisoner("John", "Smith", maritalStatus = "Widowed")

    whenever(probationOffenderSearchGateway.getOffender(hmppsId)).thenReturn(Response(data = mockOffender, errors = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPrisonOffender(mockOffender.otherIds.nomsNumber!!)).thenReturn(Response(data = mockPrisonOffender))

    val result = service.execute(hmppsId)

    verifyNoInteractions(nomisGateway)
    result.data.shouldNotBeNull()
    result.errors.shouldHaveSize(0)
    result.data!!.age.shouldBe(35)
    result.data!!.gender.shouldBe("Male")
    result.data!!.sexualOrientation.shouldBe("Unknown")
    result.data!!.ethnicity.shouldBe("British")
    result.data!!.nationality.shouldBe("British")
    result.data!!.religion.shouldBe("None")
    result.data!!.disabilities.shouldHaveSize(0)
    result.data!!.maritalStatus.shouldBe("Widowed")
    result.data!!.reasonableAdjustments.shouldHaveSize(0)
  }

  @Test
  fun `return reasonable adjustments data`() {
    whenever(probationOffenderSearchGateway.getOffender(hmppsId)).thenReturn(Response(data = mockOffender, errors = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPrisonOffender(mockOffender.otherIds.nomsNumber!!)).thenReturn(Response(data = mockPrisonOffender))
    whenever(nomisGateway.getReasonableAdjustments(mockPrisonOffender.bookingId!!)).thenReturn(Response(data = listOf(mockReasonableAdjustment)))

    val result = service.execute(hmppsId)

    result.data.shouldNotBeNull()
    result.errors.shouldHaveSize(0)
    result.data!!.age.shouldBe(35)
    result.data!!.gender.shouldBe("Male")
    result.data!!.sexualOrientation.shouldBe("Unknown")
    result.data!!.ethnicity.shouldBe("British")
    result.data!!.nationality.shouldBe("British")
    result.data!!.religion.shouldBe("None")
    result.data!!.disabilities.shouldHaveSize(0)
    result.data!!.maritalStatus.shouldBe("Widowed")
    result.data!!.reasonableAdjustments.shouldHaveSize(1)
    result.data!!.reasonableAdjustments.first().treatmentCode.shouldBe("abc")
  }
}
