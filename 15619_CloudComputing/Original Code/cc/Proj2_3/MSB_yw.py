# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.3(CC-Proj-2.3)
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
import vertx
import urllib2
import sys
import socket

import collections


# =============================================================================

class CCProj23Const ( object ) :
    CACHE_TOTAL_MAX_SIZE = 1000
    CACHE_STATIC_MAX_SIZE = 86
    CACHE_DYNAMIC_MAX_SIZE = CACHE_TOTAL_MAX_SIZE - CACHE_STATIC_MAX_SIZE
    RANGE_LOWER = -10
    RANGE_UPPER = 10
    FILE_ID_LIST_HIGH_FREQUENCY = "data_pure_id_high_freq.txt"


# =============================================================================
class LRUCache2 ( object ) :

    def __init__( self, max_dynamic_size, max_static_size ):
        self._max_dynamic_size = max_dynamic_size
        self._cache = collections.OrderedDict()

        self._cache_static_max_size = max_static_size
        self._cache_static = collections.OrderedDict()
        self._cache_static_constructed = False

    def access( self, key ) :
        # First try with the static cache
        try :
            value = self._cache_static.pop( key )
            self._cache_static[key] = value
            return value
        except KeyError :
            # Do not return. Continue to the next try...except
            pass

        # If the key is not found in static cache, try the dynamic one.
        try :
            value = self._cache.pop( key )
            self._cache[key] = value
            return value
        except KeyError :
            return None

    def update( self, key, value ) :
        try :
            self._cache.pop( key )
        except KeyError :
            if len(self._cache) >= self._max_dynamic_size :
                self._cache.popitem( last = False )
        self._cache[key] = value

    def static_cache_constructed( self ) :
        return self._cache_static_constructed

    def construct_static_cache( self, key_value_pairs ) :
        pairs_size = len( key_value_pairs )

        for i in range( 0, min( pairs_size, self._cache_static_max_size ) ) :
            pair = key_value_pairs[i]
            try :
                self._cache_static.pop( pair[0] )
            except KeyError :
                # Need to do nothing because we ensure the size would not exceed.
                pass
            self._cache_static[pair[0]] = pair[1]

        self._cache_static_constructed = True


# =============================================================================

server = vertx.create_http_server( )
database_instances = ["" for x in range( 2 )]

id_list_high_freq = list()
g_LRU_Cache2 = LRUCache2( CCProj23Const.CACHE_DYNAMIC_MAX_SIZE, CCProj23Const.CACHE_STATIC_MAX_SIZE )


# =============================================================================

def read_id_list( file_name ) :

    id_list = list()

    file_id_list = open( file_name, "r" )

    for line in file_id_list :
        parts = line.split("\t\n")
        id_list.append( parts[0] )

    file_id_list.close()

    return id_list


# =============================================================================

# Initialize the variables that store the DNS of database instances
def init( ) :
    database_instances[0] = "<INSERT FIRST DCI'S DNS HERE>"
    database_instances[1] = "<INSERT SECOND DCI'S DNS HERE>"

    global id_list_high_freq

    id_list_high_freq = read_id_list( CCProj23Const.FILE_ID_LIST_HIGH_FREQUENCY )


#Send HTTP request
def send_request( host, url ) :
    Port = 80  #Port for HTTP connection

    try :
        sock = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
    except socket.error, msg :
        sys.stderr.write( "ERROR: %s\n" % msg[1] )
        sys.exit( 1 )

    try :
        sock.connect( (host, Port) )
    except socket.error, msg :
        sys.stderr.write( "ERROR %s\n" % msg[1] )
        sys.exit( 1 )

    sock.send( "GET %s HTTP/1.0\r\nHost: %s\r\n\r\n" % (url, host) )

    data = sock.recv( 1024 )
    string = ""
    while len( data ) :
        string = string + data
        data = sock.recv( 1024 )
    sock.close( )

    return string


#Send a request to the backend datacenter
def send_request_to_datacenter( host, url ) :
    response_from_server = send_request( host, url )
    result = ""
    parsed_response = response_from_server.split( )[7 :]

    for r in parsed_response :
        result += r + " "

    return result.strip( )


#Check that the DCI are up and running
def check_backend( ) :
    DC0_response = send_request( database_instances[0], generate_path( 1 ) )
    DC1_response = send_request( database_instances[1], generate_path( 1 ) )
    if (DC0_response == "" or DC1_response == "" or
                DC0_response.split( )[1] != '200' or DC1_response.split( )[1] != '200') :
        return 0
    else :
        return 1


#Generate the path for a single targetID
def generate_path( targetID ) :
    return "/target?targetID=" + str( targetID )


#Generate the path for a range of targetIDs
#The details of 10,000 most recently targets are cached in the database instance
def generate_range_path( start_range, end_range ) :
    return "/range?start_range=" + str( start_range ) + "&end_range=" + str( end_range )


#Takes targetID as the input and returns the record for that targetID
#You need to optimize this function
def retrieve_details( targetID ) :

    if not g_LRU_Cache2.static_cache_constructed() :
        data_pairs = list()
        for _id in id_list_high_freq :
            result = send_request_to_datacenter( database_instances[0],
                                                 generate_path( _id ) )
            data_pairs.append( ( _id, result ) )
        g_LRU_Cache2.construct_static_cache( data_pairs )

    result = g_LRU_Cache2.access( targetID )

    if result is None :

        id_num = int( targetID )
        results_string = send_request_to_datacenter(
            database_instances[0],
            generate_range_path( id_num + CCProj23Const.RANGE_LOWER,
                                 id_num + CCProj23Const.RANGE_UPPER )
        )

        results = results_string.split(";")

        for i in range( 0, CCProj23Const.RANGE_UPPER - CCProj23Const.RANGE_LOWER ) :
            g_LRU_Cache2.update( key = str( id_num + CCProj23Const.RANGE_LOWER + i ),
                              value = results[i]
            )

        result = g_LRU_Cache2.access( targetID )

    return result


#Calls the retrieve details function
def process_request( targetID, req ) :
    result = retrieve_details( targetID )
    if result == "" :
        req.response.end( "No response received" )
    else :
        req.response.end( result )


init( )

#Check that the backend instances are up
if check_backend( ) == 0 :
    print "ERROR: Unable to connect to data center instances"
    sys.exit( )

#Handle the request
@server.request_handler
def handle( req ) :
    if req.path == "/target" :
        targetID = req.params['targetID']
        process_request( targetID, req )
    if req.path == "/" :
        targetID = 1
        process_request( targetID, req )


server.listen( 80 )
