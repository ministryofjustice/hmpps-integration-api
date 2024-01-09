"""Module to discover endpoints for a schema and its parents"""

import sys
import pandas as pd

from constants import common

INPUT_FILE = common.SCHEMA_PARENT_CHILD_FILE
OUTPUT_FILE = common.ENDPOINTS_FILE

def load_data_from_file(file_name):
    """
    Used to look for and load an input file containing 
    Schema parent-child information into a Dataframe

        Parameters:
            file_name (str): The full path and filename of the file to load.

        Returns:
            loaded_data_frame (pd.DataFrame): A Dataframe with 4 columns.
                Columns indicated by this list: 
                [\"Parent_Schema\",\"Field\",\"Child_Schema\", \"Searched_bool\"]
    """
    common.prepare_directory(OUTPUT_FILE)
    column_list = ["Parent_Schema","Field","Child_Schema", "Searched_bool"]
    is_no_arguments = len(sys.argv) == 1
    is_file_name_provided = len(sys.argv) == 2
    try:
        if is_no_arguments:
            loaded_data_frame = pd.read_csv(file_name)
        elif is_file_name_provided:
            loaded_data_frame = pd.read_csv(str(sys.argv[1]))
        else:
            raise TypeError(f"Too many arguments, expected 0 or 1, got {len(sys.argv)-1}")
        loaded_data_frame=loaded_data_frame[column_list]
    except FileNotFoundError as fnfe:
        try:
            print(f"Unexpected {type(fnfe)}: {fnfe}")
            print(f"You must first create your file named: {str(sys.argv[1])}")
            print(f"Alternatively, run the default option with: {INPUT_FILE}")
            sys.exit()
        except IndexError:
            print("Please generate an appropriate file.")
            print("Alternatively, specify an appropriate file to load")
            sys.exit()
    except TypeError as t_e:
        print(f"{type(t_e)} Error handled and can't continue, exiting. Error details:, {t_e=}")
        sys.exit()
    except KeyError as k_e:
        print("Loaded data must contain the following columns")
        print(column_list)
        print(f"error raised is {k_e=}")
        sys.exit()
    return loaded_data_frame

def find_nested_schema_reference(api_url, schema_list):
    """
    Used to search API documentation for nested references 
    of schemas in each endpoint's successful response objects

        Parameters: 
            api_url (str): The json/yaml source url of a set of OpenAPI / Swagger docs
            schema_list (list[str]): A list of strings representing the schemas to be searched for

        Outputs:
            data_frame (pd.DataFrame): A dataframe of end points related to which schema, 
                or an empty dataframe if nothing is found

        Example:
            `find_nested_schema_reference(common.URL, ["AddressDto","SentenceCalcDates"])`
    """
    data_frames = []
    successful_response_list = ["200","201","202","203","204","205","206","207","208","226"]

    dict_extract = common.extract_data(api_url)
    for path in dict_extract["paths"]:
        for http_method in dict_extract["paths"][path]:
            if http_method in ["get", "post"]:
                for response in common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses"]):
                    if response in successful_response_list:
                        value_to_test = 0
                        try:
                            if isinstance(common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses", response, "content", "application/json", "schema", "$ref"]), int) is False:
                                value_to_test = common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses", response, "content", "application/json", "schema", "$ref"])

                            elif isinstance(common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses", response, "content", "application/json", "schema", "items", "$ref"]), int) is False:
                                value_to_test = common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses", response, "content", "application/json", "schema", "items","$ref"])

                            elif isinstance(common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses", response, "content", "*/*", "schema", "$ref"]), int) is False:
                                value_to_test = common.get_nested_dictionary_or_value(dict_extract, ["paths", path, http_method, "responses", response, "content", "*/*", "schema", "$ref"])

                            else:
                                continue
                        except KeyError:
                            value_to_test = 0
                            continue

                        try:
                            for schema in schema_list:
                                if "/" in value_to_test and schema in value_to_test: #Look for non-zero values of the required nested reference format
                                    data_dict = {"Path": [path], "HTTP_method": [http_method], "HTTP_response": [response], "Schema": [schema]}
                                    data_frames.append(pd.DataFrame(data=data_dict))
                        except TypeError as t_e:
                            print(f"Unexpected {t_e=}, {type(t_e)=}")
                            print(f"Printing useful metadata, {schema=}, {path=}, {http_method=}, {response=}, {value_to_test=}")
                            continue

    if not data_frames:
        data_frame = pd.DataFrame()
    else:
        data_frame = pd.concat(data_frames, axis=0)
        data_frame = data_frame.reset_index(drop=True)
    return data_frame


def main():
    """The main method called by the script"""
    loaded_data_frame = load_data_from_file(INPUT_FILE)

    schema_list = []
    for i in range(len(loaded_data_frame)):
        schema_list.append(loaded_data_frame.iloc[i, 0]) #Parent_Schema
        schema_list.append(loaded_data_frame.iloc[i, 2]) #Child_Schema
    unique_schema_list = list(dict.fromkeys(schema_list))

    data_frame = find_nested_schema_reference(common.DEFAULT_URL, unique_schema_list)
    data_frame.to_csv(OUTPUT_FILE)

    print(f"Operation complete, output saved to {OUTPUT_FILE=}", "\n")
    print("Data information:")
    print(data_frame.info())


if __name__ == "__main__":
    main()
