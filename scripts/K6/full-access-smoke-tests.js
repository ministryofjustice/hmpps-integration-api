const http = require('k6/http');
const { check, fail } = require('k6');
import encoding from 'k6/encoding';
import exec from 'k6/execution';

/***********
 To run this script locally, make sure the following environment variables are set:-

 - DOMAIN = the fully qualified DNS name of the API server
 - HMPPSID = the primary hmppsId to use for testing
 - PROFILE = which testing profile to use (MAIN | LAO | NOPERMS | NOCERT)
 - FULL_ACCESS_API_KEY = API key that has full access to the API
 - FULL_ACCESS_CERT = certificate (either base 64 encoded or name of a file)
 - FULL_ACCESS_KEY = private key for certificate (either base 64 encoded or name of a file)
***********/

const api_key = __ENV.FULL_ACCESS_API_KEY;
const domain = __ENV.DOMAIN;
const profile = __ENV.PROFILE;

const cert = __ENV.FULL_ACCESS_CERT.includes(".pem") ?
  open(__ENV.FULL_ACCESS_CERT) :
  encoding.b64decode(__ENV.FULL_ACCESS_CERT, 'std', 's');

const key = __ENV.FULL_ACCESS_KEY.includes(".key") ?
  open(__ENV.FULL_ACCESS_KEY) :
  encoding.b64decode(__ENV.FULL_ACCESS_KEY, 'std', 's');


export const options = {
  tlsAuth: [
    {
      // domains: [domain],
      cert,
      key,
    },
  ],
};

const baseUrl = `https://${domain}`;

const hmppsId = "A8451DY";
const primaryHmppsId = __ENV.HMPPSID;
const hmppsIdWithLaoContext = "A4433DZ";
const visitsHmppsId = "A8452DY"
const alternativeHmppsId = "G6333VK";
const plpHmppsId = "A5502DZ";
const attendancesHmppsId = "G4328GK";
const risksCrn = "X756352";
const prisonId = "MKI";
const alternativeprisonId = "RSI";
const visitReference = "qd-lh-gy-lx";
const clientVisitReference = "SMOKE_TEST_CLIENT_REF";
const contactId = "1898610";
const imageId = "1988315";
const locationIdKey = "MKI-A";
const activityId = 1162
const scheduleId = 518
const contactEventId = 500
const today = new Date();
const year = today.getFullYear();
const month = (today.getMonth() + 1).toString().padStart(2, '0');
const day = today.getDate().toString().padStart(2, '0');
const todayFormatted = `${year}-${month}-${day}`
const startDate = "2022-01-01"
const endDate = "2022-02-01"
const attendancesStartDate = "2025-07-04"
const attendancesEndDate = "2025-07-11"

const get_endpoints = [
  `/v1/hmpps/id/by-nomis-number/${hmppsId}`,
  `/v1/hmpps/id/nomis-number/by-hmpps-id/${hmppsId}`,
  `/v1/persons/${hmppsId}/addresses`,
  `/v1/persons/${hmppsId}/contacts`,
  `/v1/persons/${hmppsId}/iep-level`,
  `/v1/persons/${alternativeHmppsId}/visit-orders`,
  `/v1/persons/${hmppsId}/visit-restrictions`,
  `/v1/persons/${hmppsId}/alerts`,
  `/v1/persons/${hmppsId}/name`,
  `/v1/persons/${hmppsId}/cell-location`,
  `/v1/persons/${hmppsId}/risks/categories`,
  `/v1/persons/${hmppsId}/sentences`,
  `/v1/persons/${hmppsId}/offences`,
  `/v1/persons/${hmppsId}/reported-adjudications`,
  `/v1/persons/${hmppsId}/number-of-children`,
  `/v1/persons/${hmppsId}/physical-characteristics`,
  `/v1/persons/${hmppsId}/care-needs`,
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
  `/v1/prison/${alternativeprisonId}/prison-regime`,
  `/v1/prison/${alternativeprisonId}/activities`,
  `/v1/prison/${alternativeprisonId}/prison-pay-bands`,
  `/v1/contacts/${contactId}`,
  `/v1/persons?first_name=john`,
  `/v1/persons/${primaryHmppsId}`,
  `/v1/persons/${hmppsIdWithLaoContext}/licences/conditions`,
  `/v1/persons/${hmppsId}/needs`,
  `/v1/persons/${hmppsIdWithLaoContext}/risks/mappadetail`,
  `/v1/persons/${hmppsIdWithLaoContext}/risks/scores`,
  `/v1/persons/${hmppsId}/plp-induction-schedule`,
  `/v1/persons/${hmppsId}/plp-induction-schedule/history`,
  `/v1/persons/${plpHmppsId}/plp-review-schedule`,
  `/v1/persons/${hmppsIdWithLaoContext}/status-information`,
  `/v1/persons/${hmppsId}/sentences/latest-key-dates-and-adjustments`,
  `/v1/persons/${hmppsIdWithLaoContext}/risks/serious-harm`,
  `/v1/persons/${hmppsIdWithLaoContext}/risks/scores`,
  `/v1/persons/${hmppsIdWithLaoContext}/risks/dynamic`,
  `/v1/hmpps/id/nomis-number/${hmppsId}`,
  `/v1/persons/${visitsHmppsId}/visit/future`,
  `/v1/visit/${visitReference}`,
  `/v1/visit/id/by-client-ref/${clientVisitReference}`,
  `/v1/prison/${prisonId}/visit/search?visitStatus=BOOKED`,
  `/v1/persons/${primaryHmppsId}/protected-characteristics`,
  `/v1/epf/person-details/${primaryHmppsId}/1`,
  `/v1/persons/${risksCrn}/risk-management-plan`,
  `/v1/persons/${alternativeHmppsId}/person-responsible-officer`,
  `/v1/persons/${alternativeHmppsId}/visitor/${contactId}/restrictions`,
  `/v1/persons/${hmppsId}/images`,
  `/v1/persons/${hmppsId}/images/${imageId}`,
  `/v1/persons/${hmppsId}/case-notes`,
  `/v1/hmpps/reference-data`,
  `/v2/config/authorisation`,
  `/v1/persons/${hmppsId}/health-and-diet`,
  `/v1/persons/${hmppsId}/languages`,
  `/v1/persons/${plpHmppsId}/education`,
  `/v1/persons/${hmppsId}/prisoner-base-location`,
  `/v1/activities/${activityId}/schedules`,
  `/v1/activities/attendance-reasons`,
  `/v1/activities/schedule/${scheduleId}`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/scheduled-instances?startDate=${startDate}&endDate=${endDate}`,
  `/v1/activities/deallocation-reasons`,
  `/v1/prison/prisoners/${attendancesHmppsId}/activities/attendances?startDate=${attendancesStartDate}&endDate=${attendancesEndDate}`,
  `/v1/activities/schedule/${scheduleId}/waiting-list-applications`,
  `/v1/activities/schedule/${scheduleId}/suitability-criteria`,
  `/v1/status`,
  `/v1/persons/${hmppsId}/education/san/plan-creation-schedule`,
  `/v1/persons/${alternativeHmppsId}/education/san/review-schedule`,
  `/v1/persons/${primaryHmppsId}/contact-events`,
  `/v1/persons/${primaryHmppsId}/contact-events/${contactEventId}`,
];

const broken_endpoints = []

const postEducationUpdateEndpoint = `/v1/persons/${hmppsId}/education/status`
const postEducationUpdateRequest = JSON.stringify({
  status: "EDUCATION_STARTED",
  detailUrl:"https://example.com/sequation-virtual-campus2-api/learnerEducation/${hmppsId}",
  requestId: "0650ba37-a977-4fbe-9000-4715aaecadba"
})

const postEducationALNUpdateEndpoint = `/v1/persons/${hmppsId}/education/aln-assessment`
const postEducationALNUpdateRequest = JSON.stringify({
  status: "ASSESSMENT_COMPLETED",
  detailUrl:"https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
  requestId: "0650ba37-a977-4fbe-9000-4715aaecadba"
})

const post_visit_endpoint = "/v1/visit";
const post_visit_data = JSON.stringify({
  prisonerId: "A4433DZ",
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

const postLocationDeactivateEndpoint = `/v1/prison/${prisonId}/location/${locationIdKey}/deactivate`;
const postLocationDeactivateData = JSON.stringify({
  deactivationReason: "DAMAGED",
  deactivationReasonDescription: "Damaged",
  proposedReactivationDate: "2025-12-01",
  externalReference: "TestEvent"
})

const postSearchAppointmentsEndpoint = `/v1/prison/${alternativeprisonId}/appointments/search`;
const postSearchAppointmentsData = JSON.stringify({
  startDate: "2025-06-16"
});

const putAttendanceEndpoint = `/v1/activities/schedule/attendance`
const putAttendanceData = JSON.stringify([{
  id: 123456,
  prisonId: "MDI",
  status: "TestEvent",
  attendanceReason: "ATTENDED",
  comment: "Prisoner has COVID-19",
  issuePayment: true,
  caseNote: "Prisoner refused to attend the scheduled activity without reasonable excuse",
  incentiveLevelWarningIssued: true,
  otherAbsenceReason: "Prisoner has another reason for missing the activity"
}])

const putDeallocationEndpoint = `/v1/activities/schedule/${scheduleId}/deallocate`
const putDeallocationData = JSON.stringify({
  prisonerNumber: hmppsId,
  reasonCode: "TestEvent",
  endDate: todayFormatted,
  caseNote: {
    type: "GEN",
    text: "Case note text"
  },
  scheduleInstanceId: 1234
})

const postAllocationEndpoint = `/v1/activities/schedule/${scheduleId}/allocate`
const postAllocationData = JSON.stringify({
  prisonerNumber: hmppsId,
  startDate: todayFormatted,
  endDate: todayFormatted,
  payBandId: 123456,
  exclusions: [
    {
      timeSlot: "AM",
      weekNumber: 1,
      monday: true,
      tuesday: true,
      wednesday: true,
      thursday: false,
      friday: false,
      saturday: false,
      sunday: false,
      customStartTime: "09:00",
      customEndTime: "11:00",
      daysOfWeek: ["MONDAY", "TUESDAY", "WEDNESDAY"]
    }
  ],
  testEvent: "TestEvent"
})

function verify_get_endpoints(params) {
  for (const endpoint of get_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, params);
    if (!check(res, {
      [`GET ${endpoint} returns 200`]: (r) => r.status === 200,
    })) {
      fail(`${endpoint} caused the test to fail`)
    }
  }
}

function verify_broken_endpoints(params) {
  for (const endpoint of broken_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, params);
    if (!check(res, {
      [`GET ${endpoint} returns error`]: (r) => r.status >= 400,
    })) {
      fail(`${endpoint} caused the test to fail`)
    }
  }
}

function verify_post_endpoints(params) {
  const postEducationStatusRes = http.post(`${baseUrl}${postEducationUpdateEndpoint}`, postEducationUpdateRequest, params);
  if (!check(postEducationStatusRes, {
    'POST /v1/persons/${hmppsId}/education/status returns 201': (r) => r.status === 201,
  })) {
    fail(`${postEducationUpdateEndpoint} caused the test to fail`)
  }

  const postEducationALNRes = http.post(`${baseUrl}${postEducationALNUpdateEndpoint}`, postEducationALNUpdateRequest, params);
  if (!check(postEducationALNRes, {
    'POST /v1/persons/${hmppsId}/education/aln-assessment returns 201': (r) => r.status === 201,
  })) {
    exec.test.fail(`${postEducationALNUpdateEndpoint} caused the test to fail`)
  }

  const postRes = http.post(`${baseUrl}${post_visit_endpoint}`, post_visit_data, params);
  if (!check(postRes, {
    'POST /v1/visit returns 200': (r) => r.status === 200,
  })) {
    exec.test.fail(`${post_visit_endpoint} caused the test to fail`)
  }

  const postLocationDeactivateRes = http.post(`${baseUrl}${postLocationDeactivateEndpoint}`, postLocationDeactivateData, params);
  if (!check(postLocationDeactivateRes, {
    [`POST ${postLocationDeactivateEndpoint} returns 200`]: (r) => r.status === 200,
  })) {
    exec.test.fail(`${postLocationDeactivateEndpoint} caused the test to fail`)
  }

  const postSearchAppointmentsRes = http.post(`${baseUrl}${postSearchAppointmentsEndpoint}`, postSearchAppointmentsData, params);
  if (!check(postSearchAppointmentsRes, {
    [`POST ${postSearchAppointmentsEndpoint} returns 200`]: (r) => r.status === 200,
  })) {
    exec.test.fail(`${postSearchAppointmentsEndpoint} caused the test to fail`)
  }

  const putAttendanceRes = http.put(`${baseUrl}${putAttendanceEndpoint}`, putAttendanceData, params);
  if (!check(putAttendanceRes, {
    [`PUT ${putAttendanceEndpoint} returns 200`]: (r) => r.status === 200,
  })) {
    exec.test.fail(`${putAttendanceEndpoint} caused the test to fail`)
  }

  const putDeallocationRes = http.put(`${baseUrl}${putDeallocationEndpoint}`, putDeallocationData, params);
  if (!check(putDeallocationRes, {
    [`PUT ${putDeallocationEndpoint} returns 200`]: (r) => r.status === 200,
  })) {
    exec.test.fail(`${putDeallocationEndpoint} caused the test to fail`)
  }

  const postAllocationRes = http.post(`${baseUrl}${postAllocationEndpoint}`, postAllocationData, params);
  if (!check(postAllocationRes, {
    [`POST ${postAllocationEndpoint} returns 200`]: (r) => r.status === 200,
  })) {
    exec.test.fail(`${postAllocationEndpoint} caused the test to fail`)
  }
}

/**
 * Make a GET request to the API and validate that the http response code indicates syccess.
 * @returns the http response object
 */
function validate_get_request(path, params) {
  const res = http.get(`${baseUrl}${path}`, params);
  check(res, {
    [`GET ${path} successful`]: (r) => r.status < 400,
  });
  return res;
}

function structured_verification_test(hmppsId, params) {
  let res = validate_get_request("/v1/status", params);
  if (res.status >= 400) {
    return
  }

  res = validate_get_request(`/v1/persons/${hmppsId}`, params);

  if (res.status >= 400) {
    return
  }

  let probationData = res.json()["data"]["probationOffenderSearch"];
  let crn = probationData["identifiers"]["deliusCrn"];

  check(crn, {
    [`CRN identified`]: () => crn != null,
  })
  check(probationData["lastName"], {
    [`Last name identified`]: (name) => name != null,
  })

  let nomisNumber = res.json()["data"]["prisonerOffenderSearch"]["identifiers"]["nomisNumber"];

  check(nomisNumber, {
    [`Prisoner number identified`]: () => nomisNumber != null,
  })

  validate_get_request(`/v1/prison/prisoners/${nomisNumber}`, params)
}

export default function ()  {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': api_key,
    },
  };

  console.log(`Using profile: ${profile} with base url: ${baseUrl}`)

  verify_post_endpoints(params);

  verify_get_endpoints(params);

  verify_broken_endpoints(params);

  if (profile === "MAIN") {
    structured_verification_test(primaryHmppsId, params);
  }
};
