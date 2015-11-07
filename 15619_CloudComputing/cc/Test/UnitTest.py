# =============================================================================
# @type: file
# @brief: Cloud Computing: Unit test code for all the projects.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

from Proj1_2.mapper import GetDatePartInWikimediaLogFileName
from Proj1_2.mapper import Retainable
from Proj1_2.reducer import ValidIntermediateLine
from Proj1_2.reducer import MonthlyPageAccessInfo

import Proj1_2.mapper
import Proj1_2.reducer


# =============================================================================
# @type: function
# @brief: Test Proje1_2_mapper.GetDatePartInWikimediaLogFileName()

def UT_Proje1_2_mapper_GetDatePartInWikimediaLogFileName() :

    expected_date_part = "20141101"

    date_part = GetDatePartInWikimediaLogFileName( "projectcounts-20141101-000000" )
    if date_part != expected_date_part :
        return 1

    date_part = GetDatePartInWikimediaLogFileName( "projectcounts-20141101-000000/" )
    if date_part != expected_date_part :
        return 2

    date_part = GetDatePartInWikimediaLogFileName( "s3://wikipediatraf/201411-gz/projectcounts-20141101-000000" )
    if date_part != expected_date_part :
        return 3

    date_part = GetDatePartInWikimediaLogFileName( "s3://wikipediatraf/201411-gz/projectcounts-20141101-000000/" )
    if date_part != expected_date_part :
        return 4

    return 0


# =============================================================================
# @type: function
# @brief: Test Proj1_2_mapper.Retainable_1_2()

def UT_Proj1_2_mapper_Retainable() :

    line = "De\tSimpsonregel\t1\t76433"
    if Retainable( line.split( '\t' ) ) :
        return 1

    line = "en\tEnglish_title\t10\t76433"
    if not Retainable( line.split( '\t' ) ) :
        return 2

    line = "en %D8%A3%D8%A8%D9%88%D9%84%D9%88_4 1 52369"
    if Retainable( line.split( '\t' ) ) :
        return 3

    line = "en\t  \t1\t76433"
    if not Retainable( line.split( '\t' ) ) :
        return 4

    line = "en\t\t1\t76433"
    if Retainable( line.split( '\t' ) ) :
        return 5

    return 0


# =============================================================================
# @type: function
# @brief: Test Proj1_2_mapper.Retainable_1_2()

def UT_Proj1_2_reducer_ValidIntermediateLine() :

    Proj1_2.reducer.ConstructGlobalVariables()

    test_pairs = []
    test_pairs.append( ( "title\t20150130\t10", True ) )
    test_pairs.append( ( "title\t20150130", False ) )
    test_pairs.append( ( "title\t20150130\t10\t10", False ) )
    test_pairs.append( ( "title\t2015a130\t10", False ) )
    test_pairs.append( ( "title\t201a1130\t10", False ) )
    test_pairs.append( ( "title\t2015113a\t10", False ) )
    test_pairs.append( ( "title\t20150130\t1a", False ) )

    result = ""
    for test_pair in test_pairs :
        line = test_pair[0]
        parts = line.split( '\t' )
        if ValidIntermediateLine( parts ) != test_pair[1] :
            result += "Fail: " + str( test_pair ) + '\n'

    return ( "All succeeded" if result == "" else result )


def UT_Proj1_2_reducer_PageAccessInfo() :

    year = 2014
    month = 7
    days = 31

    obj = MonthlyPageAccessInfo( year, month, days )

    obj.setTitle( "page1" )

    expected_keys = [
        "20140701", "20140702", "20140703", "20140704", "20140705",
        "20140706", "20140707", "20140708", "20140709", "20140710",
        "20140711", "20140712", "20140713", "20140714", "20140715",
        "20140716", "20140717", "20140718", "20140719", "20140720",
        "20140721", "20140722", "20140723", "20140724", "20140725",
        "20140726", "20140727", "20140728", "20140729", "20140730",
        "20140731"
    ]

    missed_keys = []

    for expected_key in expected_keys :
        if not obj.daily.has_key( expected_key ) :
            missed_keys.append( expected_key )

    # Test the addDayAccessNum() method
    missed_access_number = list()
    modified_daily_zero = list()

    for day in range( 0, days ) :
        obj.addDayAccessNum( expected_keys[day], int( day+1 ) )

    for day in expected_keys :
        if obj.daily[ day ] != int( day ) - year * 10000 - month * 100 :
            missed_access_number.append( obj.daily[ day ] )

    # Also make sure that daily_zero is never modified after construction.
    for day in expected_keys :
        if obj.daily_zero[ day ] != 0 :
            modified_daily_zero.append( obj.daily_zero[ day ] )

    # Test the getTotalAccessNum() method
    total_access_num_correct = True
    if obj.total_access_num != (days + 1) * days / 2 :
        total_access_num_correct = False

    # Test reset() method
    obj.reset()
    reset_works_fine = True
    if obj.title != str() or obj.total_access_num != 0 :
        reset_works_fine = False

    for pair in obj.daily_zero.items() :
        if pair[1] != 0 :
            reset_works_fine = False
            break

    for pair in obj.daily.items() :
        if pair[1] != 0 :
            reset_works_fine = False
            break

    # Return the test result.
    return "Key count: " + str( len( obj.daily ) ) + "; " + \
           "Missed Keys: " + str( missed_keys ) + "; " + \
           "Modified Daily Zero: " + str( modified_daily_zero ) + "; " + \
           "Missed Access: " + str( missed_access_number ) + "; " + \
           "Total Access: " + str( total_access_num_correct ) + "; " + \
           "Reset: " + str( reset_works_fine ) + "; " + \
           "String Representation: " + str( obj )

# =============================================================================
# @type: function
# @brief: The main work flow of unit test functions.

def Main() :

    test_suites = []
    test_suites.append( UT_Proje1_2_mapper_GetDatePartInWikimediaLogFileName )
    test_suites.append( UT_Proj1_2_mapper_Retainable )
    test_suites.append( UT_Proj1_2_reducer_ValidIntermediateLine )
    test_suites.append( UT_Proj1_2_reducer_PageAccessInfo )

    for test in test_suites :
        print "Test: " + test.func_name + " : " + str( test() )


# =============================================================================
# @type: script
# @brief: The entry point of the script.

Main()