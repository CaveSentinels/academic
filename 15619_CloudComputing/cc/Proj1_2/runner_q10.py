#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q10.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: Used for answering q10 in runner.sh

LINE_DELIMITER = '\t'
IDX_TOTAL_VIEW_NUM = 0
IDX_PAGE_TITLE = 1
FIRST_DAY_OFFSET = 2
DAY_DATA_DELIMITER = ':'
IDX_DATE = 0
IDX_DAY_ACCESS_NUM = 1  # "yyyymmdd:num"


# =============================================================================
# @type: function
# @brief: Parse a text line in output and transform it into ( title, list ).
# @param: [in] line: A text line in file 'output'.
# @return: tuple: ( str, list )
#   str: The title of the page.
#   list: The access numbers of every day, in the order of date.

def ParsePageDayViews( line ) :

    parts = line.split( LINE_DELIMITER )

    parts_len = len( parts )
    title = parts[IDX_PAGE_TITLE]
    day_views = list()

    for i in range( FIRST_DAY_OFFSET, parts_len ) :
        day_parts = parts[i].split( DAY_DATA_DELIMITER )
        if len( day_parts ) != 2 :  # In case of '\n' line.
            continue

        day_views.append( int( day_parts[IDX_DAY_ACCESS_NUM] ) )

    return ( title, day_views )


# =============================================================================
# @type: function
# @brief: Calculate the maximum strictly increasing sequence of the daily view.
# @param: [in] page_day_views: Tuple, see function ParsePageDayViews()'s return.
# @return: tuple: ( str, int )
#   str: The title of the page.
#   int: The maximum strictly increasing sequence of the daily view.

def CalcPageStrictlyIncreasingNum( page_day_views ) :

    title = page_day_views[0]
    day_views = page_day_views[1]
    day_count = len( day_views )

    max_strictly_inc_seq_num = 0
    strictly_inc_seq_num = 0
    for i in range( 0, day_count-1 ) :
        if day_views[i] < day_views[i+1] :
            strictly_inc_seq_num += 1
        else :
            if strictly_inc_seq_num > max_strictly_inc_seq_num :
                max_strictly_inc_seq_num = strictly_inc_seq_num
            strictly_inc_seq_num = 0    # reset to zero

    if strictly_inc_seq_num > max_strictly_inc_seq_num :
        max_strictly_inc_seq_num = strictly_inc_seq_num

    return ( title, max_strictly_inc_seq_num )


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q10.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: Find out the number of articles with longest number of strictly 
#   increasing sequence of views.
#   Example: If 27 articles have strictly increasing page views everyday for 
#   5 days (which is the global maximum), then your script should find these 
#   articles from the output file and return 27.

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]

    # Variables
    strictly_increasing_seq_dict = dict()

    output_file = open( output_file_name, 'r' )

    for line in output_file :

        # Parse the line and transform it into ( title, list( day view ) )
        page_day_views = ParsePageDayViews( line )

        # Calculate the maximum strictly increasing sequence number.
        page_strictly_increasing_num = CalcPageStrictlyIncreasingNum( page_day_views )

        # Put it in the dictionary.
        title = page_strictly_increasing_num[0]
        strictly_inc_num = page_strictly_increasing_num[1]
        if not strictly_increasing_seq_dict.has_key( strictly_inc_num ) :
            strictly_increasing_seq_dict[strictly_inc_num] = list()

        strictly_increasing_seq_dict[strictly_inc_num].append( title )

    output_file.close()

    # Sort according to the maximum number of strictly increasing sequence.
    strictly_increasing_seq_list = strictly_increasing_seq_dict.items()
    strictly_increasing_seq_list.sort( reverse = True )

    # strictly_increasing_seq_list[0] is the pair that contains the maximum
    # number of strictly increasing sequences and all the corresponding
    # page titles.
    # Therefore, strictly_increasing_seq_list[0][1] is the list of all the
    # page titles.
    return len( strictly_increasing_seq_list[0][1] )


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    result = Main( sys.argv )
    print result