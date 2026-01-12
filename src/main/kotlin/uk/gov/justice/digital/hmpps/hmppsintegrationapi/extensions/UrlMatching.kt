package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

// Keep this in sync with the copy in hmpps-integration-events until the code bases are merged

const val DEFAULT_PATH_PLACEHOLDER = "[a-zA-Z0-9_-]+"

fun String.ensurePrefix(prefix: String) = if (startsWith(prefix)) this else "$prefix$this"

fun String.normalisePathPlaceholder(placeholder: String) = replace(placeholder, DEFAULT_PATH_PLACEHOLDER)

/**
 * Normalise a path pattern into a standard regular expression format.
 *
 * Normalised patterns have consistent placeholders for path parameters
 * and so can be easily compared. Normalisation also replaces named
 * parameter placeholders with corresponding regular expression matchers,
 * again in a standardised manner.
 *
 * An explicit list of named parameter placeholders are supported to
 * trigger an analysis of the impact of introducing new placeholder
 * names, including in the integration events codebase.
 */
fun normalisePath(pathPattern: String): String =
  if (pathPattern == "/.*") {
    pathPattern // Wildcard at start of path is not a parameter placeholder
  } else {
    pathPattern
      .normalisePathPlaceholder("{hmppsId}")
      .normalisePathPlaceholder("{prisonId}")
      .normalisePathPlaceholder("{contactId}")
      .normalisePathPlaceholder("{visitReference}")
      .normalisePathPlaceholder("{scheduleId}")
      .normalisePathPlaceholder("{accountCode}")
      .normalisePathPlaceholder("{clientVisitReference}")
      .normalisePathPlaceholder("{imageId}")
      .normalisePathPlaceholder("{key}")
      .normalisePathPlaceholder("{id}")
      .normalisePathPlaceholder("{clientUniqueRef}")
      .normalisePathPlaceholder("{activityId}")
      .normalisePathPlaceholder("{scheduleId}")
      .normalisePathPlaceholder("[^/]*")
      .normalisePathPlaceholder("[^/]+")
      .normalisePathPlaceholder(".*")
      .removePrefix("^")
      .removeSuffix("$")
      .ensurePrefix("/")
  }
