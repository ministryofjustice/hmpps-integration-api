from constants import common

import pandas as pd
import requests
import yaml
import os

OUTPUT_FILE = common.SCHEMA_FIELD_FILE

json_extract = common.extract_data(common.URL)

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
