# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.1(CC-Proj-1.1)
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Imports for CC-Proj-1.1.

import CCLib.WikiLog


# =============================================================================
# @type: constant
# @brief: File-related constants

FILE_NAME_SOURCE = 'pagecounts-20141101-000000'
FILE_NAME_TEMP = FILE_NAME_SOURCE + ".temporary.txt"
FILE_NAME_DESTINATION = "output"


# =============================================================================
# @type: function
# @brief: The main work flow of CC-Proj-1.1.
# @param: N/A
# @return: N/A

def Main():
    CCLib.WikiLog.FilterFile( FILE_NAME_SOURCE, FILE_NAME_TEMP, FILE_NAME_DESTINATION )


# =============================================================================
# @type: script
# @brief: The entry point of the script.

Main()
