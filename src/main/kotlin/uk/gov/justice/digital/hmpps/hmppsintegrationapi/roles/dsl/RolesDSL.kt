package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import kotlin.collections.MutableList

class RoleConstants(
  val allEndpoints: MutableList<String>,
)

class RoleConstantsBuilder {
  private val allEndpoints = mutableListOf<String>()

  fun build(): RoleConstants = RoleConstants(allEndpoints)

  fun allEndpoints(init: PermissionBuilder.() -> Unit) {
    PermissionBuilder().apply(init).content?.let {
      allEndpoints.addAll(it)
    }
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
  private var permissions: MutableList<String>? = null
  private var filters: ConsumerFilters? = null
  private var redactionPolicies: MutableList<RedactionPolicy>? = null

  fun build(): Role = Role(name, permissions, filters, redactionPolicies)

  fun permissions(init: PermissionBuilder.() -> Unit) {
    PermissionBuilder().apply(init).content?.let {
      if (permissions == null) {
        permissions = mutableListOf()
      }
      permissions?.addAll(it)
    }
  }

  fun filters(init: FiltersBuilder.() -> Unit) {
    filters = FiltersBuilder().apply(init).build()
  }

  fun redactionPolicies(init: List<RedactionPolicy>) {
    if (redactionPolicies == null) {
      redactionPolicies = mutableListOf()
    }
    redactionPolicies?.addAll(init)
  }
}

class PermissionBuilder {
  var content: MutableList<String>? = null

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }

  operator fun List<String>.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.addAll(this)
  }
}

class SupervisionStatusBuilder {
  var content: MutableList<String>? = mutableListOf()

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }
}

class PrisonsBuilder {
  var content: MutableList<String>? = mutableListOf()

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }
}

class CaseNotesBuilder {
  var content: MutableList<String>? = null

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }
}

class AlertCodesBuilder {
  var content: MutableList<String>? = null

  operator fun String.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(this)
  }

  operator fun List<String>.unaryMinus() {
    if (content == null) {
      content = mutableListOf()
    }
    content?.addAll(this)
  }
}

class MappaCategoriesBuilder {
  var content: MutableList<Any>? = null

  operator fun MappaCategory.unaryMinus() {
    addContent(this)
  }

  // Wildcard
  operator fun Any.unaryMinus() {
    if ("*" == this) {
      addContent(this)
    }
  }

  private fun addContent(value: Any) {
    if (content == null) {
      content = mutableListOf()
    }
    content?.add(value)
  }
}

class FiltersBuilder {
  private var prisons: MutableList<String>? = null
  private var caseNotes: MutableList<String>? = null
  private var mappaCategories: MutableList<Any>? = null
  private var alertCodes: MutableList<String>? = null
  private var supervisionStatus: MutableList<String>? = null

  fun build(): ConsumerFilters = ConsumerFilters(prisons, caseNotes, mappaCategories, alertCodes, supervisionStatus)

  fun supervisionStatuses(init: SupervisionStatusBuilder.() -> Unit) {
    SupervisionStatusBuilder().apply(init).content?.let {
      if (supervisionStatus == null) {
        supervisionStatus = mutableListOf()
      }
      supervisionStatus?.addAll(it)
    }
  }

  fun prisons(init: PrisonsBuilder.() -> Unit) {
    PrisonsBuilder().apply(init).content?.let {
      if (prisons == null) {
        prisons = mutableListOf()
      }
      prisons?.addAll(it)
    }
  }

  fun caseNotes(init: CaseNotesBuilder.() -> Unit) {
    CaseNotesBuilder().apply(init).content?.let {
      if (caseNotes == null) {
        caseNotes = mutableListOf()
      }
      caseNotes?.addAll(it)
    }
  }

  fun mappaCategories(init: MappaCategoriesBuilder.() -> Unit) {
    MappaCategoriesBuilder().apply(init).content?.let {
      if (mappaCategories == null) {
        mappaCategories = mutableListOf()
      }
      mappaCategories?.addAll(it)
    }
  }

  fun alertCodes(init: AlertCodesBuilder.() -> Unit) {
    AlertCodesBuilder().apply(init).content?.let {
      if (alertCodes == null) {
        alertCodes = mutableListOf()
      }
      alertCodes?.addAll(it)
    }
  }
}

enum class MappaCategory(
  val category: Number?,
) {
  CAT1(1),
  CAT2(2),
  CAT3(3),
  CAT4(4),
  UNKNOWN(null),
  ;

  companion object {
    fun from(category: Number): MappaCategory = entries.firstOrNull { it.category == category } ?: UNKNOWN

    fun all() = entries.filter { it.category != null }
  }
}

enum class SupervisionStatus {
  PRISONS,
  PROBATION,
  NONE,
  UNKNOWN,
}
