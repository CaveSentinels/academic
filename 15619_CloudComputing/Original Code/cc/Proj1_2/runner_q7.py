#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q7.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q7 in runner.sh

LINE_DELIMITER = '\t'
IDX_PAGE_TITLE = 1
FIRST_DAY_OFFSET = 2
DAY_DATA_DELIMITER = ':'
IDX_DAY_ACCESS_NUM = 1  # "yyyymmdd:num"


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q7.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: # Rank the cities in the file q7 based on their total wikipedia page
#   views for November 2014 (In descending order of page views, with the highest
#   one first): London, Beijing, New_York_City, Bangalore, Tokyo
#   Ensure that you print the answers comma separated (As shown in the above line)

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]
    q7_file_name = args[2]

    # Get all the cities we need to handle.
    city_total_view_dict = dict()
    city_count = 0

    q7_file = open( q7_file_name, 'r' )

    for line in q7_file :
        # In the q7 file, a line ends up with '\n', so we can split the line
        #   to two parts: parts[0] is the city name.
        parts = line.split( '\n' )
        if len( parts[0] ) > 0 :
            city_total_view_dict[ parts[0] ] = 0 # Initially, the total view is 0.
            city_count += 1

    q7_file.close()

    # Get the total page view of each city in q7.
    already_found = 0
    output_file = open( output_file_name, 'r' )

    for line in output_file :
        parts = line.split( LINE_DELIMITER )

        # If the current line is not any of the city, skip it.
        if not city_total_view_dict.has_key( parts[IDX_PAGE_TITLE] ) :
            continue

        # Otherwise, the current line is one of the cities that we care about.
        city_total_view_dict[ parts[IDX_PAGE_TITLE] ] = int( parts[0] )

        # If we have found the data for all the cities, we can break.
        already_found += 1
        if already_found == city_count :
            break

    output_file.close()

    # Now we sort the city total view data in a descendant order.
    city_data = city_total_view_dict.items()
    city_data.sort( key = lambda x:x[1], reverse = True )

    # Now the city_data should be in the order from more views to less.
    # Construct the result string.
    result = str()
    for i in range( 0, city_count - 1 ) :
        result += city_data[i][0] + ", "
    result += city_data[city_count-1][0]

    return result


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    result = Main( sys.argv )
    print result