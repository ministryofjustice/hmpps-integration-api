const http = require('k6/http');
const { check } = require('k6');

const options = {
  tlsAuth: [
    {
      domains: ["dev.integration-api.hmpps.service.justice.gov.uk"],
      cert: open('/tmp/full_access.pem'),
      key: open('/tmp/full_access.key'),
    },
  ],
};

const baseUrl = "https://dev.integration-api.hmpps.service.justice.gov.uk";
const hmppsId = "A8451DY";
const alternativeHmppsId = "G6333VK";
const plpHmppsId = "A5502DZ";
const deliusCrn = "X725642";
const risksCrn = "X756352";
const prisonId = "MKI";
const visitReference = "qd-lh-gy-lx";
const clientReference = "123456";
const contactId = "1898610";
const imageId = "1988315";
const locationIdKey = "MKI-A";

const get_endpoints = [
  // same array as before...
];

const post_visit_endpoint = "/v1/visit";
const post_visit_data = JSON.stringify({
  prisonerId: "A8451DY",
  prisonId: "MKI",
  clientVisitReference: "123456",
  visitRoom: "A1",
  visitType: "SOCIAL",
  visitRestriction: "OPEN",
  startTimestamp: "2025-09-05T10:15:41",
  endTimestamp: "2025-09-05T11:15:41",
  visitNotes: [
    {
      type: "VISITOR_CONCERN",
      text: "Visitor is concerned their mother in law is coming!",
    },
  ],
  visitContact: {
    name: "John Smith",
    telephone: "0987654321",
    email: "john.smith@example.com",
  },
  createDateTime: "2025-09-05T10:15:41",
  visitorSupport: {
    description: "Visually impaired assistance",
  },
});

module.exports.options = options;

module.exports.default = function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': __ENV.FULL_ACCESS_API_KEY,
    },
  };

  const postRes = http.post(`${baseUrl}${post_visit_endpoint}`, post_visit_data, params);
  check(postRes, {
    'POST /v1/visit returns 200': (r) => r.status === 200,
  });

  for (const endpoint of get_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, params);
    check(res, {
      [`GET ${endpoint} returns 200`]: (r) => r.status === 200,
    });
  }
};
