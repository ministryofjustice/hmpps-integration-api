package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl

class Role(
  val include: MutableSet<String>,
)

fun role(init: RoleBuilder.() -> Unit): Role = RoleBuilder().apply(init).build()

class RoleBuilder {
  private val includes = mutableSetOf<String>()

  fun build(): Role = Role(includes)

  fun include(init: IncludeBuilder.() -> Unit) {
    includes.addAll(IncludeBuilder().apply(init).content)
  }
}

class IncludeBuilder {
  val content = mutableSetOf<String>()

  operator fun String.unaryMinus() {
    content.add(this)
  }
}
