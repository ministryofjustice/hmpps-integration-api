package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class ImageMetadata(
  @Schema(description = "The Image ID, in reference to a unique identifier.", example = "2461788")
  val id: Long,
  @Schema(description = "A flag to indicate whether an image is in active use. It is no guarantee that the latest uploaded image will be the active one.", example = "true")
  val active: Boolean,
  @Schema(description = "The Date and Time of when the image was captured.", example = "2015-05-27T17:13:59")
  val captureDateTime: LocalDateTime,
  @Schema(
    description = """
      View is the subject focus; describing the inner focus or subject of the image, normally referring to a marking, tattoo or deeper level focus of the orientation. In practise this is the interior foci of what is captured in the 'orientation' field.
      Possible values are:
      `FACE` - Facing,
      `TAT` - Tattoo,
      `OTH` - Other,
      `SCAR` - Scar,
      `MARK` - Mark,
      `OIC` - Offence in Custody
    """,
    allowableValues = ["FACE", "TAT", "OTH", "SCAR", "MARK", "OIC"],
    example = "FACE",
  )
  val view: String,
  @Schema(
    description = """
      Orientation is the scope focus; describing the scope or outer focus of the image, normally referring to the highest level object of interest within the bounds of the photo itself. This is normally a body part or an angle of photography, such as a photo of someone’s facial view (`FRONT`) or arm (`ARM`).
      Possible values are:
      `ANKLE` - Ankle,
      `ARM` - Arm,
      `DAMAGE` - Damage,
      `EAR` - Ear,
      `ELBOW` - Elbow,
      `FACE` - Face,
      `FIGHT` - Fight,
      `FINGER` - Finger,
      `FOOT` - Foot,
      `FRONT` - Front Facial View,
      `HAND` - Hand,
      `HEAD` - Head,
      `INCIDENT` - Incident,
      `INJURY` - Injury,
      `KNEE` - Knee,
      `LEG` - Leg,
      `LIP` - Lip,
      `NECK` - Neck,
      `NOSE` - Nose,
      `SHOULDER` - Shoulder,
      `THIGH` - Thigh,
      `TOE` - Toe,
      `TORSO` - Torso
    """,
    allowableValues = ["ANKLE", "ARM", "DAMAGE", "EAR", "ELBOW", "FACE", "FIGHT", "FINGER", "FOOT", "FRONT", "HAND", "HEAD", "INCIDENT", "INJURY", "KNEE", "LEG", "LIP", "NECK", "NOSE", "SHOULDER", "THIGH", "TOE", "TORSO"],
    example = "FRONT",
  )
  val orientation: String,
  @Schema(
    description = """
      Type is the contextual focus; describing the context or scenario the image was taken within. It could be for a particular purpose or capturing the results of a particular type of incident.
      Possible values are:
      `OFF_BKG` - Offender Booking,
      `OFF_IDM` - Offender Identification Marks,
      `OIC` - Offence In Custody,
      `PPTY` - Property Image
    """,
    allowableValues = ["OFF_BKG", "OFF_IDM", "OIC", "PPTY"],
    example = "OFF_BKG",
  )
  val type: String,
)
