# HMPPS Integration API Documentation Site

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=for-the-badge&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Ftemplate-documentation-site)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#template-documentation-site "Link to report")

This repository is used to generate the [technical documentation website](https://ministryofjustice.github.io/hmpps-integration-api-docs) for the HMPPS Integration API

---

## Developer Notes

The syntax is Markdown, more details can be found [here](https://daringfireball.net/projects/markdown/).

## Preview docs

You can preview how your changes will look, if you've cloned this repo to your local machine, and run this command:

```
make preview
```

This will run a preview web server on http://localhost:4567 which you can open in your browser.

Use `make check` to compile the site to html and check the URLs are valid.

This is only accessible on your computer, and won't be accessible to anyone else.

For more details see the [tech-docs-github-pages-publisher](https://github.com/ministryofjustice/tech-docs-github-pages-publisher) repository.

## Publishing

Any changes you push/merge into the `main` branch should be published to GitHub Pages site automatically.

## Configuration

The webpage layout is configured using the config/tech-docs.yml file.

The template can be configured in [config/tech-docs.yml](config/tech-docs.yml)

Further configuration options are described on the Tech Docs Template website: [Global Configuration](https://tdt-documentation.london.cloudapps.digital/configure_project/global_configuration/).
