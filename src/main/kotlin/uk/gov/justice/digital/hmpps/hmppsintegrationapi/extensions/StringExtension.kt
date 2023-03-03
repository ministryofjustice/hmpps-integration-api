package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")
