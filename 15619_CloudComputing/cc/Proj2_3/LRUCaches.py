# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.3(CC-Proj-2.3)
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================

import ordereddict
import datetime


# =============================================================================
# @type: class
# @brief: A linked list node.

class LLNode ( object ) :

    def __init__( self ) :
        self.key = None
        self.value = None
        self.prev = None
        self.next = None


# =============================================================================
# @type: class
# @brief: The LRU implementation for CC-Proj-2.3

class LRUCache ( object ) :

    # @type: function
    # @brief: Constructor
    # @param: [in] max_size: The maximum size of the cache.
    # @return: N/A
    def __init__( self, max_size ) :
        self._head = None
        self._size = 0    # Initial size is 0
        self._max_size = max_size   # The maximum size of cache

    # @type: function
    # @brief: Convert to string.
    # @param: N/A
    # @return: str(): A string for this object.
    def __str__( self ) :
        content = list()
        content.append( "{head}{" + str( self._size ) + "}" )
        curr_node = self._head
        while not ( curr_node is None ) :
            content.append( "[ " + curr_node.key + " : " + curr_node.value + " ]" )
            curr_node = curr_node.next

        return str().join( content )

    # @type: function
    # @brief: Access the value associated with key.
    # @param: [in] key: The key to access.
    # @return: The associated value of key.
    def access( self, key ) :
        value = None
        found = False

        curr_node = self._head
        while ( curr_node is not None ) and ( not found ) :
            if curr_node.key == key :
                value = curr_node.value
                found = True
                self._move_to_head( curr_node )
            else :
                curr_node = curr_node.next

        return value

    # @type: function
    # @brief: Update the cache with the given key-value pairs.
    # @param: N/A
    # @return: boolean
    #   True: The faults have been handled successfully.
    #   False:
    # TODO: Improve comments.
    def update( self, key_value_pairs, k = 1 ) :
        # Validate arguments
        if len( key_value_pairs ) < k :
            # The key_value_pairs is not in the right size.
            return False

        if k > self._max_size :
            # We cannot update the cache to one larger than the
            #   maximum size.
            return False

        # Try to break the k key_value_pairs into two parts:
        #   adds:
        #   replaces:
        # TODO: Improve comments
        remaining = self._max_size - self._size
        adds = k if remaining >= k else remaining
        replaces = 0 if remaining > k else ( k - remaining )

        # Update the cache.
        # We first drop some elements at the tail to make room for the new
        #   elements. This also guarantees that we do not exceed the size
        #   limit.
        # We then add the key_value_pairs to the cache.
        self._drop_tail_k( replaces )
        self._add_head_k( key_value_pairs, adds + replaces )

        return True

    # TODO: Add comments
    def _add_head_k( self, key_value_pairs, k ) :
        for i in range( 0, k ) :
            pair = key_value_pairs[i]

            # Create a new node.
            new_node = LLNode()
            new_node.key = pair[0]
            new_node.value = pair[1]

            # Link the new node into the cache.
            new_node.next = self._head
            new_node.prev = None
            if self._size > 0 :
                self._head.prev = new_node
            self._head = new_node

            # Increase the size
            self._size += 1

    # TODO: Add comments
    def _drop_tail_k( self, k ) :

        # Check the arguments
        if k == 0 or self._size == 0 or k > self._size :
            return

        curr_node = self._head
        for i in range( 0, self._size - k - 1 ) :
            curr_node = curr_node.next

        next_dropped_node = curr_node.next
        curr_node.next = None

        next_dropped_node.prev = None
        while next_dropped_node is not None :
            next_dropped_node2 = next_dropped_node.next
            if next_dropped_node2 is not None :
                next_dropped_node2.prev = None
            next_dropped_node.next = None
            next_dropped_node = next_dropped_node2

            # Decrease the size
            self._size -= 1

    # TODO: Add comments
    def _move_to_head( self, node ) :
        if self._head is node :
            return
        else :
            prev_node = node.prev
            next_node = node.next
            head_node = self._head

            self._head = node
            node.next = head_node
            node.prev = None

            if not ( prev_node is None ) :
                prev_node.next = next_node
            if not ( next_node is None ) :
                next_node.prev = prev_node


# =============================================================================
# @type: class
# @brief: LRU cache implemented with ordered dictionary.

class LRUCache2 ( object ) :

    def __init__( self, max_dynamic_size, max_static_size ):
        self._max_dynamic_size = max_dynamic_size
        self._cache = ordereddict.OrderedDict()

        self._cache_static_max_size = max_static_size
        self._cache_static = ordereddict.OrderedDict()
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
# @type: class
# @brief: The statistical results

class StatisticalData ( object ) :

    def __init__( self ) :
        self.total_query_count = 0
        self.hit_count = 0
        self.miss_count = 0
        self.start_time = None
        self.end_time = None

    def __str__( self ) :
        return ( "------------------------------" + "\n" +
                 "Statistical Data:" + "\n" +
                 "\tStart time:\t" + str( self.start_time ) + "\n" +
                 "\tEnd time:\t" + str( self.end_time ) + "\n" +
                 "\tTotal:\t\t" + str( self.total_query_count ) + "\n" +
                 "\tHit:\t\t" + str( self.hit_count ) + "\n" +
                 "\tMiss:\t\t" + str( self.miss_count ) + "\n" +
                 "------------------------------" + "\n"
        )

    def start_test( self ) :
        self.start_time = datetime.datetime.now()

    def end_test( self ) :
        self.end_time = datetime.datetime.now()

    def hit( self ) :
        self.total_query_count += 1
        self.hit_count += 1

    def miss( self ) :
        self.total_query_count += 1
        self.miss_count += 1
