"""TODO"""
import sys
import pandas as pd

from constants import common

SCHEMA_RESULT_COLS = ["Schema", "Field", "Field_metadata"]
PATH_RESULT_COLS = ["Path", "Http_method", "Path_metadata"]

# Define a search function
def search_string(string_to_search, search_phrase) -> str:
    """TODO"""
    return str(search_phrase).lower().replace(" ", "") in str(string_to_search).lower().replace(" ", "")

def get_schema_or_path_data(dict_resp,
                            dict_keys_list,
                            schema_bool=True,
                            metadata_divider='|') -> pd.DataFrame:
    """ 
    TODO
    Currently only returns all schemas, fields and nested metadata values (not metadata keys).
    Outputs as a dataframe. 
    If errenous schema or field data is attempted to be appended the row will just be empty.
    Needs to handle an empty dictionary.
    """
    if schema_bool:
        data_frame_cols = SCHEMA_RESULT_COLS
    else:
        data_frame_cols = PATH_RESULT_COLS
    list_of_lists = []
    try:
        nested_dict=common.get_nested_dictionary_or_value(dict_resp,dict_keys_list,return_value={})
    except TypeError as t_e:
        print("You need to provide a dictionary object", "\n")
        print(f"Unexpected {t_e=}, {type(t_e)=}")
        sys.exit()
    except KeyError as k_e:
        print("Response is not compatable with this function")
        print("Check it is standardised swagger spec","\n")
        print(f"Unexpected {k_e=}, {type(k_e)=}")
        sys.exit()

    try:
        for key_a in nested_dict:
            if isinstance(common.get_nested_dictionary_or_value(nested_dict, [key_a, "properties"]), int) is False:
                modelled_nested_dict = nested_dict[key_a]["properties"]
            else:
                modelled_nested_dict = nested_dict[key_a]
            for key_b in modelled_nested_dict:
                try:
                    row_builder = []
                    row_builder.append(key_a)
                    row_builder.append(key_b)
                    string_to_build = str(metadata_divider)
                    for metadata_value in modelled_nested_dict[key_b].values():
                        string_to_build = f"{string_to_build} {metadata_value} |"
                    row_builder.append(string_to_build)
                    list_of_lists.append(row_builder)
                except AttributeError:
                    row_builder = [None, None, None]
        data_frame = pd.DataFrame(list_of_lists, columns=data_frame_cols)

    except KeyError:
        sys.exit()
    return data_frame

def search_df_for_phrase(data_frame, search_phrase) -> pd.DataFrame:
    """
    TODO
    """
    mask = data_frame.applymap(lambda df_cell: search_string(df_cell, search_phrase))
    filtered_data_frame = data_frame.loc[mask.any(axis=1)]
    return filtered_data_frame

def add_column_value(data_frame, column_name, value) -> pd.DataFrame:
    """ 
    #TODO - this function is just responsible for adding an extra column to a dataframe for a phrase
    """
    data_frame[column_name] = value
    return data_frame

def find_context(target_str, search_str, col_sep='|') -> str:
    """ 
    TODO
    #Currently doesn't work for enums?
    """
    context = ''
    formatted_target = str.lower(target_str)
    formatted_search = str.lower(search_str)
    index_pos = formatted_target.find(formatted_search)
    if index_pos != -1:
        context_left = formatted_target.rfind(col_sep, 0, index_pos)
        context_right = formatted_target.find(col_sep,index_pos) #if -1, then don't populate?
        if context_left != -1 and context_right != -1:
            context = target_str[context_left+1:context_right] #So then here, if both -1, just return the string.
    #If the term doesn't exist this will actually default to [0,-1], which is everything but the last character
    return context

def search_api_for_phrase(url=common.DEFAULT_URL, search_phrase="") -> (pd.DataFrame, pd.DataFrame):
    """
    TODO
    """
    json_extract = common.extract_data(url)
    schema_data_frame = get_schema_or_path_data(
                json_extract,
                dict_keys_list=["components","schemas"],
                schema_bool=True,
                metadata_divider='|'
            )
    path_data_frame   = get_schema_or_path_data(
                json_extract,
                dict_keys_list=["paths"],
                schema_bool=False,
                metadata_divider='|'
            )

    filtered_schema_data_frame = search_df_for_phrase(schema_data_frame, search_phrase)
    filtered_path_data_frame = search_df_for_phrase(path_data_frame, search_phrase)

    return filtered_schema_data_frame, filtered_path_data_frame

def retrieve_context(data_frame, column_name, search_phrase, col_sep='|') -> pd.DataFrame:

    """ 
    #TODO returns a dataframe with a Context column added
    """
    ##Currently not filtering context properly, look into this.
    #started happening since this was .loc

    copy_data_frame = data_frame.copy()
    copy_data_frame["Context"] = ''
    column_index = copy_data_frame.columns.get_loc(column_name)
    context_series = copy_data_frame.loc[:, column_name].apply(lambda x: find_context(x, search_phrase, col_sep))
    copy_data_frame["Context"] = context_series
    for index, row in copy_data_frame.iterrows():
        if str.lower(search_phrase) in str.lower(row[column_index]):
            copy_data_frame.at[index, 'Context'] = str.lower(row[column_index]).replace(search_phrase, f">>>{search_phrase}<<<")
    return copy_data_frame

def main():
    """The main method, used to call the script. Command line argument used as the search phrase"""
    common.prepare_directory(common.SCHEMA_SEARCH_REPORT)
    common.prepare_directory(common.PATH_SEARCH_REPORT)
    is_no_search_phrase = len(sys.argv) == 1
    is_one_search_phrase = len(sys.argv) == 2
    print(sys.argv)
    if is_no_search_phrase:
        print("Please provide a phrase to search for")
        sys.exit()
    elif is_one_search_phrase:

        ### Need to extract this to a variable possibly
        empty_schema_report = pd.DataFrame(
            columns=["Schema", "Field", "Field_metadata", "Context", "Search Phrase", "API"])
        empty_path_report = pd.DataFrame(
            columns=["Path", "Http_method", "Path_metadata", "Context", "Search Phrase", "API"])
        empty_schema_report.to_csv(common.SCHEMA_SEARCH_REPORT, index=False)
        empty_path_report.to_csv(common.PATH_SEARCH_REPORT, index=False)

        for url in common.URLS:
            schema_df, path_df = search_api_for_phrase(url, sys.argv[1])
            #Schema search
            schema_w_context = retrieve_context(schema_df, "Field_metadata", sys.argv[1])
            full_schema_w_search = add_column_value(schema_w_context, "Search Phrase", sys.argv[1])
            full_schema_w_search = add_column_value(schema_w_context, "API", url)
            full_schema_w_search.to_csv(common.SCHEMA_SEARCH_REPORT,
                                        mode='a',
                                        header=False,
                                        index=False)
            #Path search
            path_w_context = retrieve_context(path_df, "Path_metadata", sys.argv[1])
            full_path_w_search = add_column_value(path_w_context, "Search Phrase", sys.argv[1])
            full_path_w_search = add_column_value(path_w_context, "API", url)
            full_path_w_search.to_csv(common.PATH_SEARCH_REPORT,
                                      mode='a',
                                      header=False,
                                      index=False)

    print(f"Reports created: {common.SCHEMA_SEARCH_REPORT} and {common.PATH_SEARCH_REPORT}")

if __name__ == "__main__":
    main()
