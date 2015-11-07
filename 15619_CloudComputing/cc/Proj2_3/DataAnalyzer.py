# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.3(CC-Proj-2.3)
# @author: robin
# @email: yaobinw@andrew.cmu.edu
# @note: This is the data pattern analysis program.


# =============================================================================
# @type: directive
# @brief: Imports.

import CCLib.CommonDefs


# =============================================================================
# @type: Constants
# @brief: CC-Proj-2.3 related constants

FILE_TARGET_ID_LIST = "data_pure_id.txt"
FILE_PATTERN_DIFFERENCE = "pattern_diff.txt"
FILE_PATTERN_ALL = "pattern_all.txt"
FILE_PATTERN_FREQUENCY = "pattern_frequency.txt"


# =============================================================================
# @type: class
# @brief: ID frequency data

class IDFrequencyData ( object ) :

    def __init__( self ) :
        self.count = 0
        self.occurrence_first = -1
        self.occurrence_last = -1


# =============================================================================
# @type: class
# @brief: Pattern data of the target IDs.

class PatternAnalyzer ( object ) :

    LIMIT_LOWER = 0
    LIMIT_UPPER = 1

    RANGE_1 = (-10, 10)
    RANGE_2 = (-50, 50)
    RANGE_3 = (-100, 100)
    RANGE_4 = (-250, 250)
    RANGE_5 = (-500, 500)
    RANGE_11 = (0, 20)
    RANGE_12 = (0, 100)
    RANGE_13 = (0, 200)
    RANGE_14 = (0, 500)
    RANGE_15 = (0, 1000)
    RANGE_20 = (-1000000, 1000000)

    RANGES = [ RANGE_1, RANGE_2, RANGE_3, RANGE_4, RANGE_5,
               RANGE_11, RANGE_12, RANGE_13, RANGE_14, RANGE_15,
               RANGE_20 ]

    def __init__( self, id_list ) :
        self.id_list = id_list
        self.id_list_length = len( id_list )
        self.diffs = list()
        self.diff_max = 0
        self.diff_min = 0

        self.RANGE_NUM = len( PatternAnalyzer.RANGES )
        self.range_in_hits = [0] * self.RANGE_NUM
        self.range_out_hits = [0] * self.RANGE_NUM

        self.id_frequency = list()

    def analyze( self ) :
        self._pattern_diff()    # Must be done first.
        self._pattern_range_cover()
        self._pattern_max_min_diff()
        self._pattern_frequency()

    def write_files( self ) :
        # Differences
        diff_list_length = len( self.diffs )
        file_diff = open( FILE_PATTERN_DIFFERENCE, CCLib.CommonDefs.FILE_OPEN_MODE_WRITE_ONLY )
        for i in range( 0, diff_list_length ) :
            file_diff.write( str( self.diffs[i] ) + "\n" )
        file_diff.close()

        # Ranges
        file_patterns = open( FILE_PATTERN_ALL, CCLib.CommonDefs.FILE_OPEN_MODE_WRITE_ONLY )

        for i in range( 0, self.RANGE_NUM ) :
            file_patterns.write( "------------------------------\n" )
            file_patterns.write( "\tRange: " + str( PatternAnalyzer.RANGES[i] ) + "\n" )
            file_patterns.write( "\tIn-range hits: " + str( self.range_in_hits[i] ) + "\n" )
            file_patterns.write( "\tOut-range hits: " + str( self.range_out_hits[i] ) + "\n" )

        file_patterns.write( "------------------------------\n" )
        file_patterns.write( "\tDiff max: " + str( self.diff_max ) + "\n" )
        file_patterns.write( "\tDiff min: " + str( self.diff_min ) + "\n" )

        file_patterns.close()

        # Frequency
        file_freq = open( FILE_PATTERN_FREQUENCY, CCLib.CommonDefs.FILE_OPEN_MODE_WRITE_ONLY )
        for i in range( 0, len( self.id_frequency ) ) :
            pair = self.id_frequency[i]
            file_freq.write( "ID: " + str( pair[0] ) + "\t\t" +
                             "Frequency: " + str( pair[1].count ) + "\t\t" +
                             "Coverage: " + str( pair[1].occurrence_last - pair[1].occurrence_first ) +
                             "\n" )
        file_freq.close()

    def _pattern_diff( self ) :
        for i in range( 0, self.id_list_length - 1 ) :
            prev_id = self.id_list[i]
            next_id = self.id_list[i+1]
            diff = int( next_id ) - int( prev_id )
            self.diffs.append( diff )

    def _pattern_range_cover( self ) :
        diff_list_length = len( self.diffs )
        for i in range( 0, diff_list_length ) :
            for j in range( 0, self.RANGE_NUM ) :
                if ( ( PatternAnalyzer.RANGES[j][PatternAnalyzer.LIMIT_LOWER] <= self.diffs[i] ) and
                         ( self.diffs[i] <= PatternAnalyzer.RANGES[j][PatternAnalyzer.LIMIT_UPPER] ) ):
                    self.range_in_hits[j] += 1
                else :
                    self.range_out_hits[j] += 1

    def _pattern_max_min_diff( self ) :
        self.diff_max = max( self.diffs )
        self.diff_min = min( self.diffs )

    def _pattern_frequency( self ) :
        id_freq_dict = { str() : IDFrequencyData() }

        for i in range( 0, self.id_list_length ) :
            data = None
            if not id_freq_dict.has_key( self.id_list[i] ) :
                data = IDFrequencyData()
                id_freq_dict[self.id_list[i]] = data
            else :
                data = id_freq_dict[self.id_list[i]]

            data.count += 1

            if data.count > 1 :
                data.occurrence_last = i
            else :
                data.occurrence_first = i
                data.occurrence_last = i

        id_freq_list = id_freq_dict.items()
        id_freq_list.sort( key = lambda tup:tup[1].count, reverse = True )

        self.id_frequency = id_freq_list


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

    id_list = read_test_id_list( FILE_TARGET_ID_LIST )

    analyzer = PatternAnalyzer( id_list )

    analyzer.analyze()

    analyzer.write_files()

# =============================================================================
# @type: script
# @brief: The entry point of the script.

if __name__ == "__main__" :
    main()
