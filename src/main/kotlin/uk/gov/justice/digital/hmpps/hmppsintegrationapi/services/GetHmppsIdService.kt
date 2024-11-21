package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetHmppsIdService(
  @Autowired val getPersonService: GetPersonService,
) {

  private val log: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)

  fun execute(hmppsId: String): Response<HmppsId?> {
    val personResponse = getPersonService.execute(hmppsId.uppercase())

    var hmppsIdToReturn =
      personResponse.data?.hmppsId
    log.info("hmppsId from probation: $hmppsIdToReturn")
    if (hmppsIdToReturn == null) {
      hmppsIdToReturn = getPersonService.getPersonFromNomis(hmppsId.uppercase()).data?.prisonerNumber
      log.info("hmppsId from prison: $hmppsIdToReturn")
    }

    return Response(
      data = HmppsId(hmppsIdToReturn),
      errors = personResponse.errors,
    )
  }

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> {
    val nomisResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    return Response(
      data = NomisNumber(nomisNumber = nomisResponse.data?.nomisNumber),
      errors = nomisResponse.errors,
    )
  }
}
