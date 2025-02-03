//= require govuk_tech_docs

window.addEventListener("load", (event) => {
  const endpoints = [{
    schemaName: 'transactionrequest',
    endpoint: 'v1/prison/{prisonId}/prisoners/hmppsid/transactions',
    method: 'post'
  }, {
    schemaName: 'transactiontransferrequest',
    endpoint: 'v1/prison/{prisonId}/prisoners/{hmppsId}/transactions/transfer',
    method: 'post'
  }];
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
    const requestBody = document.querySelector(`.schema-${body.schemaName}`).cloneNode(true);
    const responses = document.querySelector(`#${endpointFormatted}-responses`);
    responses.before(heading, requestBody);
  })
});
