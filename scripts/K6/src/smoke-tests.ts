/// <reference types="k6" />

import http from 'k6/http';
import {check} from "k6";

const certPath = __ENV.FULL_ACCESS_CERT;
const keyPath = __ENV.FULL_ACCESS_KEY;

export const options = {
  tlsAuth: [
    {
      domains: ["dev.integration-api.hmpps.service.justice.gov.uk"],
      cert: open(certPath),
      key: open(keyPath),
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
  `/v1/hmpps/id/by-nomis-number/${hmppsId}`,
  `/v1/hmpps/id/nomis-number/by-hmpps-id/${hmppsId}`,
  `/v1/persons/${hmppsId}/addresses`,
  `/v1/persons/${hmppsId}/contacts`,
  `/v1/persons/${hmppsId}/iep-level`,
  `/v1/persons/${alternativeHmppsId}/visit-orders`,
  `/v1/persons/${hmppsId}/visit-restrictions`,
  `/v1/persons/${hmppsId}/alerts`,
  `/v1/persons/${hmppsId}/alerts/pnd`,
  `/v1/persons/${hmppsId}/name`,
  `/v1/persons/${hmppsId}/cell-location`,
  `/v1/persons/${hmppsId}/risks/categories`,
  `/v1/persons/${hmppsId}/sentences`,
  `/v1/persons/${hmppsId}/offences`,
  `/v1/persons/${hmppsId}/reported-adjudications`,
  `/v1/persons/${hmppsId}/number-of-children`,
  `/v1/persons/${hmppsId}/physical-characteristics`,
  `/v1/pnd/persons/${hmppsId}/alerts`,
  `/v1/prison/prisoners?first_name=john`,
  `/v1/prison/prisoners/${hmppsId}`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/balances`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/accounts/spends/balances`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/accounts/spends/transactions`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/transactions/canteen_test`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/non-associations`,
  `/v1/prison/${prisonId}/residential-hierarchy`,
  `/v1/prison/${prisonId}/location/${locationIdKey}`,
  `/v1/prison/${prisonId}/residential-details`,
  `/v1/prison/${prisonId}/capacity`,
  `/v1/contacts/123456`,
  `/v1/persons?first_name=john`,
  `/v1/persons/${deliusCrn}`,
  `/v1/persons/${hmppsId}/licences/conditions`,
  `/v1/persons/${hmppsId}/needs`,
  `/v1/persons/${hmppsId}/risks/mappadetail`,
  `/v1/persons/${hmppsId}/risks/scores`,
  `/v1/persons/${hmppsId}/plp-induction-schedule`,
  `/v1/persons/${hmppsId}/plp-induction-schedule/history`,
  `/v1/persons/${plpHmppsId}/plp-review-schedule`,
  `/v1/persons/${hmppsId}/status-information`,
  `/v1/persons/${hmppsId}/sentences/latest-key-dates-and-adjustments`,
  `/v1/persons/${hmppsId}/risks/serious-harm`,
  `/v1/persons/${hmppsId}/risks/scores`,
  `/v1/persons/${hmppsId}/risks/dynamic`,
  //`/v1/hmpps/reference-data`, Currently 401 code from delius.
  `/v1/hmpps/id/nomis-number/${hmppsId}`,
  `/v1/persons/${hmppsId}/visit/future`,
  `/v1/visit/${visitReference}`,
  `/v1/visit/id/by-client-ref/${clientReference}`,
  `/v1/prison/${prisonId}/visit/search?visitStatus=BOOKED`,
  `/v1/persons/${deliusCrn}/protected-characteristics`,
  `/v1/epf/person-details/${deliusCrn}/1`,
  `/v1/persons/${risksCrn}/risk-management-plan`,
  `/v1/persons/${alternativeHmppsId}/person-responsible-officer`,
  `/v1/persons/${alternativeHmppsId}/visitor/${contactId}/restrictions`,
  `/v1/persons/${hmppsId}/images`,
  `/v1/persons/${hmppsId}/images/${imageId}`,
  `/v1/persons/${hmppsId}/case-notes`,
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
  // visitors: [
  //   {
  //     nomisPersonId: 654321,
  //     visitContact: true,
  //   },
  // ],
});

export default function () {
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
}


