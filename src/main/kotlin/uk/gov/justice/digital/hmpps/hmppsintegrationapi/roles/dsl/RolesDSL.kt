package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role

class RoleConstants(
  val allEndpoints: MutableList<String>,
)

class RoleConstantsBuilder {
  private val allEndpoints = mutableListOf<String>()

  fun build(): RoleConstants = RoleConstants(allEndpoints)

  fun allEndpoints(init: IncludeBuilder.() -> Unit) {
    allEndpoints.addAll(IncludeBuilder().apply(init).content)
  }
}

fun constants(init: RoleConstantsBuilder.() -> Unit): RoleConstants = RoleConstantsBuilder().apply(init).build()

fun role(
  name: String,
  init: RoleBuilder.() -> Unit,
): Role = RoleBuilder(name).apply(init).build()

class RoleBuilder(
  private val name: String,
) {
  private val includes = mutableListOf<String>()
  private var filters: ConsumerFilters? = null

  fun build(): Role = Role(name, includes, filters)

  fun include(init: IncludeBuilder.() -> Unit) {
    includes.addAll(IncludeBuilder().apply(init).content)
  }

  fun filters(init: FiltersBuilder.() -> Unit) {
    filters = FiltersBuilder().apply(init).build()
  }
}

class IncludeBuilder {
  val content = mutableListOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }

  operator fun MutableList<String>.unaryMinus() {
    content.addAll(this)
  }
}

class PrisonsBuilder {
  val content = mutableListOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }
}

class CaseNotesBuilder {
  val content = mutableListOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }
}

class FiltersBuilder {
  private val prisons = mutableListOf<String>()
  private val caseNotes = mutableListOf<String>()

  fun build(): ConsumerFilters = ConsumerFilters(prisons, caseNotes)

  fun prisons(init: PrisonsBuilder.() -> Unit) {
    prisons.addAll(PrisonsBuilder().apply(init).content)
  }

  fun caseNotes(init: CaseNotesBuilder.() -> Unit) {
    caseNotes.addAll(CaseNotesBuilder().apply(init).content)
  }
}
