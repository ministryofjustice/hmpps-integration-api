package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceTerm
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

class PersonSentencesTest :
  DescribeSpec(
    {
      describe("#toSentence") {
        it("maps one-to-one attributes to integration API attributes") {
          val nomisSentence =
            NomisSentence(
              sentenceDate = LocalDate.parse("2022-02-02"),
              sentenceStatus = "A",
              sentenceTypeDescription = "ORA CJA03 Standard Determinate Sentence",
            )

          val integrationApiSentence = nomisSentence.toSentence()

          integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
          integrationApiSentence.isActive.shouldBe(true)
          integrationApiSentence.isCustodial.shouldBe(true)
          integrationApiSentence.description.shouldBe(nomisSentence.sentenceTypeDescription)
        }

        it("maps Nomis terms to Integration API terms when all possible Nomis term values are provided") {
          val nomisSentence =
            NomisSentence(
              terms =
                listOf(
                  NomisTerm(
                    years = 3,
                    months = 4,
                    weeks = 0,
                    days = 2,
                    code = "Z",
                  ),
                  NomisTerm(
                    years = 7,
                    months = 3,
                    weeks = 4,
                    days = 0,
                    code = "Y",
                  ),
                ),
            )

          val integrationApiSentence = nomisSentence.toSentence()

          integrationApiSentence.length.shouldBe(
            SentenceLength(
              duration = null,
              units = null,
              terms =
                listOf(
                  SentenceTerm(
                    years = 3,
                    months = 4,
                    weeks = null,
                    days = 2,
                    hours = null,
                    prisonTermCode = "Z",
                  ),
                  SentenceTerm(
                    years = 7,
                    months = 3,
                    weeks = 4,
                    days = null,
                    hours = null,
                    prisonTermCode = "Y",
                  ),
                ),
            ),
          )
        }

        it("maps Nomis terms to Integration API terms when some term values are provided") {
          val nomisSentence =
            NomisSentence(
              terms =
                listOf(
                  NomisTerm(
                    years = 3,
                  ),
                  NomisTerm(
                    months = 3,
                  ),
                ),
            )

          val integrationApiSentence = nomisSentence.toSentence()

          integrationApiSentence.length.shouldBe(
            SentenceLength(
              duration = null,
              units = null,
              terms =
                listOf(
                  SentenceTerm(
                    years = 3,
                    months = null,
                    weeks = null,
                    days = null,
                    hours = null,
                    prisonTermCode = null,
                  ),
                  SentenceTerm(
                    years = null,
                    months = 3,
                    weeks = null,
                    days = null,
                    hours = null,
                    prisonTermCode = null,
                  ),
                ),
            ),
          )
        }

        it("maps Nomis terms to Integration API terms when only one term is provided") {
          val nomisSentence =
            NomisSentence(
              terms =
                listOf(
                  NomisTerm(
                    years = 3,
                    months = 9,
                  ),
                ),
            )

          val integrationApiSentence = nomisSentence.toSentence()

          integrationApiSentence.length.shouldBe(
            SentenceLength(
              duration = null,
              units = null,
              terms =
                listOf(
                  SentenceTerm(
                    years = 3,
                    months = 9,
                    weeks = null,
                    days = null,
                    hours = null,
                    prisonTermCode = null,
                  ),
                ),
            ),
          )
        }

        it("sentenceStatusToBoolean correctly maps 'I' to false") {
          val nomisSentence =
            NomisSentence(
              sentenceDate = null,
              sentenceStatus = "I",
            )

          val integrationApiSentence = nomisSentence.toSentence()

          integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
          integrationApiSentence.isActive.shouldBe(false)
        }

        it("sentenceStatusToBoolean correctly maps anything other than 'A' or 'I' to null") {
          val nomisSentence =
            NomisSentence(
              sentenceDate = null,
              sentenceStatus = "X",
            )

          val integrationApiSentence = nomisSentence.toSentence()

          integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
          integrationApiSentence.isActive.shouldBe(null)
        }

        it("deals with NULL values") {
          val integrationApiSentence =
            Sentence(isCustodial = true, serviceSource = UpstreamApi.NOMIS, systemSource = SystemSource.PRISON_SYSTEMS)
          integrationApiSentence.shouldBe(
            Sentence(
              serviceSource = UpstreamApi.NOMIS,
              systemSource = SystemSource.PRISON_SYSTEMS,
              dateOfSentencing = null,
              description = null,
              isActive = null,
              isCustodial = true,
              fineAmount = null,
              length =
                SentenceLength(
                  duration = null,
                  units = null,
                  terms = emptyList(),
                ),
            ),
          )
        }
      }
    },
  )
