###########
# IMPORTS #
###########
import pandas as pd
import requests
import yaml


#############
# CONSTANTS #
#############
URL = 'https://api-dev.prison.service.justice.gov.uk/v3/api-docs'
URLS = [
#API Docs
  "https://manage-adjudications-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://court-register-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://raw.githubusercontent.com/ministryofjustice/curious-API/main/curious-api-specification.yaml",
  "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/v3/api-docs",
  "https://keyworker-api-dev.prison.service.justice.gov.uk/v3/api-docs",
#Typo in URL? "https://allocation-manager-staging.apps.live-1.cloud-platform.service.justice.gov.uk/api-docs/index.html",
#timeout  "https://community-api.test.probation.service.justice.gov.uk/v3/api-docs/Community%20API",
#site can't be reached "https://probation-offender-events-dev.hmpps.service.justice.gov.uk/swagger-ui.html",
  "https://probation-offender-search-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://api-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://offender-events-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://prisoner-offender-search-dev.prison.service.justice.gov.uk/v3/api-docs",
# Timeout  "https://offender-dev.aks-dev-1.studio-hosting.service.justice.gov.uk/v3/api-docs",
  "https://prison-register-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-allocations-dev.hmpps.service.justice.gov.uk/v3/api-docs",
# 403 forbidden, wrong url "https://probation-teams-dev.prison.service.justice.gov.uk/swagger-ui/index.html",
  "https://hmpps-interventions-service-dev.apps.live-1.cloud-platform.service.justice.gov.uk/v3/api-docs",
  "https://restricted-patients-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-staff-lookup-service-dev.hmpps.service.justice.gov.uk/v3/api-docs",
# 503 unavailable, wrong url "https://hmpps-tier-dev.hmpps.service.justice.gov.uk/swagger-ui.html",
  "https://token-verification-api-dev.prison.service.justice.gov.uk/v3/api-docs",
# 503, wrong url "https://hmpps-workload-dev.hmpps.service.justice.gov.uk/swagger-ui.html"

#Microservices
  "https://raw.githubusercontent.com/ministryofjustice/hmpps-complexity-of-need/main/Complexity%20Of%20Need%20API%20Specification.yaml"
]

#############
# FUNCTIONS #
#############

def extract_data(url=URL):
    """
    TODO Need to add logic to handle a timeout on a URL, or otherwise long response time
    Description

        Parameters:
            url (string): a url string to a raw json or yaml source for an API documentation. Default: Constant URL parameter

        Returns:
            TODO need to add empty dictionary output
            data (dict): A dictionary object representing the response yaml/json
    """
    response = requests.get(url)
    if response.status_code == 200:
        if url.endswith('yaml'):
            data = yaml.safe_load(response.text)
        else:
            data = response.json()
        return data
    else:
        print(url, " responded with ", response.status_code)
        return '{}'