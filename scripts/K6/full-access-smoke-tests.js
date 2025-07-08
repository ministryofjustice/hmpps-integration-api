const http = require('k6/http');
const { check } = require('k6');
import encoding from 'k6/encoding';
import exec from 'k6/execution';

const cert = encoding.b64decode(__ENV.FULL_ACCESS_CERT, 'std', 's');
const key = encoding.b64decode(__ENV.FULL_ACCESS_KEY, 'std', 's');

export const options = {
  tlsAuth: [
    {
      domains: ["dev.integration-api.hmpps.service.justice.gov.uk"],
      cert,
      key,
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
const alternativeprisonId = "RSI";
const visitReference = "qd-lh-gy-lx";
const clientVisitReference = "SMOKE_TEST_CLIENT_REF";
const contactId = "1898610";
const imageId = "1988315";
const locationIdKey = "MKI-A";
const activityId = 1162
const scheduleId = 1
const today = new Date();
const year = today.getFullYear();
const month = (today.getMonth() + 1).toString().padStart(2, '0');
const day = today.getDate().toString().padStart(2, '0');
const todayFormatted = `${year}-${month}-${day}`

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
  `/v1/hmpps/id/nomis-number/${hmppsId}`,
  `/v1/persons/${hmppsId}/visit/future`,
  `/v1/visit/${visitReference}`,
  `/v1/visit/id/by-client-ref/${clientVisitReference}`,
  `/v1/prison/${prisonId}/visit/search?visitStatus=BOOKED`,
  `/v1/persons/${deliusCrn}/protected-characteristics`,
  `/v1/epf/person-details/${deliusCrn}/1`,
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
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10`,
  `/v1/activities/deallocation-reasons`
];

const broken_endpoints = []

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

export default function ()  {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': __ENV.FULL_ACCESS_API_KEY,
    },
  };

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

  for (const endpoint of get_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, params);
    if (!check(res, {
      [`GET ${endpoint} returns 200`]: (r) => r.status === 200,
    })) {
      exec.test.fail(`${endpoint} caused the test to fail`)
    }
  }

  for (const endpoint of broken_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, params);
    if (!check(res, {
      [`GET ${endpoint} returns error`]: (r) => r.status >= 400,
    })) {
      exec.test.fail(`${endpoint} caused the test to fail`)
    }
  }
};
