"""A module containing static variables and common methods all in one place"""
import os
import requests
import yaml

import pandas as pd

URL = 'https://api-dev.prison.service.justice.gov.uk/v3/api-docs'
SCHEMA_FIELD_FILE = "outputs/schema_field.csv"
SCHEMA_PARENT_CHILD_FILE = "outputs/schema_parent_child.csv"
SCHEMA_DIAGRAM = "outputs/schema_hierachy.dot"
ENDPOINTS_FILE = "outputs/endpoint_analysis.csv"

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

def extract_data(url=URL):
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
    response = requests.get(url, timeout=10)
    if response.status_code == 200 and url.endswith('yaml'):
        data = yaml.safe_load(response.text)
    elif response.status_code == 200 and not url.endswith('yaml'):
        data = response.json()
    else:
        print(url, " responded with ", response.status_code, " returning empty dictionary")
        data = '{}'
    return data

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
    
def get_nested_dictionary_or_value(my_dict, keys):
    """
    A Really powerful iterative function to explore very deeply nested dictionaries. 
    It takes in a dictionary, and a list of key values, to return nested dictionary objects 
    however many levels deep you require WITHOUT lines and lines of code.
    
        Parameters:

        Returns:
            my_dict (dict/string) 
            Which is the nested dictionary or value that you require, 
            unless one cannot be found in which case 0 is returned.

        Example:
            Suppose you have a dictionary like so:
            `my_dict = {"a": {"b": {"c": {"d": 1}}}}`
            To get to the `1`, instead of having to write:
            `my_dict["a"]["b"]["c"]["d"]` (which you can imagine could be painful for long key names)
            You can use this function by passing in those keys as a list, like so:
            get_value(my_dict, ["a", "b", "c", "d"])

        Usage:
            Where this becomes more powerful is this method handles when one of those keys 
            might be missing rather than just exiting with a TypeError/KeyError, allowing you to
            to iterate over nested objects that might not always exist.
    
    """
    if not keys:
        return my_dict
    key = keys[0]
    try:
        newdict = my_dict[key]
    except (TypeError, KeyError):
        return 0
    return get_nested_dictionary_or_value(newdict, keys[1:])