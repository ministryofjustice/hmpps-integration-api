"""Module to generate a schema parent-child relationship diagram, with corresponding outputs"""
import sys

import pandas as pd

from graphviz import Digraph
from constants import common

OUTPUT_FILE = common.SCHEMA_PARENT_CHILD_FILE
DIAGRAM_FILE = common.SCHEMA_DIAGRAM

response_dict = common.extract_data(common.URL)

def create_aggregate_data_frame(dict_object):
    """
    Search through the keys of a dataframe, which are schema names, 
    and aggregate the data into a single dataframe.

        Parameters:
            dict_object (dictionary): Dictionary where keys are schema names

        Returns:
            aggregate_data_frame (pd.DataFrame): Data frame of all the parent-child schema relations
    """
    for schema in dict_object:
        data_frame = common.find_parent_schema(response_dict, schema)
        if not data_frame.empty:
            aggregate_data_frame = pd.concat([aggregate_data_frame, data_frame], axis=0)
            aggregate_data_frame.reset_index(drop=True)
    return aggregate_data_frame

def main():
    """The main method, used to call the script. Command line arguments used as search terms"""
    common.prepare_directory(DIAGRAM_FILE)
    aggregate_data_frame = pd.DataFrame()
    is_full_schema_diagram = len(sys.argv) == 1
    if is_full_schema_diagram:
        aggregate_data_frame = create_aggregate_data_frame(response_dict["components"]["schemas"])
    else:
        aggregate_data_frame = create_aggregate_data_frame(sys.argv[1:])

    #To ensure we don't break future logic we can remove all rows without a child
    for index, row_data in aggregate_data_frame.iterrows():
        if row_data[2] == "":
            aggregate_data_frame.drop(index, inplace=True)

    print(aggregate_data_frame.groupby("Parent_Schema").count())

    #Parents of parents
    aggregate_data_frame["Searched_bool"] = False
    counter = 0
    while counter < 8: #Up to 8 steps away parents can be found
        counter +=1
        for index, row_data in aggregate_data_frame.iterrows():
            if row_data[3] is False:
                aggregate_data_frame.at[index,'Searched_bool'] = True
                data_frame = common.find_parent_schema(response_dict, row_data[0])
                if not data_frame.empty:
                    data_frame["Searched_bool"] = False
                    aggregate_data_frame = pd.concat([aggregate_data_frame, data_frame], axis=0)
                    aggregate_data_frame.reset_index(drop=True)
            elif row_data[3] is True:
                continue

    aggregate_data_frame.drop_duplicates(inplace=True)
    aggregate_data_frame.dropna(inplace=True)

    schema_graph = Digraph('schema_graph',
                           filename=DIAGRAM_FILE,
                           node_attr={'shape': 'record', 'height': '.1'},
                           graph_attr={'rankdir': 'LR'})

    for index, row_data in aggregate_data_frame.dropna().iterrows():
        schema_graph.node(row_data[0])
        schema_graph.edge(row_data[0], row_data[2])

    schema_graph.save(filename=DIAGRAM_FILE)
    print(f"Visual saved to {DIAGRAM_FILE=}")
    aggregate_data_frame.to_csv(OUTPUT_FILE)
    print(f"Child-parent data saved to {OUTPUT_FILE=}")

if __name__ == "__main__":
    main()
