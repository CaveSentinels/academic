#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 3.3(CC-Proj-3.3) Step 3 processor.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: The intermediate line related.

LINE_DELIMITER = ','
IDX_USER_ID = 0
IDX_TIME = 1
IDX_USER_AVATAR = 2


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 reducer.
# @param: [in] args: The command line arguments.
# @return: N/A

def Main( args ):

    # Determine if the user specifies an input file explicitly.
    #   If an input file is provided, then use the input file; otherwise we
    #   receive data from the standard input.
    if len( args ) > 1 :
        # An input file is provided. We should use the data in this file.
        file_obj = open( args[1], 'r' )
    else:
        # No input file is provided. We should receive the data from the stdin.
        file_obj = sys.stdin

    # Process every line read in.
    for line in file_obj :

        # Remove the '\n' at the end of line.
        temp_parts = line.split('\n')

        # Split the line into different parts.
        parts = temp_parts[0].split( LINE_DELIMITER )

        # Transform the line to the desired format.
        print "url\x03{\"s\":\"" + parts[IDX_USER_AVATAR] + "\"}\x02userid\x03{\"n\":\"" + parts[IDX_USER_ID] + "\"}\x02time\x03{\"s\":\"" + parts[IDX_TIME] + "\"}"

    # Close the file if it is not stdin.
    if not ( file_obj is sys.stdin ) :
        file_obj.close()


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :

    Main( sys.argv )