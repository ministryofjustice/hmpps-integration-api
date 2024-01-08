## About this section
This section of our repo is for various scripts, such as:
- Scripts for deploying various parts of the services we offer
- Scripts for querying api endpoints (or templates to this effect)
- Data Analysis tooling, such as those used for upstream data investigations
- Client certificate generation

### Client Certificates
This folder contains everything you should need for generating new client certificates

### Data Analysis
The [Data Analysis](https://github.com/ministryofjustice/hmpps-integration-api/blob/10d618e7bcc0a68d1aa9037ba54028426bf50191/scripts/data_analysis) directory houses all of the scripts our data engineer built to speed up and optimise the data investigation process. It has its own README within explaining usage.

## Querying API endpoints
Motivated by the risks clients like Postman cause for the safe storage of credentials, we opted for building our own scripts to access this data. The below sections cover how we handle these queries within our team.
### Development endpoints
The following scripts are templates for this endeavour:
- [query_hmpps_dev.sh](https://github.com/ministryofjustice/hmpps-integration-api/blob/af4fcc057ca6fd561ef48fb87934627983d6cf91/scripts/query_hmpps_dev.sh) - This is used to query our development endpoints, and requires you to have an appropriate api-key. You can generate this yourself or ask the team for one for your use.
  - Usage: `./query_hmpps_dev.sh -e "endpoint" -i "hmppsID"`
  - Example: `./query_hmpps_dev.sh -e "sentences" -i "X739869"`
- If you want to query upstream dev endpoints, you will need to instead refer to our protected confluence space on this, see [Access upstream dev endpoints](https://dsdmoj.atlassian.net/wiki/spaces/HIA/pages/4500980971/Get+access+to+upstream+APIs#Accessing-upstream-dev-endpoints)

