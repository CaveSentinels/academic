#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q3 & q4.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q3 & q4 in runner.sh

LINE_DELIMITER = '\t'
IDX_TOTAL_ACCESS_NUM = 0
IDX_PAGE_TITLE = 1


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q3 & q4.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: How many lines emerged in your output files?

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]
    field = args[2]

    # Variables
    max_total_access_num = 0
    title_most_popular = str()

    # Find the lines for first and second.
    output_file = open( output_file_name, 'r' )

    for line in output_file :
        parts = line.split( LINE_DELIMITER )
        total_access_num = int( parts[IDX_TOTAL_ACCESS_NUM] )
        title = parts[IDX_PAGE_TITLE]
        if total_access_num > max_total_access_num :
            max_total_access_num = total_access_num
            title_most_popular = title

    output_file.close()

    result = str()
    if field == "title" :
        result = title_most_popular
    elif field == "total_view" :
        result = str( max_total_access_num )
    else :
        result = str()

    return result


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    print Main( sys.argv )