# 0016 - Service Name Change

Decision date: 2025-09-03 (Integration API Steering Committee)

## Status

Decision status: Accepted

## Context

The original/current component name is hmpps-integration-api which is not as informative as it might be.

* The purpose of the API is to expose HMPPS data externally, but this external focus is not reflected in the name
* APIs are, by definition, an integration mechanism, so “integration” is somewhat redundant

We would therefore like to rename the component to something more appropriate.

## Options

### No change
Keep using the name “HMPPS Integration API”, with all its downsides.

### "HMPPS External Integration API”
Add the word “external” to the name.

Does not remove the redundancy between “integration” and “API”, and is somewhat unweildy.

### “HMPPS External Integration”
Add the word “external” and remove “API”.

The name now explicitly references external integration. The fact that it is an API rather than a different integration technology is an implementation detail.

### “HMPPS External API”
Add the word “external” and remove “Integration”.

The name now explicitly references external integration. The fact that the API is used for integration is implied.

## Decision

The service will be renamed to "HMPPS External API".

### Rationale

The new name reflects that the API is for external use, 
and removes redundancy from the name. 

## Consequences

* The name of the service is embedded in many documentation, development and operational contexts. 
* Changing the name without breaking existing uses will not be trivial.

