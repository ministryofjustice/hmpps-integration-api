import pandas as pd
import requests
import yaml
import os
import sys

from constants import common

INPUT_FILE = common.SCHEMA_PARENT_CHILD_FILE
OUTPUT_FILE = common.ENDPOINTS_FILE

def main():
    common.check_directory()

    if len(sys.argv) == 1: # Default functionality, pull o utput of previous scripts if it exists
        try:
            data_df = pd.read_csv(INPUT_FILE)
        except FileNotFoundError as fnfe:
            print("Unexpected {0}: {1}".format(type(fnfe), fnfe))
            print("Check the file: {0} exists".format(INPUT_FILE))
            exit
    elif len(sys.argv) == 2: # passing in your own file name
        try:
            data_df = pd.read_csv(str(sys.argv[1]))
        except FileNotFoundError as fnfe:
            print("Unexpected {0}: {1}".format(type(fnfe), fnfe))
            print("Check that {0} exists and is a relative path string".format(str(sys.argv)))
            exit
    else:
        print("Handled error in number of arguments, raising exception:")
        raise TypeError("Too many arguments, expected 0 or 1, got {0}".format(len(sys.argv)-1))
    
    try:
        data_df=data_df[["Parent_Schema","Field","Child_Schema", "Searched_bool"]]
    except KeyError as ke:
        print("Loaded data must contain the following columns")
        [print(item) for item in ["Parent_Schema","Field","Child_Schema", "Searched_bool"]]
        print(f"error raised is {ke=}")
        exit
    
    #Create list of schemas
    schema_list = []
    for index, row_data in data_df.iterrows():
        schema_list.append(row_data[0])
        schema_list.append(row_data[2])
    unique_schema_list = list(dict.fromkeys(schema_list))

    #Logic for one schema
    #This is highly inefficient as it loops over each possible http_method and each successful response variation
    #TODO is there a way to essentially drill down past this variable key value?
    #TODO extract this logic into a function with a docstring
    #DONE - reduce computation effort by skipping unnecessary loops
    dfs=[]
    json_extract = common.extract_data(common.URL)
    for path in common.get_value(json_extract, ["paths"]):
        for http_method in ["get", "put", "post", "head", "delete", "connect", "options"]:
            for successful_response in ["200","201","202","203","204","205","206","207","208","226"]:
                value_to_test = 0
                try:
                    if type(common.get_value(json_extract["paths"][path], [http_method,"responses",successful_response,"content","application/json","schema","$ref"])) != int:
                        value_to_test = common.get_value(json_extract["paths"][path], [http_method,"responses",successful_response,"content","application/json","schema","$ref"])

                    elif type(common.get_value(json_extract["paths"][path], [http_method,"responses",successful_response,"content","application/json","schema","items","$ref"])) != int:
                        value_to_test = common.get_value(json_extract["paths"][path], [http_method,"responses",successful_response,"content","application/json","schema","items","$ref"])

                    else:
                        continue
                except KeyError as ke:
                    value_to_test = 0
                    continue

                try:
                    for schema in unique_schema_list:
                        if "/" in value_to_test and schema in value_to_test: #Look for non-zero values of the required nested reference format   
                            data_dict = {"Path": [path], "HTTP_method": [http_method], "HTTP_response": [successful_response], "Schema": [schema]}
                            dfs.append(pd.DataFrame(data=data_dict))
                except TypeError as te:
                    print(f"Unexpected {te=}, {type(te)=}")
                    print(f"Printing useful metadata, {schema=}, {path=}, {http_method=}, {successful_response=}, {value_to_test=}")
                    continue

    if dfs == []:
        df = pd.DataFrame()
    else:
        df = pd.concat(dfs, axis=0)
        df = df.reset_index(drop=True)

    df.to_csv(OUTPUT_FILE)
    
    print(f"Operation complete, output saved to {OUTPUT_FILE=}", "\n")
    print("Data information:")
    print(df.info())


if __name__ == "__main__":
    main()