package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

fun String.decodeUrl(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)
