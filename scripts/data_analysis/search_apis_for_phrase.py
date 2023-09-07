"""Module to search all APIs on Structurizer, hardcoded on 05/09/2023"""
import sys
import pandas as pd

from constants import common

SCHEMA_RESULT_COLS = ["Schema", "Field", "Field_metadata"]
PATH_RESULT_COLS = ["Path", "Http_method", "Path_metadata"]

# Define a search function
def search_string(string_to_search, search_phrase) -> bool:
    """
    Searches a string for a particular phrase.
    Search logic includes logic to mitigate risk of missed searches in the following ways:
        - Allows for phrases to be searched for in both field names and descriptions.
        - Allows mixed case searches to be conducted

    Parameters:
        string_to_search (str): String to search.
        search_phrase (str): The word, or phrase, to search for in the String to search.

    Returns:
        True/False (boolean): Boolean conditional on the phrase being found
    """
    return str(search_phrase).lower().replace(" ", "") in str(string_to_search).lower().replace(" ", "")

def get_schema_or_path_data(dict_resp,
                            dict_keys_list,
                            schema_bool=True,
                            metadata_divider='|') -> pd.DataFrame:
    """ 
    Expects a dictionary response object along with a list of dictionary keys 
    used to drill down into the nested Dictionary for either the Schemas or Paths.
    Everything deper than either Schema/Field or Path/HTPP is considered as 'Metadata'
    And each value associated with a different key is simply seperated with the metadata_divider.
    This long metadata string is much easier to search and more flexibly generated regardless
    of dictionary complexity or nested depth > 3.
    If errenous schema or field data is attempted to be appended the row will just be empty.
    
    Parameters:
        dict_resp (dictionary): The API response in Dictionary format
        dict_keys_list (List(str)): The ordered keys listed, to retrieve the
            appropriate depth of the Dictionary.
            Default/nothing found: {}
        schema_bool (boolean): Boolean used to indicate whether it is a 
            'Schema' or 'Path' search the keys are being provided for.
            Default: True
        metadata_divider (str): A single character string used as the desired
            seperator between dictionary values that comprise the metadata string.
            Default: '|'

    Returns:
        data_frame (pd.DateFrame): A dataframe containing all of the relevant 
            Schema or Path data ready to be searched.
            
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
        print("Error in provided keys, please check your input values. Exiting")
        sys.exit(1)
    return data_frame

def search_df_for_phrase(data_frame, search_phrase) -> pd.DataFrame:
    """
    Searches target data_frame cell by cell for a search_phrase. 
    creates a mask of True/False values for each and returns a dataframe
    where only the rows contained at least 1 true value in the mask.
    See the "search_string" function to understand the search logic applied.

    Parameters:
        data_frame (pd.DataFrame): The data frame to search.
        search_phrase (str): The phrase to search the data frame for.

    Returns:
        filtered_data_frame (pd.DataFrame): The filtered data_frame
            based on the successful search results. Can be empty if no
            results are found.
    """
    mask = data_frame.applymap(lambda df_cell: search_string(df_cell, search_phrase))
    filtered_data_frame = data_frame.loc[mask.any(axis=1)]
    return filtered_data_frame

def add_column_value(data_frame, column_name, value) -> pd.DataFrame:
    """ 
    This function forces the logic of adding a column using a simple assignment into a function.
    This prevents it failing to add a column to a view of the dataframe.
    There is probably a better way than this, but it could not be found quickly.

    Parameters:
        data_frame (pd.DataFrame): The data frame to which you want to add a permanent column.
        column_name (str): The name of the column you wish to add.
        value (any primitive): The value you wish to add for each row in this new column.

    Returns:
        data_frame (pd.DataFrame): The original data_frame with the new column added.
    """
    data_frame[column_name] = value
    return data_frame

def find_context(target_str, search_str, col_sep='|') -> str:
    """ 
    A powerful function designed to highlight in a string divided
    into parts with a divider where the search string is.
    (divider exmaples: the ',' in a csv, or a custom one like the '|' in the metadata string)

    Parameters:
        target_str (str): The divided string that is to be searched
        search_str (str): The search phrase to search the target_str

    Returns:
        context (str): The context of the search_str.
        If the string is not divided it will simply return the full string.

    Example:
        find_context('| Hello | Cruel | World |', 'Cru') -> ' Cruel '

    Limitations:
        - Currently isn't perfect for nested lists within the string.
        - divider character cannot be anywhere in the string other than as a divider.
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
    Specifically searches the api-docs of a desired API url for a particular search phrase contained anywhere 
    within either the components/schema section or paths section of the json/yaml response.
    Returns a tabulated response for the result from each of these sections

    Parameters:
        url (str): Default is Prison API, but can specify any API url that contains components/schema or paths
        search_phrase (str): The phrase you wish to search for

    Returns:
        tuple(pd.DataFrame, pd.DataFrame): The dataframes for the schema and path search respectively.
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
    Implements the find_context logic on a target data_frame column based on a search phrase.
    For a string contained within each row of that column, that has a divider defined as col_sep,
    it will return the substring between such dividers that the first search result is in.
    It returns this on a row by row basis as a new column, "Context".

    Parameters:
        data_frame (pd.DataFrame): The dataframe over which you wish to iteratively search
        column_name (str): The column name of the data frame you wish to search
        search_phrase (str): The phrase you wish to search for within the metadata of the row
        col_sep (str): The single character divider(s) within the string.
            These define the boundaries from within which to retrieve context.
    """

    copy_data_frame = data_frame.copy()
    copy_data_frame["Context"] = ''
    column_index = copy_data_frame.columns.get_loc(column_name)
    context_series = copy_data_frame.loc[:, column_name].apply(
        lambda x: find_context(x, search_phrase, col_sep).replace(search_phrase, f">>>{search_phrase}<<<"))
    copy_data_frame["Context"] = context_series
    return copy_data_frame

def main():
    """The main method, used to call the script. Command line argument used as the search phrase"""
    common.prepare_directory(common.SCHEMA_SEARCH_REPORT)
    common.prepare_directory(common.PATH_SEARCH_REPORT)
    is_no_search_phrase = len(sys.argv) == 1
    is_one_search_phrase = len(sys.argv) == 2
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
