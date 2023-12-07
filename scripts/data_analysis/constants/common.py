"""A module containing static variables and common methods all in one place"""
import os
import requests
import yaml

import pandas as pd

DEFAULT_URL = 'https://api-dev.prison.service.justice.gov.uk/v3/api-docs'
URLS = [
#API Docs
  "https://manage-adjudications-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://raw.githubusercontent.com/ministryofjustice/hmpps-complexity-of-need/main/Complexity%20Of%20Need%20API%20Specification.yaml",
#  "https://court-register-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://raw.githubusercontent.com/ministryofjustice/curious-API/main/curious-api-specification.yaml",
  "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/v3/api-docs",
#  "https://keyworker-api-dev.prison.service.justice.gov.uk/v3/api-docs",
#  "https://allocation-manager-staging.apps.live-1.cloud-platform.service.justice.gov.uk/api-docs/index.html",
  "https://community-api.test.probation.service.justice.gov.uk/v3/api-docs/Community%20API",
#  "https://probation-offender-events-dev.hmpps.service.justice.gov.uk/swagger-ui.html",
  "https://probation-offender-search-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://api-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://offender-events-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://prisoner-search-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://offender-dev.aks-dev-1.studio-hosting.service.justice.gov.uk/v3/api-docs",
  "https://prison-register-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-allocations-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://probation-teams-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-interventions-service-dev.apps.live-1.cloud-platform.service.justice.gov.uk/v3/api-docs",
  "https://restricted-patients-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-staff-lookup-service-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-tier-dev.hmpps.service.justice.gov.uk/v3/api-docs",
  "https://token-verification-api-dev.prison.service.justice.gov.uk/v3/api-docs",
  "https://hmpps-workload-dev.hmpps.service.justice.gov.uk/v3/api-docs",

#Microservices
  "https://raw.githubusercontent.com/ministryofjustice/hmpps-complexity-of-need/main/Complexity%20Of%20Need%20API%20Specification.yaml"
]

OUTPUTS_DIR = "outputs/"
SCHEMA_SEARCH_REPORT = OUTPUTS_DIR + "schema_report.csv"
PATH_SEARCH_REPORT = OUTPUTS_DIR + "path_report.csv"
SCHEMA_FIELD_FILE = "outputs/schema_field.csv"
SCHEMA_PARENT_CHILD_FILE = "outputs/schema_parent_child.csv"
SCHEMA_DIAGRAM = "outputs/schema_hierachy.dot"
ENDPOINTS_FILE = "outputs/endpoint_analysis.csv"
TIMEOUT = 10


def prepare_directory(filename=""):
    """
    A function to prepare the current directory for outputs of any script
    You can provide an optional filename parameter to delete that file if it exists.
    This is to get a clean output file.

        Parameters:
            (optional) filename (str): The name of the output file to be removed

    """
    if not os.path.exists("outputs/"):
        os.mkdir("outputs/")
    if os.path.exists(filename) and str(filename) != "":
        os.remove(filename)


def extract_data(url=DEFAULT_URL):
    """
    Makes a get request against a provided url, 
    returning the response as a dictionary object if possible

        Parameters:
            url (string): a url string to a raw json or yaml source for an API documentation. 
                Default: Constant URL parameter

        Returns:
            data (dict): A dictionary object representing the response yaml/json, 
                Unsuccesful request: An empty dictionary
    """
    try:
        response = requests.get(url, timeout=TIMEOUT)
        if response.status_code == 200 and url.endswith('yaml'):
            data = yaml.safe_load(response.text)
        elif response.status_code == 200 and not url.endswith('yaml'):
            data = response.json()
        else:
            print(url, " responded with ", response.status_code,
                  " returning empty dictionary")
            data = '{}'
    except requests.JSONDecodeError:
        print(f"{url} is not a valid json source, returning empty dictionary object")
        data = '{}'
    except requests.exceptions.Timeout:
        print(f"Timeout exception caught for {url}, returning empty dictionary object")
        data = '{}'
    except requests.exceptions.TooManyRedirects:
    # Tell the user their URL was bad and try a different one
        print(f"TooManyRedirects exception caught for {url}, Returning empty dictionary object")
        data = '{}'
    except requests.exceptions.RequestException as r_e:
    # catastrophic error. bail.
        raise SystemExit(r_e) from r_e

    return data


def find_parent_schema(response_dict, child_schema):
    """
    Search all schemas for any reference to the child schema (i.e. find all direct parents).
    Returns the results in a dataframe.
    This function assumes the dictionary object passed in is generated from OpenAPI Swagger spec

        Parameters:
            response_dict (dict): A dictionary object representing the extract from API Docs, 
                Example: The output of the extract_data function above
            child_schema (str): A string of the exact schema name to search for

        Returns:
            data_frame (pd.DataFrame): a dataframe of schema, field and reference information.
                No results found: An empty dataframe

    """
    data_frames = []

    for schema in response_dict["components"]["schemas"]:
        for field in response_dict["components"]["schemas"][schema]["properties"]:
            try:
                ref_to_test = response_dict["components"]["schemas"][schema]["properties"][field]["$ref"]
                if isinstance(ref_to_test, str):
                    nested_ref = ref_to_test
            except KeyError:
                nested_ref = ""

            try:
                item_ref_to_test = response_dict["components"]["schemas"][schema]["properties"][field]["items"]["$ref"]
                if isinstance(item_ref_to_test, str):
                    nested_item_ref = item_ref_to_test
            except KeyError:
                nested_item_ref = ""

            if child_schema in nested_ref:
                data_dict = {'Parent_Schema': [schema],
                             'Field': [field],
                             'Child_Schema': [nested_ref.split('/')[-1]]}
                data_frames.append(pd.DataFrame(data=data_dict))
            elif child_schema in nested_item_ref:
                data_dict = {'Parent_Schema': [schema],
                             'Field': [field],
                             'Child_Schema': [nested_item_ref.split('/')[-1]]}
                data_frames.append(pd.DataFrame(data=data_dict))

    if not data_frames:
        return pd.DataFrame()

    data_frame = pd.concat(data_frames, axis=0)
    data_frame = data_frame.reset_index(drop=True)
    return data_frame

def get_nested_dictionary_or_value(my_dict, keys, return_value=0):
    """
    A Really powerful iterative function to explore very deeply nested dictionaries.
    You can use this to define generic search functions into nested dict objects,
    bypassing the need for the dict[][] nomenclature
    This also works when one of the keys might be missing, exiting without error.
    It takes in a dictionary, and a list of key values, to return nested dictionary objects 
    however many levels deep you require WITHOUT lines and lines of code.

        Parameters:
            my_dict (dict): Dictionary object, can use this function recursively.
            keys (list): A list of keys representing each nested level.

        Returns:
            my_dict (dict/string) 
                Which is the nested dictionary or value that you require..
            return_value 
                ..Unless one cannot be found in which case what you specify is returned.
                default: 0

        Example:
            Suppose you have a dictionary like so:
            `my_dict = {"a": {"b": {"c": {"d": 1}}}}`
            To get to the `1`, instead of having to write:
            `my_dict["a"]["b"]["c"]["d"]`
            You can use this function by passing in those keys as a list, like so:
            get_nested_dictionary_or_value(my_dict, ["a", "b", "c", "d"])

        Usage:
            Where this becomes more powerful is this method handles when one of those keys 
            might be missing rather than just exiting with a TypeError/KeyError, allowing you to
            to iterate over nested objects that might not always exist, and use the return_value
            to check for this existence in your own logic.

    """
    if not keys:
        return my_dict
    key = keys[0]
    try:
        newdict = my_dict[key]
    except (TypeError, KeyError):
        return return_value
    return get_nested_dictionary_or_value(newdict, keys[1:])