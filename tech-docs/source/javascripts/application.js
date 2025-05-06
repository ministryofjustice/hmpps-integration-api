//= require govuk_tech_docs

window.addEventListener("load", (event) => {
  SwaggerUIBundle({
    url: 'https://ministryofjustice.github.io/hmpps-integration-api/openapi/api-docs.json',
    dom_id: '#swagger-ui',
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    layout: "StandaloneLayout",
    displayOperationId: false
  })
});
