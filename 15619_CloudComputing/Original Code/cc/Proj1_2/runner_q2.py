#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) runner q2.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.2 runner q2.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: How many lines emerged in your output files?

def Main( args ):

    # Get the parameters
    # [0] is always the name of this script.
    output_file_name = args[1]

    # Variable
    line_count = 0

    # Find the lines for first and second.
    output_file = open( output_file_name, 'r' )

    for line in output_file :
        line_count += 1

    output_file.close()

    return line_count


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :
    print Main( sys.argv )