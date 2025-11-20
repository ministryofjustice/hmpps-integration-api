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
  fun `Private Prison Role filters prisons is null`() {
    assertThat(roles["private-prison"]?.filters?.prisons).isNull()
  }

  @Test
  fun `Mappa role has all 4 mappa categories`() {
    assertThat(roles["mappa"]?.filters?.mappaCategories).isEqualTo(listOf("*"))
  }

  @Test
  fun `Mappa Cat 4 role has only category 4`() {
    assertThat(roles["mappa-cat4"]?.filters?.mappaCategories).isEqualTo(listOf(MappaCategory.CAT4))
  }

  @Test
  fun `private-prison role does not have any mappa category filters `() {
    assertThat(roles["private-prison"]?.filters?.mappaCategories).isNull()
  }

  @Test
  fun `prisoner-finance role has an empty prison filter`() {
    assertThat(roles["prisoner-finance"]?.filters?.hasPrisonFilter()).isEqualTo(true)
    assertThat(roles["prisoner-finance"]?.filters?.prisons).isEmpty()
  }
}
