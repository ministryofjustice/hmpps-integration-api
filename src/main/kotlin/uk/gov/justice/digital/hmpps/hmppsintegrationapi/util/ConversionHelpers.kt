package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.withoutNotFound
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

/**
 * Generic Function that returns a single list of data from both the prison and probation domains
 *
 * @param T the data type e.g Address, Offence)
 * @param id
 * @param filters the filters applied to the consumer
 * @param personService the person service bean providing the conversion functions
 * @param probationFunction the function to get the data from the probation domain (if permitted)
 * @param prisonFunction the function to get the data from the prison domain (if permitted)
 * @return the merged list of data
 */

inline fun <reified T> getDataFromBothDomains(
  id: String,
  filters: ConsumerFilters?,
  personService: GetPersonService,
  probationFunction: (id: String) -> Response<List<T>>,
  prisonFunction: (id: String) -> Response<List<T>>,
): Response<List<T>> {
  // Verify that the provided ID exists in its own domain
  val verifyId = personService.verifyId(id)
  if (verifyId.errors.isNotEmpty()) {
    return Response(data = emptyList(), errors = verifyId.errors)
  }

  val prisonerData =
    if (ConsumerFilters.hasPrisonAccess(filters)) {
      val prisonerId = personService.getNomisNumber(id, filters)
      prisonerId.data?.nomisNumber?.let { prisonFunction(it).withoutNotFound() } ?: Response(data = emptyList(), errors = prisonerId.errors)
    } else {
      Response(emptyList(), emptyList())
    }

  val probationData =
    if (ConsumerFilters.hasProbationAccess(filters)) {
      val probationId = personService.convert(id, GetPersonService.IdentifierType.CRN)
      probationId.data?.let { probationFunction(it).withoutNotFound() } ?: Response(data = emptyList(), errors = probationId.errors)
    } else {
      Response(emptyList(), emptyList())
    }

  return Response.merge(listOf(prisonerData, probationData))
}
