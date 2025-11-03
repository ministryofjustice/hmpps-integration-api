import http from 'k6/http';
import { group, check, fail } from 'k6';
import exec from 'k6/execution';
import { read_certificate } from "./support.js"

/***********
 To run this script locally, make sure the following environment variables are set:-

 - DOMAIN = the fully qualified DNS name of the API server
 - HMPPSID = the primary hmppsId to use for testing
 - PROFILE = which testing profile to use (MAIN | LAO | NOPERMS | NOCERT)
 - FULL_ACCESS_API_KEY = API key that has full access to the API
 - FULL_ACCESS_CERT = certificate (either base 64 encoded or name of a file)
 - FULL_ACCESS_KEY = private key for certificate (either base 64 encoded or name of a file)


 The script is run using the k6 utility from https://k6.io/

 Note that because TLS certificates are defined globally for k6 scripts, we need to launch
 k6 separately for each certificate we want to test with.
***********/

const domain = __ENV.DOMAIN;
const profile = __ENV.PROFILE;

const [cert, key, api_key] = read_certificate(profile);

export const options = (cert === "") ? {} : {
  tlsAuth: [
    {
      cert,
      key,
    },
  ],
};

const httpParams = {
  headers: {
    'Content-Type': 'application/json',
    'x-api-key': api_key,
  },
};

const baseUrl = `https://${domain}`;

const hmppsId = "A8451DY";
const primaryHmppsId = __ENV.HMPPSID;
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
const locationIdKey = "MKI-A";
const activityId = 1162
const scheduleId = 518
const today = new Date();
const year = today.getFullYear();
const month = (today.getMonth() + 1).toString().padStart(2, '0');
const day = today.getDate().toString().padStart(2, '0');
const todayFormatted = `${year}-${month}-${day}`
const startDate = "2022-01-01"
const endDate = "2022-02-01"
const attendancesStartDate = "2025-07-04"
const attendancesEndDate = "2025-07-11"

// These endpoints don't work with the primary HMPPS ID
const get_endpoints = [
  `/v1/persons/${alternativeHmppsId}/visit-orders`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/transactions/canteen_test`,
  `/v1/prison/${prisonId}/location/${locationIdKey}`,
  `/v1/prison/${alternativeprisonId}/prison-regime`,
  `/v1/persons/${hmppsId}/plp-induction-schedule`,
  `/v1/persons/${hmppsId}/plp-induction-schedule/history`,
  `/v1/persons/${plpHmppsId}/plp-review-schedule`,
  `/v1/persons/${visitsHmppsId}/visit/future`,
  `/v1/visit/${visitReference}`,
  `/v1/visit/id/by-client-ref/${clientVisitReference}`,
  `/v1/persons/${risksCrn}/risk-management-plan`,
  `/v1/persons/${alternativeHmppsId}/visitor/${contactId}/restrictions`,
  `/v1/persons/${plpHmppsId}/education`,
  `/v1/activities/${activityId}/schedules`,
  `/v1/activities/schedule/${scheduleId}`,
  `/v1/prison/${prisonId}/prisoners/${hmppsId}/scheduled-instances?startDate=${startDate}&endDate=${endDate}`,
  `/v1/prison/prisoners/${attendancesHmppsId}/activities/attendances?startDate=${attendancesStartDate}&endDate=${attendancesEndDate}`,
  `/v1/activities/schedule/${scheduleId}/waiting-list-applications`,
  `/v1/activities/schedule/${scheduleId}/suitability-criteria`,
];

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


function verify_get_endpoints() {
  for (const endpoint of get_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, httpParams);
    if (!check(res, {
      [`GET ${endpoint} returns 200`]: (r) => r.status === 200,
    })) {
      exec.test.fail(`${endpoint} caused the test to fail, status = ${res.status}`)
    }
  }
}

function verify_post_endpoints() {
  let params = httpParams;
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
 * Make a GET request to the API and validate that the http response code indicates success.
 * @returns the http response object
 */
function validate_get_request(path) {
  const res = http.get(`${baseUrl}${path}`, httpParams);
  if (!check(res, {
    [`GET ${path} successful`]: (r) => r.status < 400,
  })) {
    exec.test.fail(`GET ${path} failed, http status = ${res.status}`);
  }
  return res;
}


function confirm_access_denied(path) {
  const res = http.get(`${baseUrl}${path}`, httpParams);
  if (!check(res, {
    [`GET ${path} ACCESS DENIED`]: (r) => ((r.status === 0) || (r.status === 401) || (r.status === 403)),
  })) {
    exec.test.fail(`${path} was not denied`);
  }
}

/**
 * Validates the generic endpoints (status, config, refdata).
 *
 * @returns true if the status endpoint worked
 */
function verify_system_endpoints() {
  let response = validate_get_request("/v1/status");
  if (!check(response, {
    ["Status endpoint reports OK"]: (res) => res.json()["data"]["status"] === "ok",
  })) {
    return false
  }

  validate_get_request("/v2/config/authorisation");

  return true
}

function verify_contact_events(hmppsId){
  let res = validate_get_request(`/v1/persons/${hmppsId}/contact-events?page=1&size=10`);
  if (res.status !== 200) {
    return
  }
  check(res, {
    [`Contact events found`]: () => res?.json()["data"].length > 0,
  })
  const id = res.json()["data"][0]["contactEventIdentifier"]
  check(id, {
    [`Contact event ID found`]: () => id != null,
  })
  validate_get_request(`/v1/persons/${hmppsId}/contact-events/${id}?mappaCategories=4`);
}

function validate_person_search(lastName, dob) {
  let res = validate_get_request(`/v1/persons?last_name=${lastName}&date_of_birth=${dob}`);
  if (res.status !== 200) {
    return
  }
  let data = res.json()["data"];
  check(data, {
    [`Person search found at least 1 match`]: (data) => data.length >= 1,
  })
  check(data, {
    [`Search result last name matches`]: (data) => data[0]["lastName"].toLowerCase() === lastName.toLowerCase(),
  })
}

function verify_get_person(hmppsId) {
  let res = validate_get_request(`/v1/persons/${hmppsId}`);
  if (res.status >= 400) {
    return null
  }

  let probationData = res.json()["data"]["probationOffenderSearch"];
  let crn = probationData["identifiers"]["deliusCrn"];
  let lastName = probationData["lastName"];
  let dob = probationData["dateOfBirth"];

  check(crn, {
    [`CRN identified`]: () => crn != null,
  })
  check(lastName, {
    [`Last name identified`]: (name) => name != null,
  })

  validate_person_search(lastName, dob, crn);

  let nomisNumber = res.json()["data"]["prisonerOffenderSearch"]["identifiers"]["nomisNumber"];

  check(nomisNumber, {
    [`Prisoner number identified`]: () => nomisNumber != null,
  })

  return nomisNumber
}


/**
 * Minimal verification for environments with sensitive data.
 */
function minimal_prod_verification() {
  verify_system_endpoints();
}

/**
 * Verify that an endpoint cannot be accessed.
 */
function denied_endpoint_verification() {
  confirm_access_denied(`/v1/persons?first_name=john`);
}

/**
 * Simple verification that a set of endpoints return the expected http response code.
 *
 * This includes GET and POST endpoints, and also any endpoints that do not work for some reason.
 */
function simple_endpoint_tests() {
  verify_post_endpoints();
  verify_get_endpoints();
}

/**
 * Test using a consumer that has access to some endpoints but not others.
 */
function partial_access_tests() {
  validate_get_request(`/v1/persons/${primaryHmppsId}/name`);
  denied_endpoint_verification();
}

function verify_reference_data() {
  validate_get_request(`/v1/hmpps/reference-data`);
  validate_get_request(`/v1/activities/attendance-reasons`);
  validate_get_request(`/v1/activities/deallocation-reasons`);
}

function verify_epf(hmppsId) {
  validate_get_request(`/v1/epf/person-details/${hmppsId}/1`);

  let res = validate_get_request(`/v1/persons/${hmppsId}/access-limitations`);
  if (res.status === 200) {
    check(res.json(), {
      [`LAO exclusion data provided`]: (json) => json["data"]["excludedFrom"] != null,
    })
  }
}

function verify_prison_endpoints(prisonId) {
  validate_get_request(`/v1/prison/${prisonId}/capacity`);
  validate_get_request(`/v1/prison/${prisonId}/residential-details`);
  validate_get_request(`/v1/prison/${prisonId}/residential-hierarchy`);
  validate_get_request(`/v1/prison/${prisonId}/visit/search?visitStatus=BOOKED`);
  validate_get_request(`/v1/prison/${prisonId}/activities`);
  validate_get_request(`/v1/prison/${prisonId}/prison-pay-bands`);
}

function verify_prisoner_endpoints(prisonId, nomisNumber) {
  let prisonerPrefix = `/v1/prison/${prisonId}/prisoners/${nomisNumber}`;
  validate_get_request(`${prisonerPrefix}/balances`);
  validate_get_request(`${prisonerPrefix}/accounts/spends/transactions`);
  // validate_get_request(`${prisonerPrefix}/transactions/canteen_test`);
  validate_get_request(`${prisonerPrefix}/non-associations`);
}

function verify_prisons_endpoints(nomisNumber) {
  group('prisons', () => {
    let res = validate_get_request(`/v1/prison/prisoners/${nomisNumber}`)
    if (res.status !== 200) {
      console.log(`Skipping prison checks for ${nomisNumber} - failed`);
      return
    }
    let prisoner = res.json()["data"]
    if (prisoner == null) {
      console.log(`Skipping prison checks for ${nomisNumber} - no data`);
      return
    }

    validate_get_request(`/v1/persons/${nomisNumber}`); // Get person by NOMIS ID, we already got by CRN
    validate_get_request(`/v1/persons/${nomisNumber}/cell-location`);
    validate_get_request(`/v1/persons/${nomisNumber}/visit-restrictions`);
    // Temporary removal of prisoner base location test - needs reinstated
    // validate_get_request(`/v1/persons/${nomisNumber}/prisoner-base-location`);

    let surname = prisoner["lastName"];
    let firstName = prisoner["firstName"];
    let dateOfBirth = prisoner["dateOfBirth"];

    validate_get_request(`/v1/prison/prisoners?first_name=${firstName}&surname=${surname}&date_of_birth=${dateOfBirth}`);

    let prisonId = prisoner["prisonId"];
    check(prisonId, {
      [`Prison ID found`]: () => prisonId != null,
    })

    verify_prison_endpoints(prisonId);
    verify_prisoner_endpoints(prisonId, nomisNumber);
  })
}

function verify_pnd_alerts(hmppsId) {
  validate_get_request(`/v1/pnd/persons/${hmppsId}/alerts`,)
}

function verify_risk_endpoints(hmppsId) {
  group('risk', () => {
    validate_get_request(`/v1/persons/${hmppsId}/licences/conditions`);
    validate_get_request(`/v1/persons/${hmppsId}/status-information`);
    validate_get_request(`/v1/persons/${hmppsId}/risks/mappadetail`);
    validate_get_request(`/v1/persons/${hmppsId}/risks/serious-harm`);
    validate_get_request(`/v1/persons/${hmppsId}/risks/scores`);
    validate_get_request(`/v1/persons/${hmppsId}/risks/dynamic`);
    validate_get_request(`/v1/persons/${hmppsId}/risks/categories`);
  })
}

function verify_get_basic_details(hmppsId) {
  group('basic details', () => {
    validate_get_request(`/v1/persons/${hmppsId}/name`);
    validate_get_request(`/v1/persons/${hmppsId}/addresses`);
    validate_get_request(`/v1/persons/${hmppsId}/alerts`);
    validate_get_request(`/v1/persons/${hmppsId}/offences`);
    validate_get_request(`/v1/persons/${hmppsId}/sentences`);
    validate_get_request(`/v1/persons/${hmppsId}/reported-adjudications`);
    validate_get_request(`/v1/persons/${hmppsId}/number-of-children`);
    validate_get_request(`/v1/persons/${hmppsId}/physical-characteristics`);
    validate_get_request(`/v1/persons/${hmppsId}/protected-characteristics`);
    validate_get_request(`/v1/persons/${hmppsId}/care-needs`);
    validate_get_request(`/v1/persons/${hmppsId}/needs`);
    validate_get_request(`/v1/persons/${hmppsId}/person-responsible-officer`);
    validate_get_request(`/v1/persons/${hmppsId}/case-notes`);
    validate_get_request(`/v1/persons/${hmppsId}/health-and-diet`);
    validate_get_request(`/v1/persons/${hmppsId}/languages`);
    validate_get_request(`/v1/persons/${hmppsId}/iep-level`);
    validate_get_request(`/v1/persons/${hmppsId}/sentences/latest-key-dates-and-adjustments`);
  })
}

function verify_get_images(hmppsId) {
  let res = validate_get_request(`/v1/persons/${hmppsId}/images`);
  if (res.status < 400) {
    let images = res.json()["data"];
    if (!check(images, {
      [`At least one image returned`]: (json) => images.length >= 1,
    })) {
      return
    }
    let imageId = images[0]["id"];
    validate_get_request(`/v1/persons/${hmppsId}/images/${imageId}`);
  }
}

function verify_education_san(hmppsId) {
  validate_get_request(`/v1/persons/${hmppsId}/education/san/plan-creation-schedule`);
  validate_get_request(`/v1/persons/${hmppsId}/education/san/review-schedule`);
}

function verify_id_conversion(crn, nomisNumber) {
  validate_get_request(`/v1/hmpps/id/by-nomis-number/${nomisNumber}`);
  validate_get_request(`/v1/hmpps/id/nomis-number/by-hmpps-id/${crn}`);
  validate_get_request(`/v1/hmpps/id/nomis-number/${crn}`);
}

function verify_prisoner_contacts(hmppsId) {
  let res = validate_get_request(`/v1/persons/${hmppsId}/contacts`);
  if (res.status !== 200) {
    return
  }
  let contacts = res.json()["data"];
  if (!check(contacts, {
    [`At least one prisoner contact returned`]: () => contacts.length >= 1,
  })) {
    return
  }

  let contactId = contacts[0]["contact"]["contactId"];
  validate_get_request(`/v1/contacts/${contactId}`);
}

/**
 * The primary smoke test for the External API.
 **
 * @param hmppsId
 */
function structured_verification_test(hmppsId) {
  let res = verify_system_endpoints();
  if (res.status >= 400) {
    exec.test.abort(`Status endpoint failed`)
    return
  }

  verify_reference_data();

  let nomisNumber = verify_get_person(hmppsId);

  verify_get_basic_details(hmppsId);

  verify_get_images(hmppsId);

  verify_prisoner_contacts(hmppsId);

  if (nomisNumber != null) {
    verify_prisons_endpoints(nomisNumber);
    verify_id_conversion(hmppsId, nomisNumber);
  } else {
    console.log(`No nomis number found, skipping prisons tests`)
  }

  verify_contact_events(hmppsId);

  verify_risk_endpoints(hmppsId);

  verify_epf(hmppsId);

  verify_pnd_alerts(hmppsId);

  verify_education_san(hmppsId);
}
/************************************************************************/

export default function ()  {
  console.log(`Using profile: ${profile} with base url: ${baseUrl}`);

  switch (profile) {
    case "MAIN":
      structured_verification_test(primaryHmppsId);
      simple_endpoint_tests();
      break
    case "PROD":
      minimal_prod_verification();
      break
    case "LIMITED":
      partial_access_tests();
      break
    case "NOPERMS":
    case "NOCERT":
      denied_endpoint_verification();
      break
    default:
      console.log(`Unsupported profile: ${profile}`);
      break
  }
};

/************************************************************************/
