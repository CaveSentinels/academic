#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q6.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q6 in runner.sh

LINE_DELIMITER = '\t'
IDX_PAGE_TITLE = 1
FIRST_DAY_OFFSET = 2
DAY_DATA_DELIMITER = ':'
IDX_DAY_ACCESS_NUM = 1  # "yyyymmdd:num"


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q6.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: Rank the Movie titles in the file q6 based on their single-day maximum
#   wikipedia page views (In descending order of page views, with the highest one
#   first):
#   Begin_Again_(film), Annabelle_(film), Predestination_(film),
#   The_Fault_in_Our_Stars_(film), The_Equalizer_(film)
#   Ensure that you print the answers comma separated (As shown in the above line)

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]
    q6_file_name = args[2]

    # Get all the movies we need to handle.
    movie_max_view_dict = dict()
    movie_count = 0

    q6_file = open( q6_file_name, 'r' )

    for line in q6_file :
        # In the q6 file, a line ends up with '\n', so we can split the line
        #   to two parts: parts[0] is the movie title.
        parts = line.split( '\n' )
        if len( parts[0] ) > 0 :
            movie_max_view_dict[ parts[0] ] = 0 # Initially, the max view is 0.
            movie_count += 1

    q6_file.close()

    # Calculate the maximum single-day page view of each movie in q6.
    already_found = 0
    output_file = open( output_file_name, 'r' )

    for line in output_file :
        parts = line.split( LINE_DELIMITER )

        # If the current line is not any of the movies, skip it.
        if not movie_max_view_dict.has_key( parts[IDX_PAGE_TITLE] ) :
            continue

        # Otherwise, the current line is one of the movies that we care about.
        parts_len = len( parts )
        day_max_view = -1
        for i in range( FIRST_DAY_OFFSET, parts_len ) :
            day_parts = parts[i].split( DAY_DATA_DELIMITER )
            if len( day_parts ) != 2 :  # In case of '\n' line.
                continue

            day_view = int( day_parts[IDX_DAY_ACCESS_NUM] )
            if day_view > day_max_view :
                day_max_view = day_view

        # For now, we just record the parts of each line.
        movie_max_view_dict[ parts[IDX_PAGE_TITLE] ] = day_max_view

        # If we have found the data for all the movies, we can break.
        already_found += 1
        if already_found == movie_count :
            break

    output_file.close()

    # Now we sort the movie day view data according to the max day view.
    movie_day_data = movie_max_view_dict.items()
    movie_day_data.sort( key = lambda x:x[1], reverse = True )

    # Now the movie_day_data should be in the order from more views to less.
    # Construct the result string.
    result = str()
    for i in range( 0, movie_count - 1 ) :
        result += movie_day_data[i][0] + ", "
    result += movie_day_data[movie_count-1][0]

    return result


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    result = Main( sys.argv )
    print result