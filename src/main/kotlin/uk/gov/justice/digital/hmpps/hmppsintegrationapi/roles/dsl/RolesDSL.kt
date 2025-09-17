package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl

class Role(
  val name: String,
  val include: MutableSet<String>,
  val filters: Filters? = null,
)

class Filters(
  val prisons: MutableSet<String>,
  val caseNotes: MutableSet<String>,
)

fun role(
  name: String,
  init: RoleBuilder.() -> Unit,
): Role = RoleBuilder(name).apply(init).build()

fun constants(init: RoleBuilder.() -> Unit): Role = RoleBuilder("constants").apply(init).build()

class RoleBuilder(
  private val name: String,
) {
  private val includes = mutableSetOf<String>()
  private var filters: Filters? = null

  fun build(): Role = Role(name, includes, filters)

  fun include(init: IncludeBuilder.() -> Unit) {
    includes.addAll(IncludeBuilder().apply(init).content)
  }

  fun filters(init: FiltersBuilder.() -> Unit) {
    filters = FiltersBuilder().apply(init).build()
  }
}

class IncludeBuilder {
  val content = mutableSetOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }

  operator fun MutableSet<String>.unaryMinus() {
    content.addAll(this)
  }
}

class PrisonsBuilder {
  val content = mutableSetOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }
}

class CaseNotesBuilder {
  val content = mutableSetOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }
}

class FiltersBuilder {
  private val prisons = mutableSetOf<String>()
  private val caseNotes = mutableSetOf<String>()

  fun build(): Filters = Filters(prisons, caseNotes)

  fun prisons(init: PrisonsBuilder.() -> Unit) {
    prisons.addAll(PrisonsBuilder().apply(init).content)
  }

  fun caseNotes(init: CaseNotesBuilder.() -> Unit) {
    caseNotes.addAll(CaseNotesBuilder().apply(init).content)
  }
}
