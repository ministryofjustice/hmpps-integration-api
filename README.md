# HMPPS Integration API Documentation Site

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Ftemplate-documentation-site)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#template-documentation-site "Link to report")

## Contents

- [About this project](#about-this-project)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
  - [Running the application](#running-the-application)
  - [Running checks](#running-checks)
  - [Updating configuration](#updating-configuration)
- [Publishing](#publishing)

## About this project

This repository is used to generate the [technical documentation website](https://ministryofjustice.github.io/hmpps-integration-api-docs) for the [HMPPS Integration API](https://github.com/ministryofjustice/hmpps-integration-api).

## Getting started

### Prerequisites

- [Docker](https://www.docker.com/get-started/)
- [Ruby](https://github.com/rbenv/rbenv)

### Installation

1. Clone the repository.

```bash
git clone git@github.com:ministryofjustice/hmpps-integration-api-docs.git
```

## Usage

### Running the application

To run the application for local development:

```bash
make preview
```

Then visit http://localhost:4567.

### Running checks

To check the application compiles and URLs are valid:

```bash
make check
```

For more details see the [Tech Docs GitHub Pages Publisher GitHub repository](https://github.com/ministryofjustice/tech-docs-github-pages-publisher).

### Updating configuration

Aspects of the documentation site such as the header and sidebar can be configured using [config/tech-docs.yml](config/tech-docs.yml). Further configuration options are described on the [Tech Docs Template website: Global Configuration](https://tdt-documentation.london.cloudapps.digital/configure_project/global_configuration/).

## Publishing

Changes pushed or merged into to `main` are automatically published to GitHub Pages and viewable at https://ministryofjustice.github.io/hmpps-integration-api-docs.

### Reviewing

Should you need to review the content of our documentation, ensure that the `review_in` field is updated as part of the pull request. This should be formatted `yyyy-mm-dd`.

A Ministry of Justice tool named [Tech Docs Monitor _also known as_ Daniel the Manual Spaniel](https://github.com/ministryofjustice/tech-docs-monitor) will send a notification to the slack channel specified in the `owner_slack` property when a document is due for review.

