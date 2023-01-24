# 0002- Use PlantUML as our diagrams as code tool

2023-01-24

## Status

Accepted

## Context

Rather than generating diagrams manually, we want to generate diagrams as code. 
This allows us to store them alongside our code in Git, it also alleviates the requirement of installing various tools
on everybody's workstations to update and manage them and allows us to version control our diagrams.

We need to determine which software we want to use for diagrams as code. Research has been done on various tools such 
as [Structurizr](https://structurizr.com/) and [PlantUML](https://plantuml.com/)

## Decision

We have decided to use PlantUML for the following reasons:
- We already have some experience within the PlantUML team
- The [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML#getting-started) project we'd use is actively updated
- Changes to diagrams will be immediately re-rendered

## Consequences

1. Our GitHub repo will contain some references to PlantUML
2. Our [Docs Site](https://ministryofjustice.github.io/hmpps-integration-api-docs/) can reference diagrams hosted in our
main repo