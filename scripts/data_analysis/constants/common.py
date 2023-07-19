import pandas as pd
import requests
import yaml

URL = 'https://api-dev.prison.service.justice.gov.uk/v3/api-docs'

def extract_data(url=URL):
    """
    TODO Need to add logic to handle a timeout on a URL, or otherwise long response time
    Description

        Parameters:
            url (string): a url string to a raw json or yaml source for an API documentation. Default: Constant URL parameter

        Returns:
            data (dict): A dictionary object representing the response yaml/json, or an empty dictionary if its an unsuccessful request
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
