# 0004 - Always return a JSON object for JSON responses

2023-02-21

## Status

Accepted

## Context

It is common for a number of endpoints to return a list of objects (person images is an example of this). Either an array can be returned as the response, or it can be wrapped within a JSON root object.

_Array Response_
```json
[
  {
    "firstName" : "John",
    "lastName" : "Smith"
  },
  {
    "firstName" : "Sue",
    "lastName" : "Storm"
  }
]
```

_Wrapped in a JSON Object_
```json
{
  "persons" : 
  [
    {
      "firstName" : "John",
      "lastName" : "Smith"
    },
    {
      "firstName" : "Sue",
      "lastName" : "Storm"
    }
  ]
}
```

## Decision

We have decided that all responses will be wrapped in a JSON object.

This makes our API easier to use; Consumers do not need to build cases to support a response sometimes being an Array, and other times being a JSON object.

It also gives us the ability to add more fields as required, since consumers can ignore the new field if they don't use it. If we responded with an array and were to add a new field, the response would change from an array to a JSON object; This would break consumers.

## Consequences

1. Consideration will need to be taken when building controllers to respond with the correct type.
2. When responding with only a list of data. The data will need to be wrapped into a JSON object.