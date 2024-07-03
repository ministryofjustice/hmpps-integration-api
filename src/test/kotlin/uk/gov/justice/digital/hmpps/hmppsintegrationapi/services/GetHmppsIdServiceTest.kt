package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetHmppsIdService::class],
)
internal class GetHmppsIdServiceTest(
  @MockBean val getPersonService: GetPersonService,
  private val getHmppsIdService: GetHmppsIdService,
) : DescribeSpec(
    {
      val id = "A7777ZZ"
      val hmppsId = HmppsId(hmppsId = id)

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.execute(id)).thenReturn(
          Response(
            data = Person(firstName = "Qui-gon", lastName = "Jin", hmppsId = id, identifiers = Identifiers(nomisNumber = id)),
          ),
        )
      }

      it("Returns a hmpps id for the given id") {
        val result = getHmppsIdService.execute(id)

        result.shouldBe(Response(data = hmppsId))
      }
    },
  )
