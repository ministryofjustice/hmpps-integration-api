package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResponseResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonerContactsService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonerId: String,
    page: Int,
    size: Int,
    filter: ConsumerFilters?,
  ): Response<PaginatedPrisonerContacts?> {
    val (nomisNumber) =
      getPersonService.getNomisNumberWithPrisonFilter(prisonerId, filter).toResult().let {
        when (it) {
          is ResponseResult.Success -> it.data
          is ResponseResult.Failure -> return Response(data = null, it.errors)
        }
      }

    val response = personalRelationshipsGateway.getContacts(nomisNumber, page, size)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }

    return Response(data = response.data?.toPaginatedPrisonerContacts())
  }
}
