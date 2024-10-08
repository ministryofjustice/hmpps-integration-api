package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

fun String.decodeUrlCharacters(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)
