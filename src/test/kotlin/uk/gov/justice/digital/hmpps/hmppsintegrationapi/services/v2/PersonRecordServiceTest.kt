package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import jakarta.validation.ValidationException
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CPRName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import kotlin.test.Test

class PersonRecordServiceTest {
  val corePersonRecordGateway: CorePersonRecordGateway = mock(CorePersonRecordGateway::class.java)
  val service = PersonRecordService(corePersonRecordGateway)
  val requestContext = buildRequestContext()
  val successResponse =
    CorePersonRecord(name = CPRName("John", "Smith"))

  @Test
  fun `should successfully get a person for a person for a NOMIS`() {
    whenever(corePersonRecordGateway.corePersonRecordFor(GetPersonService.IdentifierType.NOMS, "A1234DC", requestContext)).thenReturn(successResponse)
    val response = service.personRecord("A1234DC", requestContext)
    response shouldBe Response(successResponse)
  }

  @Test
  fun `should successfully get a person for a person for a CRN`() {
    whenever(corePersonRecordGateway.corePersonRecordFor(GetPersonService.IdentifierType.CRN, "A123456", requestContext)).thenReturn(successResponse)
    val response = service.personRecord("A123456", requestContext)
    response shouldBe Response(successResponse)
  }

  @Test
  fun `should throw a not found exception when the gateway returns not found`() {
    whenever(corePersonRecordGateway.corePersonRecordFor(GetPersonService.IdentifierType.CRN, "A123456", requestContext))
      .thenThrow(EntityNotFoundException("Not found"))
    val exception = shouldThrow<EntityNotFoundException> { service.personRecord("A123456", requestContext) }
    exception.message shouldBe "Not found"
  }

  @Test
  fun `should return a bad request when supplied with neither a CRN or NOMIS`() {
    val exception = shouldThrow<ValidationException> { service.personRecord("671230K", requestContext) }
    exception.message.shouldContain("Not a valid HMPPS Id")
  }
}
