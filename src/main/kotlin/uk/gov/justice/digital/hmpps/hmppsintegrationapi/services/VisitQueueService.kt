package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Service
class VisitQueueService(
  private val getPersonService: GetPersonService,
  private val hmppsQueueService: HmppsQueueService,
) {
  private val visitsQueue by lazy { hmppsQueueService.findByQueueId("") as HmppsQueue }
  private val visitsQueueSqsClient by lazy { visitsQueue.sqsClient }
  private val visitsQueueUrl by lazy { visitsQueue.queueUrl }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendCreateVisit(
    visit: CreateVisitRequest,
    consumerFilters: ConsumerFilters?,
  ) {
    val visitPrisonerId = visit.prisonerId

    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = visitPrisonerId, filters = consumerFilters)
  }
}
