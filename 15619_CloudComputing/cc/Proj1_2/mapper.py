#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) mapper.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys
import os


# =============================================================================
# @type: constant
# @brief: Log file-related constants

LOG_FILE_NAME_DELIMITER = '-'    # The delimiter in the log file name.
LOG_FILE_NAME_ENV_VAR = "mapreduce_map_input_file"  # The environment variable name for log file name.


# =============================================================================
# @type: constant
# @brief: Log line-related constants

LOG_LINE_FIELD_COUNT = 4
LOG_LINE_NAME = 0        # project name
LOG_LINE_TITLE = 1       # page title
LOG_LINE_ACCESS_NUM = 2      # number of access
LOG_LINE_DATA_RET = 3    # total data returned in bytes
LOG_LINE_LANG_ENGLISH = "en"

LOG_SPECIAL_TITLE_PREFIX = [
    "Media:",
    "Special:",
    "Talk:",
    "User:",
    "User_talk:",
    "Project:",
    "Project_talk:",
    "File:",
    "File_talk:",
    "MediaWiki:",
    "MediaWiki_talk:",
    "Template:",
    "Template_talk:",
    "Help:",
    "Help_talk:",
    "Category:",
    "Category_talk:",
    "Portal:",
    "Wikipedia:",
    "Wikipedia_talk:"
]

LOG_TITLE_IMAGE_SUFFIX = [
    ".jpg",
    ".gif",
    ".png",
    ".JPG",
    ".GIF",
    ".PNG",
    ".txt",
    ".ico"
]

LOG_TITLE_BOILERPLATE_ARTICLE = [
    "404_error/",
    "Main_Page",
    "Hypertext_Transfer_Protocol",
    "Search"
]


# =============================================================================
# @type: constant
# @brief: Log line-related constants used in the final output file.

LOG_LINE_OUTPUT_FIELDS = 2      # In the output, each line has 2 fields
LOG_LINE_OUTPUT_TITLE = 0       # In the output, article title is the 1st field
LOG_LINE_OUTPUT_ACCESS_NUM = 1      # In the output, access number is the 2nd field

LOG_LINE_OUTPUT_DELIMITER = '\t' # Field separator in the output


# =============================================================================
# @type: variable
# @brief: The global variables

g_LogSpecialTitlePrefix = set( LOG_SPECIAL_TITLE_PREFIX )
g_LogTitleImageSuffix = set( LOG_TITLE_IMAGE_SUFFIX )
g_LogTitleBoilerplateArticle = set( LOG_TITLE_BOILERPLATE_ARTICLE )

g_LogSpecialTitlePrefix_ShortestLen = sys.maxint
g_LogTitleImageSuffix_ShortestLen = sys.maxint
g_LogTitleBoilerplateArticle_ShortestLen = sys.maxint


# =============================================================================
# @type: function
# @brief: Construct the global variables.
# @param: N/A
# @return: N/A

def ConstructGlobalVariables() :
    global g_LogSpecialTitlePrefix
    global g_LogTitleImageSuffix
    global g_LogTitleBoilerplateArticle
    global g_LogSpecialTitlePrefix_ShortestLen
    global g_LogTitleImageSuffix_ShortestLen
    global g_LogTitleBoilerplateArticle_ShortestLen

    # Figure out the length of the shortest one in LOG_SPECIAL_TITLE_PREFIX
    for prefix in LOG_SPECIAL_TITLE_PREFIX :
        curr_len = len( prefix )
        if curr_len < g_LogSpecialTitlePrefix_ShortestLen :
            g_LogSpecialTitlePrefix_ShortestLen = curr_len

    # Figure out the length of the shortest one in LOG_TITLE_IMAGE_SUFFIX
    for suffix in LOG_TITLE_IMAGE_SUFFIX :
        curr_len = len( suffix )
        if curr_len < g_LogTitleImageSuffix_ShortestLen :
            g_LogTitleImageSuffix_ShortestLen = curr_len

    # Figure out the length of the shortest one in LOG_TITLE_BOILERPLATE_ARTICLE
    if len( LOG_TITLE_BOILERPLATE_ARTICLE ) > 0 :
        g_LogTitleBoilerplateArticle_ShortestLen = len( LOG_TITLE_BOILERPLATE_ARTICLE[0] )
        for article in LOG_TITLE_BOILERPLATE_ARTICLE :
            curr_len = len( article )
            if curr_len < g_LogTitleBoilerplateArticle_ShortestLen :
                g_LogTitleBoilerplateArticle_ShortestLen = curr_len


# =============================================================================
# @type: function
# @brief: Determine if line should be removed or retained.
# @param: [in] parts: The list of all parts in the current line
# @return: bool: whether line should be retained
#   - True: line should be retained
#   - False: line should be removed

def Retainable( parts ):
    global g_LogSpecialTitlePrefix
    global g_LogTitleImageSuffix
    global g_LogTitleBoilerplateArticle
    global g_LogSpecialTitlePrefix_ShortestLen
    global g_LogTitleImageSuffix_ShortestLen
    global g_LogTitleBoilerplateArticle_ShortestLen

    # If the log line is in wrong format, remove it.
    if len( parts ) != LOG_LINE_FIELD_COUNT :
        return False

    # To make the check faster, we get the title's length first.
    title_len = len( parts[LOG_LINE_TITLE] )

    # All the parts should have something.
    if len( parts[LOG_LINE_NAME] ) == 0 or \
       title_len == 0 or \
       len( parts[LOG_LINE_ACCESS_NUM] ) == 0 or \
       len( parts[LOG_LINE_DATA_RET] ) == 0 :
        return False

    # Rule #1: If the page is not English Wikipedia, remove it!
    if parts[LOG_LINE_NAME] != LOG_LINE_LANG_ENGLISH :
        return False

    # Rule #3: If the title starts with an lowercase English characters,
    #   and it is not an non-English title, then remove it!
    # NOTE: We notice that the special prefix always starts with an upper
    #   case letter, so we will check this rule first. This rule will
    #   filter any title starting with non-upper-case letter so we will
    #   not compare them with the special titles, which could save time.
    if parts[LOG_LINE_TITLE][0].islower() :
        return False

    # Rule #2: If the title starts with a special prefix, remove it!
    # NOTE: If the title_len is not longer than the shortest special prefix,
    #   we can skip this step.
    if title_len >= g_LogSpecialTitlePrefix_ShortestLen :
        first_colon = parts[LOG_LINE_TITLE].find(':')
        if first_colon != -1 :
            prefix = parts[LOG_LINE_TITLE][0:first_colon+1] # "+1" would not cause exception
            if prefix in g_LogSpecialTitlePrefix :
                return False

    # Rule #4: If the title indicates an image, remove it!
    # NOTE: If the title_len is not longer than the shortest image
    #   suffix, we can skip this step.
    if title_len >= g_LogTitleImageSuffix_ShortestLen :
        last_dot = parts[LOG_LINE_TITLE].rfind('.')
        if last_dot != -1 :
            suffix = parts[LOG_LINE_TITLE][last_dot:] # do not +1 to include the '.'
            if suffix in g_LogTitleImageSuffix :
                return False

    # Rule #5: If the title indicates a boilerplate article, remove it!
    # NOTE: If the title_len is not longer than the shortest boilerplate
    #   title, we can skip this step.
    if title_len >= g_LogTitleBoilerplateArticle_ShortestLen :
        if parts[LOG_LINE_TITLE] in g_LogTitleBoilerplateArticle :
            return False

    # If the line survives from all the above-mentioned rules, then it's a
    #   good one to keep.

    return True


# =============================================================================
# @type: function
# @brief: Get the date part in the Wikimedia log file name.
# @param: [in] file_name: The name of the file whose data are being processed
#   currently.
# @return: string: The date part in file_name, in the format of "yyyymmdd".
# @note: The file_name is in the format of "pagecounts-yyyymmdd-hhmmss".
#   However, there could be prefix and suffix around it.
#   The assumption is that any suffix does not contain additional '-'.

def GetDatePartInWikimediaLogFileName( file_name ) :

    # Find the index for the last '-'.
    # "pagecounts-yyyymmdd-hhmmss"
    #                     ^
    last_dash_pos = file_name.rfind( LOG_FILE_NAME_DELIMITER )

    # Substring from the first char to last_dash_pos-1.
    # So now we have "pagecounts-yyyymmdd".
    temp = file_name[:last_dash_pos]

    # Find the index for the last '-'.
    # "pagecounts-yyyymmdd"
    #            ^
    last_dash_pos = temp.rfind( LOG_FILE_NAME_DELIMITER )

    # We should add one to the last_dash_pos in order to start from 'yyyy'.
    return temp[last_dash_pos + 1 :]


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 mapper.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: There could be two kinds of arguments:
#   1). One argument, which is this program itself. In this case, the program
#       will receive input from the stdin and the file name of the input data
#       is stored in the environment variable LOG_FILE_NAME_ENV_VAR.
#   2). Two arguments, which are this program plus the input data file name.

def Main( args ):

    # Determine if the user specifies an input file explicitly.
    #   If an input file is provided, then use the input file; otherwise we
    #   receive data from the standard input.
    file_name = None
    file_obj = None
    if len( args ) > 1 :
        # An input file is provided. We should use the data in this file.
        file_name = args[1]
        file_obj = open( file_name, 'r' )
    else:
        # No input file is provided. We should receive the data from the stdin.
        #   In this case, the file name is in the environment variable.
        file_name = os.environ[LOG_FILE_NAME_ENV_VAR]
        file_obj = sys.stdin

    # Get the date of the current file name.
    date_part = GetDatePartInWikimediaLogFileName( file_name )

    # Process every line read in.
    for line in file_obj :
        # Split the line into parts using white space as separator.
        parts = line.split()

        # Check if the line should be retained or not
        if Retainable( parts ) :
            out_line = parts[LOG_LINE_TITLE] + \
                       LOG_LINE_OUTPUT_DELIMITER + \
                       date_part + \
                       LOG_LINE_OUTPUT_DELIMITER + \
                       parts[LOG_LINE_ACCESS_NUM]

            print out_line # No need to append a '\n' because print will do so.

    # Close the file if it is not stdin.
    if not ( file_obj is sys.stdin ):
        file_obj.close()


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :

    ConstructGlobalVariables()

    Main( sys.argv )