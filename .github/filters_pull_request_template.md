# Pull Request Template

## Description

This pull request implements the following changes:

**Controller Logic:**

- [ ] Replaced `encodedHmppsId` variable with `hmppsId` in the endpoint.
- [ ] Added `<b>Applicable filters</b>: <ul><li>prisons</li></ul>` to the endpoint description.
- [ ] Updated `@Parameter` annotation for `hmppsId`:
  - [ ] Changed description to "The HMPPS ID of the person".
  - [ ] Removed URL encoding example.
- [ ] Added `@RequestAttribute filters: ConsumerFilters?` parameter to the controller function.
- [ ] Added logic to throw a validation exception when a bad request error is received from the upstream service.
- [ ] Added `ApiResponse` for HTTP 400 (Bad Request):
  - [ ] Response code: "400".
  - [ ] Description: "Malformed hmppsId."
  - [ ] Content: `Content(schema = Schema(ref = "#/components/schemas/BadRequest"))`.

**Controller Tests:**

- [ ] Updated `hmppsId` variable to a non-encoded version (e.g., "A1234AA").
- [ ] Modified mock to use `getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)` instead of `getPersonService.execute`.
- [ ] Updated service call to include `filters`.
- [ ] Added a test case for the bad request error.

**Service Logic:**

- [ ] Added `filters` as a parameter to the service function.
- [ ] Updated the service call to use `getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)`.

**Service Tests:**

- [ ] Updated `hmppsId` variable to a non-encoded version (e.g., "A1234AA").
- [ ] Modified mock to use `getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)` instead of `getPersonService.execute`.

**Integration Tests:**

- [ ] Updated `globals.yml` with the modified endpoint.
- [ ] Added the following integration tests:
  - [ ] Returns a 400 if the `hmppsId` is invalid.
  - [ ] Returns a 404 for a person in the wrong prison (using `callApiWithCN()` and passing `limitedPrisonsCn` and the path).
  - [ ] Returns a 404 when no prisons are present in the filter (using `callApiWithCN()` and passing `noPrisonsCn` and the path).

