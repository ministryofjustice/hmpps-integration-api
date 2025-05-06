//= require govuk_tech_docs

window.addEventListener("load", (event) => {
  const ui = SwaggerUIBundle({
    url: 'https://ministryofjustice.github.io/hmpps-integration-api/openapi/api-docs.json',
    dom_id: '#swagger-ui',
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    layout: "StandaloneLayout",
    displayOperationId: false
  })

  const endpoints = [
    {
      schemaName: 'TransactionRequest',
      endpoint: 'v1/prison/{prisonId}/prisoners/{hmppsId}/transactions',
      method: 'post'
    },
    {
      schemaName: 'TransactionTransferRequest',
      endpoint: 'v1/prison/{prisonId}/prisoners/{hmppsId}/transactions/transfer',
      method: 'post'
    },
    {
      schemaName: 'CreateVisitRequest',
      endpoint: 'v1/visit',
      method: 'post'
    },
    {
      schemaName: 'UpdateVisitRequest',
      endpoint: 'v1/visit/{visitReference}',
      method: 'put'
    },
    {
      schemaName: 'CancelVisitRequest',
      endpoint: 'v1/visit/{visitReference}/cancel',
      method: 'post'
    },
    {
      schemaName: 'EducationAssessmentStatusChangeRequest',
      endpoint: 'v1/persons/{hmppsId}/education/assessments/status',
      method: 'post'
    },
  ];
  endpoints.forEach(body => {
    const endpointFormatted = body.endpoint
      .replaceAll('/', '-')
      .replaceAll('{', '')
      .replaceAll('}', '')
      .toLowerCase()
      + '-' + body.method.toLowerCase();
    const heading = document.createElement("h4");
    heading.id = `${endpointFormatted}-request-body`
    heading.classList.add('anchored-heading');
    const text = document.createTextNode('Request Body')
    heading.appendChild(text)
    const requestBody = document.querySelector(`.schema-${body.schemaName.toLowerCase()}`).cloneNode(true);
    const responses = document.querySelector(`#${endpointFormatted}-responses`);
    responses.before(heading, requestBody);
  })
});
