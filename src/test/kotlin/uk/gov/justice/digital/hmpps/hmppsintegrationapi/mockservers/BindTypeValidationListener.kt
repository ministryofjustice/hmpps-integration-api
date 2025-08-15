package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response

/*
 BindTypeValidationListener.
 In Swagger Core 2.2.x, when processing OpenAPI 3.1 specifications, type field is mapped by Set<String> types
 member (instead of String type) to also support the array data type.
 This means that the foo.type in V31 schema versions is deserialized into a Schema class with populated types (foo.types)
 member (while foo.type remains null).

 This causes the discriminator validation to fail as the type is changed to null (with the following error).
 'discriminator' field 'type' must be defined as a string property

 In order to allow for no change support of 3.1 processing by existing clients in specific limited scenarios,
 users can set system property bind-type=true to have Schema.getType() return the value of the single item of Schema.types
 in OAS 3.1 specifications with a non-array Schema.type.

 */

class BindTypeValidationListener(
  openApiInteractionValidator: OpenApiInteractionValidator,
  private val overrideBindType: Boolean,
) : OpenApiValidationListener(openApiInteractionValidator) {
  override fun requestReceived(
    request: Request?,
    response: Response?,
  ) {
    if (overrideBindType) {
      withBindTypeSet { super.requestReceived(request, response) }
    } else {
      super.requestReceived(request, response)
    }
  }
}
