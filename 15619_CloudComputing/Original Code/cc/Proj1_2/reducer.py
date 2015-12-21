#!/usr/bin/env python


# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 1.2(CC-Proj-1.2) reducer.
# @author: robin
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Import the dependent modules/packages

import sys
import re


# =============================================================================
# @type: constant
# @brief: The intermediate line related.

INTERMEDIATE_LINE_PARTS = 3
INTERMEDIATE_LINE_DELIMITER = '\t'
IDX_TITLE = 0
IDX_TIMESTAMP = 1
IDX_ACCESS_NUM = 2


# =============================================================================
# @type: constant
# @brief: CC-Proj-1.2-related

DATA_YEAR = 2014    # The year of the data that we are going to process
DATA_MONTH = 11     # The month of the data that we are going to process
DATA_DAYS = 30      # The days of the month

DATA_DATE_FORMAT_SPEC = "{0:04}{1:02}{2:02}"
REGEX_DATA_DATE_FORMAT = "^(19|20)\d\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$"
REGEX_DATA_ACCESS_NUM = "^\d+$"

ACCESS_NUM_THRESHOLD = 100000


# =============================================================================
# @type: variable
# @brief: The global variables

g_RegEx_DateTime = re.compile("")
g_RegEx_AccessNum = re.compile("")


# =============================================================================
# @type: class
# @brief: The monthly access information of a Wikipedia page.

class MonthlyPageAccessInfo :

    # @type: method
    # @brief: Constructor
    # @param: [in] year: The year of the access information that this object stores.
    # @param: [in] month: The month of the access information that this object stores.
    # @param: [in] days: How many days are there in the month.
    # @return: N/A
    # @note: days could have been figured out according to year and month,
    #   but to make things simple, we let the caller specify.
    def __init__(self, year, month, days):

        # 1). Initialize the object-owned members.
        #   But for now, we just assume that year, month and days are valid.
        self.title = str()
        self.daily = dict()
        self.daily_zero = dict()    # The daily data with values set to 0. Used for quick reset.
        self.total_access_num = 0

        # 2). Construct the member daily_zero according to the year, month and days
        #   specified, in the form of yyyymmdd.
        #   Note that range's second parameter is the "stop" value, so we need
        #   to add one in order to get the desired day.
        for day in range( 1, days+1 ) :
            full_date = DATA_DATE_FORMAT_SPEC.format( year, month, day )
            self.daily_zero[full_date] = 0   # Initialize the access number to 0.

        # 3). Copy daily_zero to daily.
        self.daily = self.daily_zero.copy()

    # @type: method
    # @brief: Serializer
    # @param: N/A
    # @return: string: The string representation of this object.
    # @note: The format should be:
    #   <total month views>\t<article name>\t<date1:page views for date1>\t
    #   <date2:page views for date2> ...
    def __str__(self):
        str_rep = str( self.total_access_num ) + '\t' + self.title + '\t'

        kvpairs = self.daily.items()
        kvpairs.sort()  # We must sort according to the dates.
        for kvpair in kvpairs :
            str_rep += ( kvpair[0] + ':' + str( kvpair[1] ) + '\t' )

        return str_rep

    # @type: method
    # @brief: Set the title.
    # @param: [in] title: The new title.
    # @return: N/A
    def setTitle(self, title):
        self.title = title

    # @type: method
    # @brief: Add the access number of this page on the specified date to the
    #   corresponding date and the total access number.
    # @param: [in] date: The date of the page access information.
    # @param: [in] access: The number of access of the page on that date.
    # @return: N/A
    def addDayAccessNum(self, date, access):
        self.daily[date] += access
        self.total_access_num += access

    # @type: method
    # @brief: Reset the object to the initial state.
    # @param: N/A
    # @return: N/A
    def reset(self):
        self.title = str()
        self.daily = self.daily_zero.copy()
        self.total_access_num = 0


# =============================================================================
# @type: function
# @brief: Construct the global variables.
# @param: N/A
# @return: N/A

def ConstructGlobalVariables() :
    global g_RegEx_DateTime
    global g_RegEx_AccessNum

    g_RegEx_DateTime = re.compile( REGEX_DATA_DATE_FORMAT )
    g_RegEx_AccessNum = re.compile( REGEX_DATA_ACCESS_NUM )


# =============================================================================
# @type: function
# @brief: Verify if the given intermediate output line is valid.
# @param: [in] parts: The different parts of the intermediate output line.
# @return: bool: Whether the line is valid.
#   True: the line is valid.
#   False: the line is not valid.

def ValidIntermediateLine( parts ) :
    global g_RegEx_DateTime
    global g_RegEx_AccessNum

    # A valid intermediate line must satisfy the following conditions:

    # 1). It must have exactly three parts: the title, the date and the access
    #   number.
    if len( parts ) != INTERMEDIATE_LINE_PARTS :
        return False

    # 2). Its first part must be a string. This is satisfied by anyway, so we
    #   don't check this specifically.
    pass

    # 3). Its second part must be a date string, in the format of yyyymmdd.
    #   In this project, we don't want to perform too much check because that
    #   may sacrifice the performance a lot. We just assume that the date
    #   is logically reasonable (e.g. no stuff like 2014/02/31), and we only
    #   check if the string satisfies the required format.
    # Source of RegEx: http://www.regular-expressions.info/dates.html
    if g_RegEx_DateTime.match( parts[IDX_TIMESTAMP] ) == None :
        return False

    # 4). Its third part must not be a negative integer number.
    if g_RegEx_AccessNum.match( parts[IDX_ACCESS_NUM] ) == None :
        return False

    return True


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

    # current_page_access_info stores the access information of the page that
    #   is being processed currently.
    current_page_access_info = MonthlyPageAccessInfo( DATA_YEAR, DATA_MONTH, DATA_DAYS )

    # Process every line read in.
    for line in file_obj :

        # Split the line into different parts.
        parts = line.split( INTERMEDIATE_LINE_DELIMITER )

        # Check if the line is a valid line. If not, then throw it.
        if not ValidIntermediateLine( parts ) :
            continue

        if parts[IDX_TITLE] == current_page_access_info.title :
            # If we are still processing the current page, then we add the
            #   access information.
            current_page_access_info.addDayAccessNum( parts[IDX_TIMESTAMP], int( parts[IDX_ACCESS_NUM] ) )
        else :
            # If we are now processing a new page, then we should complete
            #   all the work necessarily to be done to the previous page.
            if current_page_access_info.total_access_num > ACCESS_NUM_THRESHOLD :
                print str( current_page_access_info )

            # And then initialize the access information for the new page.
            current_page_access_info.reset()
            current_page_access_info.setTitle( parts[IDX_TITLE] )
            current_page_access_info.addDayAccessNum( parts[IDX_TIMESTAMP], int( parts[IDX_ACCESS_NUM] ) )

    # Don't forget the last page, because it is the last one, there would be
    #   no title switch. We must manually trigger its output.
    if current_page_access_info.total_access_num > ACCESS_NUM_THRESHOLD :
        print str( current_page_access_info )

    # Close the file if it is not stdin.
    if not ( file_obj is sys.stdin ) :
        file_obj.close()


# =============================================================================
# @type: script
# @brief: The main entry of the script.

if __name__ == "__main__" :

    ConstructGlobalVariables()

    Main( sys.argv )