#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q9.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q9 in runner.sh

LINE_DELIMITER = '\t'
IDX_TOTAL_VIEW_NUM = 0
IDX_PAGE_TITLE = 1
FIRST_DAY_OFFSET = 2
DAY_DATA_DELIMITER = ':'
IDX_DATE = 0
IDX_DAY_ACCESS_NUM = 1  # "yyyymmdd:num"


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q9.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: # Rank the cities in the file q9 based on their total wikipedia page
#   views for November 2014 (In descending order of page views, with the highest
#   one first): London, Beijing, New_York_City, Bangalore, Tokyo
#   Ensure that you print the answers comma separated (As shown in the above line)

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]

    # Variables
    article_most_popular = str()
    most_access_num = 0

    # Get the line for the article.
    output_file = open( output_file_name, 'r' )

    for line in output_file :
        parts = line.split( LINE_DELIMITER )

        # Find the Nov. 1st view number
        view_data_november1 = parts[FIRST_DAY_OFFSET]
        day_parts = view_data_november1.split( DAY_DATA_DELIMITER )
        if len( day_parts ) != 2 :  # In case of '\n' line.
            continue

        # If the first day view number is not zero, then skip it.
        if int( day_parts[1] ) != 0 :
            continue

        # If the first day view number is zero, then get the total view number.
        total_view_num = int( parts[IDX_TOTAL_VIEW_NUM] )

        if total_view_num > most_access_num :
            # We find an article with more view number. Update the data.
            most_access_num = total_view_num
            article_most_popular = parts[IDX_PAGE_TITLE]

    output_file.close()

    return article_most_popular


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    result = Main( sys.argv )
    print result