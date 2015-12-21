# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.3(CC-Proj-2.3)
# @author: robin
# @email: yaobinw@andrew.cmu.edu
# @note: This is the caching algorithm testing program.


# =============================================================================
# @type: directive
# @brief: Imports.

import time

import CCLib.CommonDefs
import CCProj23Const
import LRUCaches


# =============================================================================

g_Statistical_Data = LRUCaches.StatisticalData()


# =============================================================================
# The global LRU cache instance

g_LRU_Cache = LRUCaches.LRUCache( CCProj23Const.CACHE_TOTAL_MAX_SIZE )


# =============================================================================
# @type: function
# @brief: Print the given string to stdout as tracing log.
# @param: [in] msg: The string message to print to stdout.
# @return: N/A
# @note: Later we should use the provided logging utility in Python.

def _log_stdout( msg ) :
    print msg


# =============================================================================
# @type: function

def sim_send_request_to_datacenter( id_start, id_end ) :
    results = list()
    id_num = id_start
    while id_num <= id_end :
        results.append( str( id_num ) + ";" )
        id_num += 1

    return str().join(results)


# =============================================================================
# @type: function
# @brief: The retrieve_details() simulation function.

def sim_retrieve_details( target_id ) :

    result = g_LRU_Cache.access( target_id )

    if result is None :
        id_num = int( target_id )
        results_string = sim_send_request_to_datacenter(
            id_num + CCProj23Const.RANGE_LOWER,
            id_num + CCProj23Const.RANGE_UPPER
        )

        results = results_string.split(";")

        kv_pairs = list()
        for i in range( 0, CCProj23Const.RANGE_UPPER - CCProj23Const.RANGE_LOWER ) :
            kv_pairs.append( ( str( id_num + CCProj23Const.RANGE_LOWER + i ), results[i] ) )

        g_LRU_Cache.update( key_value_pairs = kv_pairs,
                            k = CCProj23Const.RANGE_UPPER - CCProj23Const.RANGE_LOWER )

        g_Statistical_Data.miss()
    else :
        g_Statistical_Data.hit()

    return result


# =============================================================================
# @type: function
# @brief: Read all the test IDs into a list.

def read_test_id_list( file_name ) :

    id_list = list()

    file_id_list = open( file_name, CCLib.CommonDefs.FILE_OPEN_MODE_READ_ONLY )

    for line in file_id_list :
        parts = line.split("\t\n")
        id_list.append( parts[0] )

    file_id_list.close()

    return id_list


# =============================================================================
# @type: function
# @brief: The main work flow.
# @param: [in] args: The command line arguments.
# @return: N/A

def main() :

    id_list = read_test_id_list( CCProj23Const.FILE_TARGET_ID_LIST )

    g_Statistical_Data.start_test()

    test_num = CCProj23Const.MAXIMUM_TEST_NUMBER if ( CCProj23Const.MAXIMUM_TEST_NUMBER != -1 ) else len( id_list )

    for i in range( 0, test_num ) :
        sim_retrieve_details( id_list[i] )
        _log_stdout( "id_list[" + str(i) + "] = " + id_list[i])
        _log_stdout( g_LRU_Cache )

    g_Statistical_Data.end_test()

    _log_stdout( str( g_Statistical_Data ) )


# =============================================================================
# @type: script
# @brief: The entry point of the script.

if __name__ == "__main__" :
    main()
