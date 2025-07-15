# Validating Upstream Responses

We currently have 2 ways of mocking and validating upstream responses

- Wiremock + Atlassian OpenApi Wiremock Validator
- Prism

## Wiremock + Atlassian OpenApi Wiremock Validator

This is currently our preferred method of mocking upstream dependencies.
It involves storing fixtures (example responses) from upstream endpoints and using `WireMock` to return these when our tests make a call to the upstream.
As well as doing any assertions we want to do in our tests, we can also validate that the fixture matches the schema for the mocked endpoint in the OpenAPI spec.

### Example usage

A full example can be found in `src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/mockservers/ApiMockServerTest.kt`.

To set up a new upstream mock, you first need to set the name of the OpenAPI spec in the config.
The file is expected to be in the `src/test/resources/openapi-specs` directory.

Note that in the below example, `test.json` is used only for testing purposes and will not need to be refreshed alongside the other spec files. 

```kotlin
// src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/mockservers/ApiMockServer.kt

when (upstreamApi) {
  UpstreamApi.TEST -> ApiMockServerConfig(4005, "test.json")
}
```

A test file should look something like this

```kotlin
val mockServer = ApiMockServer.create(UpstreamApi.TEST)

beforeEach {
  mockServer.start()
  webClient = WebClientWrapper(mockServer.baseUrl())
}

afterEach {
  mockServer.stop()
  mockServer.resetValidator() // This is important so we don't have errors persisting across tests
}

it("correctly validates against spec") {
  // Mock the upstream endpoint using our wiremock server
  mockServer.stubForGet(
    "/pet/1",
    File("src/test/resources/fixtures/test/GetPetById.json").readText(),
  )

  // Call the method under test which will call the "/pet/1" upstream method

  // Do any assertions required on the output

  // Assert that the OpenAPI validation on the mock server passed, else the test will fail
  mockServer.assertValidationPassed()
}
```
