#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q5.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q5 in runner.sh

LINE_DELIMITER = '\t'
IDX_PAGE_TITLE = 1
FIRST_DAY_OFFSET = 2
DAY_DATA_DELIMITER = ':'
IDX_DAY_ACCESS_NUM = 1  # "yyyymmdd:num"


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q5.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: For how many days over the month was the page titled $(first) more
#   popular than the page titled $(second) ?

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]
    first = args[2]
    second = args[3]

    # Variables
    first_parts = list()
    second_parts = list()
    more_popular_days = 0

    # Find the lines for first and second.
    of = open( output_file_name, 'r' )

    first_found = False
    second_found = False
    for line in of :
        parts = line.split( LINE_DELIMITER )

        if parts[IDX_PAGE_TITLE] == first :
            first_parts = parts
            first_found = True
        elif parts[IDX_PAGE_TITLE] == second :
            second_parts = parts
            second_found = True

        if first_found and second_found :
            break

    of.close()

    # Count how many parts there are. Use the minimum of the two.
    first_parts_num = len( first_parts )
    second_parts_num = len( second_parts )
    min_parts_num = min( first_parts_num, second_parts_num )

    # Iterate through each part
    for i in range( FIRST_DAY_OFFSET, min_parts_num ) :
        day_data_first = first_parts[i]
        day_data_second = second_parts[i]

        day_data_first_parts = day_data_first.split(DAY_DATA_DELIMITER)
        day_data_second_parts = day_data_second.split(DAY_DATA_DELIMITER)

        if len( day_data_first_parts ) != 2 or len( day_data_second_parts ) != 2 :
            continue

        day_access_first = int( day_data_first_parts[IDX_DAY_ACCESS_NUM] )
        day_access_second = int( day_data_second_parts[IDX_DAY_ACCESS_NUM] )

        if day_access_first > day_access_second :   # More popular should mean ">"
            more_popular_days += 1

    return more_popular_days


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    days = Main( sys.argv )
    print days