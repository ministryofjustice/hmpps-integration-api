data class ReferenceData(
  val prisonReferenceData: Map<String, List<ReferenceDataItem>>?,
  val probationReferenceData: Map<String, List<ReferenceDataItem>>?,
)

data class ReferenceDataItem(
  val code: String,
  val description: String,
)
