#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q8.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q8 in runner.sh

LINE_DELIMITER = '\t'
IDX_PAGE_TITLE = 1
FIRST_DAY_OFFSET = 2
DAY_DATA_DELIMITER = ':'
IDX_DATE = 0
IDX_DAY_ACCESS_NUM = 1  # "yyyymmdd:num"


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q8.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: # Rank the cities in the file q8 based on their total wikipedia page
#   views for November 2014 (In descending order of page views, with the highest
#   one first): London, Beijing, New_York_City, Bangalore, Tokyo
#   Ensure that you print the answers comma separated (As shown in the above line)

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]
    article_name = args[2]

    # Variables
    day = str()
    day_max_view = 0

    # Get the line for the article.
    output_file = open( output_file_name, 'r' )

    for line in output_file :
        parts = line.split( LINE_DELIMITER )

        if parts[IDX_PAGE_TITLE] == article_name :
            parts_len = len( parts )

            for i in range( FIRST_DAY_OFFSET, parts_len ) :
                day_parts = parts[i].split( DAY_DATA_DELIMITER )
                if len( day_parts ) != 2 :  # In case of '\n' line.
                    continue

                day_view = int( day_parts[IDX_DAY_ACCESS_NUM] )
                if day_view > day_max_view :
                    day = day_parts[IDX_DATE]
                    day_max_view = day_view

    output_file.close()

    return day


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    result = Main( sys.argv )
    print result