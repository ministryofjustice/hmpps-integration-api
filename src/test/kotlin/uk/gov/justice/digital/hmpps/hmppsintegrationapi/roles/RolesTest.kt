package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roleConstants
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory
import kotlin.test.Test

class RolesTest {
  @Test
  fun `fullAccessRole includes is the same as AllEndpointsRole includes`() {
    assertThat(roles["full-access"]?.permissions).isEqualTo(roles["all-endpoints"]?.permissions)
  }

  @Test
  fun `fullAccessRole includes holds all endpoints`() {
    assertThat(roles["full-access"]?.permissions).isEqualTo(roleConstants.allEndpoints)
  }

  @Test
  fun `ReferenceDataOnlyRole includes has 2 items`() {
    assertThat(roles["reference-data-only"]?.permissions?.size).isEqualTo(2)
  }

  @Test
  fun `full-access filters prisons and caseNotes are not null`() {
    assertThat(roles["full-access"]?.filters?.prisons).isNotNull
    assertThat(roles["full-access"]?.filters?.caseNotes).isNotNull
  }

  @Test
  fun `ext-prisons-private-prison Role filters prisons is null`() {
    assertThat(roles["ext-prisons-private-prison"]?.filters?.prisons).isNull()
  }

  @Test
  fun `ext-hmpps-public-protection role has all 4 mappa categories`() {
    assertThat(roles["ext-hmpps-public-protection"]?.filters?.mappaCategories).isEqualTo(listOf("*"))
  }

  @Test
  fun `ext-hmpps-public-protection-cat4 role has only category 4`() {
    assertThat(roles["ext-hmpps-public-protection-cat4"]?.filters?.mappaCategories).isEqualTo(listOf(MappaCategory.CAT4))
  }

  @Test
  fun `ext-prisons-private-prison role does not have any mappa category filters `() {
    assertThat(roles["ext-prisons-private-prison"]?.filters?.mappaCategories).isNull()
  }

  @Test
  fun `ext-prisons-prisoner-finance role has an empty prison filter`() {
    assertThat(roles["ext-prisons-prisoner-finance"]?.filters?.hasPrisonFilter()).isEqualTo(true)
    assertThat(roles["ext-prisons-prisoner-finance"]?.filters?.prisons).isEmpty()
  }

  @Test
  fun `ext-prisons-escort-custody has a null prison filter`() {
    assertThat(roles["ext-prisons-escort-custody"]?.filters?.hasPrisonFilter()).isNotEqualTo(true)
    assertThat(roles["ext-prisons-escort-custody"]?.filters?.prisons).isNull()
  }
}
