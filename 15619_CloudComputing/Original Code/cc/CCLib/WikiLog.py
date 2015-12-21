# =============================================================================
# @type: file
# @brief: The classes and functions for processing the Wikimedia log files.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Imports for this module.

import CCLib.CommonDefs


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
# @type: function
# @brief: Determine if line should be removed or retained.
# @param: [in] parts: The list of all parts in the current line
# @return: bool: whether line should be retained
#   - True: line should be retained
#   - False: line should be removed

def Retainable( parts ):

    # If the log line is in wrong format, remove it.
    if len( parts ) != LOG_LINE_FIELD_COUNT :
        return False

    # Rule #1: If the page is not English Wikipedia, remove it!
    if parts[LOG_LINE_NAME] != LOG_LINE_LANG_ENGLISH :
        return False

    # Rule #2: If the title starts with a special prefix, remove it!
    for prefix in LOG_SPECIAL_TITLE_PREFIX :
        if parts[LOG_LINE_TITLE].startswith( prefix ) :
            return False

    # Rule #3: If the title starts with an lowercase English characters,
    #   and it is not an non-English title, then remove it!
    if parts[LOG_LINE_TITLE][0].islower() :
        return False

    # Rule #4: If the title indicates an image, remove it!
    for suffix in LOG_TITLE_IMAGE_SUFFIX :
        if parts[LOG_LINE_TITLE].endswith( suffix ) :
            return False

    # Rule #5: If the title indicates a boilerplate article, remove it!
    for boilerplate in LOG_TITLE_BOILERPLATE_ARTICLE :
        if parts[LOG_LINE_TITLE] == boilerplate :
            return False

    # If the line survives from all the above-mentioned rules, then it's a
    #   good one to keep.

    return True


# =============================================================================
# @type: function
# @brief: Transform a log object into a string.
# @param: [in] log_obj: A tuple that represents a log object.
#   log_obj[0]: The article title.
#   log_obj[1]: The access number.
# @return: string: The serialized log_obj.
# @note: The returned string does not have a '\n' at the end.

def Serialize( log_obj ) :
    return log_obj[LOG_LINE_OUTPUT_TITLE] + \
           LOG_LINE_OUTPUT_DELIMITER + \
           str( log_obj[LOG_LINE_OUTPUT_ACCESS_NUM] )


# =============================================================================
# @type: function
# @brief: Transform a log string into an object.
# @param: [in] log_line: A log string.
# @return: tuple: The log object.
#   [0]: The article title.
#   [1]: The access number.

def Deserialize( log_line ) :
    parts = log_line.split( LOG_LINE_OUTPUT_DELIMITER )
    return parts[LOG_LINE_OUTPUT_TITLE], int( parts[LOG_LINE_OUTPUT_ACCESS_NUM] )


# =============================================================================
# @type: function
# @brief: Filter the log entries in source, and store the results in
#   destination.
# @param: [in] source: The file which has the original logs.
# @param: [in] temp: The file which stores the intermediate results.
# @param: [in] destination: The file which stores the filtered result logs.
# @return: N/A

def FilterFile( source, temp, destination ):

    # Step 1: Filter the content of source. The retained lines are written
    #   into temp.

    file_source = open( source, CCLib.CommonDefs.FILE_OPEN_MODE_READ_ONLY )
    file_temp = open( temp, CCLib.CommonDefs.FILE_OPEN_MODE_WRITE_ONLY )

    # Iterate all the lines in the source file.
    for line in file_source :
        # Split the line into parts using white space as separator.
        parts = line.split()

        # Check if the line should be retained or not
        if Retainable( parts ) :
            file_temp.write( parts[LOG_LINE_TITLE] +
                             LOG_LINE_OUTPUT_DELIMITER +
                             parts[LOG_LINE_ACCESS_NUM] +
                             '\n'
            )

    file_temp.close()
    file_source.close()

    # Step 2: Sort the content in temp.

    file_temp = open( temp, CCLib.CommonDefs.FILE_OPEN_MODE_READ_ONLY )

    # Read all the lines and deserialize them into objects for sorting convenience.
    log_objs = []
    for line in file_temp :
        log_obj = Deserialize( line )
        log_objs.append( log_obj )

    file_temp.close()

    # Now we have a list of log objects. Sort them in descending order so the
    #   most frequently searched topic is at the top.
    log_objs.sort( key = lambda tup:tup[LOG_LINE_OUTPUT_ACCESS_NUM], reverse = True )

    # Step 3: Write the sorted logs back to the destination file.

    file_dest = open( destination, CCLib.CommonDefs.FILE_OPEN_MODE_WRITE_ONLY )

    for obj in log_objs :
        line = Serialize( obj )
        file_dest.write( line + '\n' )

    file_dest.close()
