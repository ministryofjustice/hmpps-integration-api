package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

class SupervisionsTest : DescribeSpec(
  {
    describe("#toOffences") {
      describe("When there are additional offences") {
        it("maps one-to-one attributes to integration API attributes") {
          val supervisions =
            NDeliusSupervisions(
              mappaDetail = NDeliusMappaDetail(),
              supervisions =
                listOf(
                  NDeliusSupervision(
                    custodial = true,
                    mainOffence = NDeliusMainOffence(description = "foobar", code = "05800", date = "2000-01-02"),
                    additionalOffences =
                      listOf(
                        NDeliusAdditionalOffence(description = "additionalFoo", code = "12345", date = "2001-01-01"),
                      ),
                    courtAppearances = listOf(NDeliusCourtAppearance(date = "2009-07-07T00:00:00+01:00")),
                  ),
                  NDeliusSupervision(
                    custodial = true,
                    mainOffence = NDeliusMainOffence(description = "barbaz", code = "05800", date = "2003-03-03"),
                    additionalOffences =
                      listOf(
                        NDeliusAdditionalOffence(description = "additionalFoo2", code = "98765", date = "2001-02-02"),
                      ),
                    courtAppearances = listOf(NDeliusCourtAppearance(date = "2010-07-07T00:00:00+01:00")),
                  ),
                ),
            )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "foobar",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2000-01-02"),
              ),
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "additionalFoo",
                hoCode = "12345",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2001-01-01"),
              ),
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "barbaz",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2010-07-07")),
                startDate = LocalDate.parse("2003-03-03"),
              ),
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "additionalFoo2",
                hoCode = "98765",
                courtDates = listOf(LocalDate.parse("2010-07-07")),
                startDate = LocalDate.parse("2001-02-02"),
              ),
            ),
          )
        }
        it("does not local date parse additional offence date if no date is provided") {
          val supervisions =
            NDeliusSupervisions(
              mappaDetail = NDeliusMappaDetail(),
              supervisions =
                listOf(
                  NDeliusSupervision(
                    custodial = true,
                    mainOffence = NDeliusMainOffence(description = "foobar", code = "05800", date = "2000-01-02"),
                    additionalOffences = listOf(NDeliusAdditionalOffence(description = "additionalFoo", code = "12345")),
                    courtAppearances = listOf(NDeliusCourtAppearance(date = "2009-07-07T00:00:00+01:00")),
                  ),
                ),
            )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "foobar",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2000-01-02"),
              ),
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "additionalFoo",
                hoCode = "12345",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = null,
              ),
            ),
          )
        }

        it("does local date parse additional offence date if a date is provided") {
          val supervisions =
            NDeliusSupervisions(
              mappaDetail = NDeliusMappaDetail(),
              supervisions =
                listOf(
                  NDeliusSupervision(
                    custodial = true,
                    mainOffence = NDeliusMainOffence(description = "foobar", code = "05800", date = "2000-01-02"),
                    additionalOffences =
                      listOf(
                        NDeliusAdditionalOffence(description = "additionalFoo", code = "12345", date = "2001-01-01"),
                      ),
                    courtAppearances = listOf(NDeliusCourtAppearance(date = "2009-07-07T00:00:00+01:00")),
                  ),
                ),
            )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "foobar",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2000-01-02"),
              ),
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "additionalFoo",
                hoCode = "12345",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2001-01-01"),
              ),
            ),
          )
        }
      }

      describe("When there are no additional offences") {
        it("maps one-to-one attributes to integration API attributes") {
          val supervisions =
            NDeliusSupervisions(
              mappaDetail = NDeliusMappaDetail(),
              supervisions =
                listOf(
                  NDeliusSupervision(
                    custodial = true,
                    mainOffence = NDeliusMainOffence(description = "foobar", code = "05800", date = "2019-09-09"),
                    additionalOffences = emptyList(),
                    courtAppearances = listOf(NDeliusCourtAppearance(date = "2009-07-07T00:00:00+01:00")),
                  ),
                  NDeliusSupervision(
                    custodial = true,
                    mainOffence = NDeliusMainOffence(description = "barbaz", code = "05800", date = "2020-02-03"),
                    additionalOffences = emptyList(),
                    courtAppearances = listOf(NDeliusCourtAppearance(date = "2010-07-07T00:00:00+01:00")),
                  ),
                ),
            )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "foobar",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2019-09-09"),
              ),
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                description = "barbaz",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2010-07-07")),
                startDate = LocalDate.parse("2020-02-03"),
              ),
            ),
          )
        }
      }
    }
    describe("#toSentence") {
      it("maps one-to-one attributes to integration API sentence attributes") {
        val supervisions =
          NDeliusSupervisions(
            communityManager = NDeliusCommunityManager(),
            mappaDetail = NDeliusMappaDetail(),
            listOf(
              NDeliusSupervision(
                active = true,
                custodial = true,
                sentence =
                  NDeliusSentence(
                    date = "2009-07-07",
                    description = "CJA - Community Order",
                    length = 10,
                    lengthUnits = "years",
                  ),
              ),
              NDeliusSupervision(
                active = false,
                custodial = true,
                sentence =
                  NDeliusSentence(
                    date = "2010-07-07",
                    description = "CJA - Suspended Sentence Order",
                    length = 4,
                    lengthUnits = "weeks",
                  ),
              ),
            ),
          )

        val integrationApiSentences = supervisions.supervisions.map { it.toSentence() }

        integrationApiSentences.shouldBe(
          listOf(
            generateTestSentence(
              serviceSource = UpstreamApi.NDELIUS,
              systemSource = SystemSource.PROBATION_SYSTEMS,
              dateOfSentencing = LocalDate.parse("2009-07-07"),
              description = "CJA - Community Order",
              length =
                SentenceLength(
                  duration = 10,
                  units = "years",
                  terms = emptyList(),
                ),
            ),
            generateTestSentence(
              serviceSource = UpstreamApi.NDELIUS,
              systemSource = SystemSource.PROBATION_SYSTEMS,
              dateOfSentencing = LocalDate.parse("2010-07-07"),
              isActive = false,
              description = "CJA - Suspended Sentence Order",
              length =
                SentenceLength(
                  duration = 4,
                  units = "weeks",
                  terms = emptyList(),
                ),
            ),
          ),
        )
      }

      it("can be constructed with NULL values") {
        val supervisions =
          NDeliusSupervisions(
            communityManager = NDeliusCommunityManager(),
            mappaDetail = NDeliusMappaDetail(),
            listOf(
              NDeliusSupervision(custodial = true),
            ),
          )

        supervisions.supervisions.first().toSentence().shouldBe(
          Sentence(
            serviceSource = UpstreamApi.NDELIUS,
            systemSource = SystemSource.PROBATION_SYSTEMS,
            isActive = null,
            isCustodial = true,
            description = null,
            dateOfSentencing = null,
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
