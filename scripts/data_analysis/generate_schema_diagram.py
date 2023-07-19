import json
import requests
import yaml
import pandas as pd
import os
import sys

from graphviz import Digraph
from constants import common

OUTPUT_FILE = common.SCHEMA_PARENT_CHILD_FILE
DIAGRAM_FILE = common.SCHEMA_DIAGRAM

response_dict = common.extract_data(common.URL)

def main():
    common.prepare_directory(DIAGRAM_FILE)
    aggregate_data_frame = pd.DataFrame()
    is_full_schema_diagram = len(sys.argv) == 1
    if is_full_schema_diagram:
        for schema in response_dict["components"]["schemas"]:
            data_frame = common.findParentSchema(response_dict, schema)
            if not data_frame.empty:
                aggregate_data_frame = pd.concat([aggregate_data_frame, data_frame], axis=0).reset_index(drop=True)
    else:
        for schema in sys.argv[1:]: #1 or more arguments passed in, schema search
            data_frame = common.findParentSchema(response_dict, schema)
            if not data_frame.empty:
                aggregate_data_frame = pd.concat([aggregate_data_frame, data_frame], axis=0).reset_index(drop=True)

    #To ensure we don't break future logic we can remove all rows without a child
    for index, row_data in aggregate_data_frame.iterrows():
        if row_data[2] == "":
            aggregate_data_frame.drop(index, inplace=True)

    print(aggregate_data_frame.groupby("Parent_Schema").count())

    #Parents of parents
    aggregate_data_frame["Searched_bool"] = False
    counter = 0
    while(counter < 8): #Up to 8 steps away parents can be found
        #TODO could do with improving this, as without the counter its an infinite loop as the data_frame keeps growing due to the same searches occuring over and over. 
        #Maybe some kind of additional column to mark a field as being used and thus not searching on it again would significantly speed up processing
        counter +=1
        for index, row_data in aggregate_data_frame.iterrows():
            if row_data[3] == False:
                aggregate_data_frame.at[index,'Searched_bool'] = True
                data_frame = common.findParentSchema(response_dict, row_data[0])
                if not data_frame.empty:
                    data_frame["Searched_bool"] = False
                    aggregate_data_frame = pd.concat([aggregate_data_frame, data_frame], axis=0).reset_index(drop=True)
            elif row_data[3] == True:
                continue

    aggregate_data_frame.drop_duplicates(inplace=True)
    aggregate_data_frame.dropna(inplace=True)

    g = Digraph('g', filename=DIAGRAM_FILE, node_attr={'shape': 'record', 'height': '.1'}, graph_attr={'rankdir': 'LR'})

    for index, row_data in aggregate_data_frame.dropna().iterrows():
        g.node(row_data[0])
        g.edge(row_data[0], row_data[2])

    g.save(filename=DIAGRAM_FILE)
    print(f"Visual saved to {DIAGRAM_FILE=}")
    aggregate_data_frame.to_csv(OUTPUT_FILE)
    print(f"Child-parent data saved to {OUTPUT_FILE=}")

if __name__ == "__main__":
    main()