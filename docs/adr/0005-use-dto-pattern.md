# 0005 - Data Transfer Object Pattern

2023-03-09

## Status

Accepted

## Context

The system must be capable of retrieving data from numerous data sources; To support this, it will need to be able
to transform the incoming response into a model. For this reason, we will have a model which represents the source system
for each response. Part of our project is to standardise and consolidate data; To achieve this, the system will need to
be able to transform the incoming data model into a standardised format.


## Decision

We have decided to use a the data transfer object pattern (DTO Pattern) to achieve this. The main benefit for us is that
each DTO data model (the upstream system model) will be capable of converting itself to our standardised & consolidated model
(domain model). This is executed by implementing a function on each of the DTO models which can convert to the appropriate
domain model.

> "Although the main reason for using a Data Transfer Object is to batch up what would be multiple remote calls into a single call,
> it's worth mentioning that another advantage is to encapsulate the serialization mechanism for transferring data over the wire.
> By encapsulating the serialization like this, the DTOs keep this logic out of the rest of the code and also provide a clear point
> to change serialization should you wish."
> _Source_ [Martin Fowler's Webpage](https://martinfowler.com/eaaCatalog/dataTransferObject.html)

### Example
Consider the following example.

_Upstream system 'a' model_
```kotlin
 data class Human(val name: String){
     toDomain() = Person(name, null)
 }
```

_Upstream system 'b' model_
```kotlin
 data class Person(val name: String, val surname: String){
     toDomain() = Person(name, surname)
 }
```

_Our 'domain' model_
```kotlin
 data class Person(val firstName: String, val lastName: String?)
```

This allows us to control how we map from upstream systems to our own objects. If we get a failure during the mapping process
due to an upstream schema changing; It'll be very clear as to where the problem lies.

## Consequences

- A structure which represents the upstream data model will need to be created for each result set we get.
- Consideration will need to be made when mapping their data types to ours.