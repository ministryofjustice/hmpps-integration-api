# 0002 - Use PlantUML to create diagrams
2023-01-24

## Status

Accepted

## Context

We need diagrams to visualise our architecture. We had decided that diagrams as code were the way forward due to
the following reasons:
- Can be stored in source control
- Multiple output formats
- Consistency, everyone will get the same output

We had not yet decided the tool we would use.

Research was done on the following tools: [Structurizr](https://structurizr.com/) and [PlantUML](https://plantuml.com/)

## Decision

We have decided to use PlantUML for the following reasons
- Existing experience within the team
- The [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML#getting-started) project we'd use is actively updated
- We already have diagrams built with PlantUML

## Consequences

1. Our GitHub repository will contain references to PlantUML and diagrams, as well as the corresponding PlantUML code to
generate them.
2. Our [API Documentation](https://ministryofjustice.github.io/hmpps-integration-api-docs/) will provide links to diagrams 
hosted in our main repository.
3. When making changes to `.puml` files. We need to be conscious to re-generate diagrams manually.