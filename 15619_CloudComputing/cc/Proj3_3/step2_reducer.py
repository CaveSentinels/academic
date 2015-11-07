#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 3.3(CC-Proj-3.3) Step 2 reducer.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys


# =============================================================================
# @type: constant
# @brief: The intermediate line related.

INTERMEDIATE_LINE_DELIMITER = ','
IDX_USER_ID = 0
IDX_FRIEND_ID = 1


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

    # current_user_id stores the ID of the user that is being processed currently.
    current_user_id = "1"
    friends_user_id = []

    # Process every line read in.
    for line in file_obj :

        temp_parts = line.split('\n')

        # Split the line into different parts.
        parts = temp_parts[0].split( INTERMEDIATE_LINE_DELIMITER )

        if parts[IDX_USER_ID] == current_user_id :
            # If we are still processing the current user, then we add the
            #   friend's user ID.
            friends_user_id.append(parts[IDX_FRIEND_ID])
        else :
            # If we are now processing a new user, then we should complete
            #   all the work necessarily to be done to the previous user.
            friends_user_id.sort()

            friends = []
            for id in friends_user_id :
                friends.append(id + ',')

            print current_user_id + '\t' + ''.join(friends)

            # And then initialize the access information for the new page.
            current_user_id = parts[IDX_USER_ID]
            friends_user_id = []
            friends_user_id.append(parts[IDX_FRIEND_ID])

    # Don't forget the last user, because it is the last one, there would be
    #   no user ID switch. We must manually trigger its output.
    friends_user_id.sort()

    friends = []
    for id in friends_user_id :
        friends.append(id + ',')

    print current_user_id + '\t' + ''.join(friends)

    # Close the file if it is not stdin.
    if not ( file_obj is sys.stdin ) :
        file_obj.close()


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :

    Main( sys.argv )