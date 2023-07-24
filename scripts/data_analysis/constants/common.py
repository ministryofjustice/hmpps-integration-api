import pandas as pd
import requests
import yaml
import os

URL = 'https://api-dev.prison.service.justice.gov.uk/v3/api-docs'
SCHEMA_FIELD_FILE = "outputs/schema_field.csv"
SCHEMA_PARENT_CHILD_FILE = "outputs/schema_parent_child.csv"
SCHEMA_DIAGRAM = "outputs/schema_hierachy.dot"

def prepare_directory(filename=""):
  if os.path.exists("outputs/") == False:
      os.mkdir("outputs/")
  if os.path.exists(f"outputs/{filename}") and str(filename) != "":
      os.remove(f"filename")

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

def findParentSchema(response_dict, child_schema):
    """
    Search all schemas for any reference to the child schema (i.e. find all direct parents) and return results in a dataframe.
    This function assumes the dictionary object passed in is generated from OpenAPI Swagger spec

        Parameters:
            response_dict (dict): A dictionary object representing the extract from API Docs, such as the output of the extract_data function above
            child_schema (str): A string of the exact schema name to search for

        Returns:
            data_frame (pd.DataFrame): a dataframe of schema, field and reference information.
                Default value is an empty dataframe if no results are found, to avoid type errors in concatenation

    """
    data_frames = []

    for schema in response_dict["components"]["schemas"]:
        for field in response_dict["components"]["schemas"][schema]["properties"]:
            try:
                if type(response_dict["components"]["schemas"][schema]["properties"][field]["$ref"]) is str:
                    nested_ref = response_dict["components"]["schemas"][schema]["properties"][field]["$ref"]
            except KeyError as ke:
                nested_ref = ""

            try:    
                if type(response_dict["components"]["schemas"][schema]["properties"][field]["items"]["$ref"] is str):
                    nested_item_ref = response_dict["components"]["schemas"][schema]["properties"][field]["items"]["$ref"]
            except KeyError as ke:
                nested_item_ref = ""

            if child_schema in nested_ref: #Note that this has the increased benefit of a partial match effect too 
                data_dict = {'Parent_Schema': [schema], 'Field': [field], 'Child_Schema': [nested_ref.split('/')[-1]]}
                data_frames.append(pd.DataFrame(data=data_dict))
            elif child_schema in nested_item_ref:
                data_dict = {'Parent_Schema': [schema], 'Field': [field], 'Child_Schema': [nested_item_ref.split('/')[-1]]}
                data_frames.append(pd.DataFrame(data=data_dict))
    
    if data_frames == []:
        return pd.DataFrame()
    else:
        data_frame = pd.concat(data_frames, axis=0)
        data_frame = data_frame.reset_index(drop=True)
        return data_frame