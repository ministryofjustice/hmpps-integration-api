package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisPersonAccount

@Service
class GetPersonPrisonAccountService(@Autowired val nomisGateway: NomisGateway, @Autowired val getPersonService: GetPersonService) {
  fun execute(hmppsId: String): Response<NomisPersonAccount> {

    val response = getPersonService.getNomisNumber(hmppsId)
    val nomisNumber = if(response.data?.nomisNumber != null) response.data.nomisNumber else null

    if (nomisNumber == null) {
      throw EntityNotFoundException("Could not find nomis number with supplied hmppsId: $hmppsId")
    }

    val prisonerAccount = nomisGateway.getOffendersAccount(nomisNumber)

    if(prisonerAccount.data.cash == null){
      throw EntityNotFoundException("Could not find prisoners accounts with supplier nomisId: $nomisNumber")
    }

    val enrichedPrisonerAccount = appendPersonsIdentifiers(hmppsId, nomisNumber, prisonerAccount.data)

    return Response(
      data = enrichedPrisonerAccount,
      errors = prisonerAccount.errors
    )
  }

  private fun appendPersonsIdentifiers(hmppsId: String, nomisNumber: String, responseObject: NomisPersonAccount): NomisPersonAccount {
    return NomisPersonAccount(
      cash = responseObject.cash,
      currency = responseObject.currency,
      damageObligations = responseObject.damageObligations,
      savings = responseObject.savings,
      spends = responseObject.spends,
      nomisNumber = nomisNumber,
      hmppsId = hmppsId
    )
  }
}
