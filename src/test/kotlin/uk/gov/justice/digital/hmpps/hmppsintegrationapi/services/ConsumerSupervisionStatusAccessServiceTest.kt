package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerSupervisionStatusAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ConsumerSupervisionStatusAccessService::class],
)
internal class ConsumerSupervisionStatusAccessServiceTest(
  private val service: ConsumerSupervisionStatusAccessService,
) : DescribeSpec(
    {
      val inPrisonPerson =
        POSPrisoner(
          firstName = "John",
          lastName = "Doe",
          youthOffender = false,
          prisonId = "MDI",
          inOutStatus = "IN",
          status = "ACTIVE_IN",
        )

      val atCourtPerson =
        POSPrisoner(
          firstName = "John",
          lastName = "Doe",
          youthOffender = false,
          prisonId = "MDI",
          inOutStatus = "IN",
          status = "ACTIVE_OUT",
        )

      val personOnProbation =
        POSPrisoner(
          firstName = "John",
          lastName = "Doe",
          youthOffender = false,
          prisonId = "MDI",
          inOutStatus = "OUT",
          status = "INACTIVE_OUT",
        )

      val nullSupervisionStatuses = ConsumerFilters(supervisionStatuses = null)
      val emptySupervisionStatuses = ConsumerFilters(supervisionStatuses = emptyList())
      val probationOnly = ConsumerFilters(supervisionStatuses = listOf("PROBATION"))
      val prisonOnly = ConsumerFilters(supervisionStatuses = listOf("PRISON"))
      val prisonAndProbation = ConsumerFilters(supervisionStatuses = listOf("PROBATION", "PRISON"))

      it("Should return true for inPrison when supervision filters are null") {
        val result = service.checkConsumerHasSupervisionStatusAccess(inPrisonPerson, nullSupervisionStatuses)
        result.shouldBe(true)
      }

      it("Should return false for inPrison when supervision filters are empty") {
        val result = service.checkConsumerHasSupervisionStatusAccess(inPrisonPerson, emptySupervisionStatuses)
        result.shouldBe(false)
      }

      it("Should return true for inPrison when supervision filters are both") {
        val result = service.checkConsumerHasSupervisionStatusAccess(inPrisonPerson, prisonAndProbation)
        result.shouldBe(true)
      }

      it("Should return false for inPrison when supervision filters is probation only") {
        val result = service.checkConsumerHasSupervisionStatusAccess(inPrisonPerson, probationOnly)
        result.shouldBe(false)
      }

      it("Should return true for inPrison when supervision filters are in prison only") {
        val result = service.checkConsumerHasSupervisionStatusAccess(inPrisonPerson, prisonOnly)
        result.shouldBe(true)
      }
      it("Should return true for atCourt when supervision filters are in prison only") {
        val result = service.checkConsumerHasSupervisionStatusAccess(inPrisonPerson, prisonOnly)
        result.shouldBe(true)
      }

      it("Should return true for onProbation when supervision filters are null") {
        val result = service.checkConsumerHasSupervisionStatusAccess(personOnProbation, nullSupervisionStatuses)
        result.shouldBe(true)
      }

      it("Should return false for onProbation when supervision filters are empty") {
        val result = service.checkConsumerHasSupervisionStatusAccess(personOnProbation, emptySupervisionStatuses)
        result.shouldBe(false)
      }

      it("Should return true for onProbation when supervision filters are both") {
        val result = service.checkConsumerHasSupervisionStatusAccess(personOnProbation, prisonAndProbation)
        result.shouldBe(true)
      }

      it("Should return true for onProbation when supervision filters is probation only") {
        val result = service.checkConsumerHasSupervisionStatusAccess(personOnProbation, probationOnly)
        result.shouldBe(true)
      }

      it("Should return false for onProbation when supervision filters are in prison only") {
        val result = service.checkConsumerHasSupervisionStatusAccess(personOnProbation, prisonOnly)
        result.shouldBe(false)
      }
    },
  )
