"""Module to extract all schemas and their fields into a csv file"""
import sys
import pandas as pd

from constants import common

OUTPUT_FILE = common.SCHEMA_FIELD_FILE

<<<<<<< HEAD
def main():
    is_no_arguments = len(sys.argv) == 1
    is_url_provided = len(sys.argv) == 2
=======
json_extract = common.extract_data(common.DEFAULT_URL)
>>>>>>> main

    if is_no_arguments:
        json_extract = common.extract_data(common.DEFAULT_URL)
    elif is_url_provided:
        json_extract = common.extract_data(sys.argv[1])
    common.prepare_directory()
    schema_field_df = pd.DataFrame(columns=["Schema", "Field"])
    for schema in json_extract["components"]["schemas"]:
        for field in json_extract["components"]["schemas"][schema]["properties"]:
            new_row_dict = {"Schema": [schema], "Field": [field]}
            schema_field_df = pd.concat([schema_field_df, pd.DataFrame(data=new_row_dict)], axis=0)
    schema_field_df = schema_field_df.reset_index(drop=True)

    schema_field_df.to_csv(OUTPUT_FILE)

    print(schema_field_df.info())
    print(f"Data loaded into {OUTPUT_FILE=}")

if __name__ == "__main__":
    main()
