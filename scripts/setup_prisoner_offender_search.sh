#!/bin/bash

set -ex

curl https://prisoner-offender-search-dev.prison.service.justice.gov.uk/v3/api-docs > prisoner-offender-search.json

sed -i "s+*/*+application/json+g" prisoner-offender-search.json

