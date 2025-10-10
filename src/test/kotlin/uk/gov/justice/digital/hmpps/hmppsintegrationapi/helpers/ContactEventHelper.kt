package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.DeliusName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.DeliusOfficer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.DeliusPdu
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.DeliusRefdata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.DeliusTeam
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusContactEvents
import java.time.ZonedDateTime
import kotlin.math.ceil

object ContactEventHelper {
  val baseDateTimeString = "2025-09-08T11:34:03.569+01:00"
  val baseDateTime = ZonedDateTime.parse(baseDateTimeString)

  fun generateNDeliusContactEvent(
    id: Long,
    crn: String,
  ) = NDeliusContactEvent(
    id = id,
    type = DeliusRefdata("1", "Contact Type for $id"),
    createdAt = baseDateTime.minusDays(id),
    updatedAt = baseDateTime.minusDays(id - 1),
    crn = crn,
    contactDate = baseDateTime.minusDays(id - 2),
    outcome = DeliusRefdata("1", "Outcome"),
    location = DeliusRefdata("1", "Location"),
    officer =
      DeliusOfficer(
        "1",
        DeliusName("Officer", "Name"),
        DeliusTeam(
          "1",
          "Team Name",
          DeliusPdu(
            "1",
            "PDU",
            DeliusRefdata("1", "area"),
          ),
        ),
      ),
    description = "description",
    notes =
      "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet " +
        "adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. " +
        "Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestibulum volutpat pretium libero. Cras id dui. Aenean ut eros et nisl sagittis vestibulum. Nullam nulla eros, ultricies sit amet, nonummy id, imperdiet feugiat, pede. Sed lectus. Donec mollis hendrerit risus. Phasellus nec sem in justo pellentesque facilisis. Etiam imperdiet imperdiet orci. Nunc nec neque. Phasellus leo dolor, tempus non, auctor et, hendrerit quis, nisi. Curabitur ligula sapien, tincidunt non, euismod vitae, posuere imperdiet, leo. Maecenas malesuada. Praesent congue erat at massa. Sed cursus turpis vitae tortor. Donec posuere vulputate arcu. Phasellus accumsan cursus velit. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Sed aliquam, nisi quis porttitor congue, elit erat euismod orci, ac placerat dolor lectus quis orci. Phasellus consectetuer vestibulum elit. Aenean tellus metus, bibendum sed, posuere ac, mattis non, nunc. Vestibulum fringilla pede sit amet augue. In turpis. Pellentesque posuere. Praesent turpis. Aenean posuere, tor",
  )

  fun generateNDeliusContactEvents(
    crn: String,
    pageSize: Int,
    pageNumber: Int,
    totalRecords: Int,
  ): NDeliusContactEvents {
    val totalPages = ceil((totalRecords.toFloat() / pageSize.toFloat())).toInt()
    val idTo = if ((pageNumber * pageSize) > totalRecords) totalRecords else (pageNumber * pageSize)
    val idFrom = ((pageNumber - 1) * pageSize) + 1

    return NDeliusContactEvents(
      content =
        (idFrom..idTo).map {
          generateNDeliusContactEvent(it.toLong(), crn)
        },
      totalResults = totalRecords,
      totalPages = totalPages,
    )
  }
}
