package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

// Keep this in sync with the copy in hmpps-integration-events until the code bases are merged

const val DEFAULT_PATH_PLACEHOLDER = "[a-zA-Z0-9_-]+"

fun String.ensurePrefix(prefix: String) = if (startsWith(prefix)) this else "$prefix$this"

fun String.normalisePathPlaceholder(placeholder: String) = replace(placeholder, DEFAULT_PATH_PLACEHOLDER)

fun normalisePath(pathPattern: String): String = pathPattern

fun normalisePathFull(pathPattern: String): String =
  pathPattern
    .normalisePathPlaceholder("{hmppsId}")
    .normalisePathPlaceholder("{prisonId}")
    .normalisePathPlaceholder("{contactId}")
    .normalisePathPlaceholder("{visitReference}")
    .normalisePathPlaceholder("{scheduleId}")
    .normalisePathPlaceholder("{accountCode}")
    .normalisePathPlaceholder("{clientVisitReference}")
    .normalisePathPlaceholder("{imageId}")
    .normalisePathPlaceholder("{clientUniqueRef}")
    .normalisePathPlaceholder("{key}")
    .normalisePathPlaceholder("{id}")
    .normalisePathPlaceholder("[^/]*")
    .normalisePathPlaceholder("[^/]+")
    .normalisePathPlaceholder(".*")
    .removePrefix("^")
    .removeSuffix("$")
    .ensurePrefix("/")
