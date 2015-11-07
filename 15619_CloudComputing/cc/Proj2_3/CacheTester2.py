# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.3(CC-Proj-2.3)
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directives
# @brief: All the imports of this program

import CCLib.CommonDefs
import CCProj23Const
import LRUCaches


# =============================================================================

g_Statistical_Data = LRUCaches.StatisticalData()


# =============================================================================

id_list_high_freq = list()

g_LRU_Cache2 = LRUCaches.LRUCache2( CCProj23Const.CACHE_DYNAMIC_MAX_SIZE,
                                    CCProj23Const.CACHE_STATIC_MAX_SIZE
)


# =============================================================================
# @type: function

def sim_send_request_to_datacenter( target_id ) :
    return str( target_id )


def sim_send_range_request_to_datacenter( id_start, id_end ) :
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

    if not g_LRU_Cache2.static_cache_constructed() :
        data_pairs = list()
        for _id in id_list_high_freq :
            result = sim_send_request_to_datacenter( _id )
            data_pairs.append( ( _id, result ) )
        g_LRU_Cache2.construct_static_cache( data_pairs )

    result = g_LRU_Cache2.access( target_id )

    if result is None :

        id_num = int( target_id )
        results_string = sim_send_range_request_to_datacenter(
            id_num + CCProj23Const.RANGE_LOWER,
            id_num + CCProj23Const.RANGE_UPPER
        )

        results = results_string.split(";")

        for i in range( 0, CCProj23Const.RANGE_UPPER - CCProj23Const.RANGE_LOWER ) :
            g_LRU_Cache2.update( key = str( id_num + CCProj23Const.RANGE_LOWER + i ),
                              value = results[i]
            )

        result = g_LRU_Cache2.access( target_id )

        g_Statistical_Data.miss()
    else :
        g_Statistical_Data.hit()

    return result


# =============================================================================
# @type: function
# @brief: Read all the test IDs into a list.

def read_id_list( file_name ) :

    id_list = list()

    file_id_list = open( file_name, CCLib.CommonDefs.FILE_OPEN_MODE_READ_ONLY )

    for line in file_id_list :
        parts = line.split("\t\n")
        id_list.append( parts[0] )

    file_id_list.close()

    return id_list


# =============================================================================

def init() :
    global id_list_high_freq

    id_list_high_freq = read_id_list( CCProj23Const.FILE_ID_LIST_HIGH_FREQUENCY )


# =============================================================================

def main() :

    g_Statistical_Data.start_test()

    id_list = read_id_list( CCProj23Const.FILE_TARGET_ID_LIST )

    for i in range( 0, len( id_list ) ) :
        sim_retrieve_details( id_list[i] )

    g_Statistical_Data.end_test()

    print g_Statistical_Data


# =============================================================================
# @type: script
# @brief: The entry point of the script.

if __name__ == "__main__" :
    init()
    main()