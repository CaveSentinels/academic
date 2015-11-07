# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 15619(CC-Proj-15619)
# @email: yaobinw@andrew.cmu.edu


# =============================================================================
# @type: directive
# @brief: Imports.

import urllib2
import sys
import random


# =============================================================================

def bombardQuery2(target_url):
    # TODO
    pass


# =============================================================================

def bombardQuery3(target_url):
    while True:
        user_id = random.randint(0, 2999999999)
        request_q3 = "http://" + target_url + "/q3?userid=" + user_id
        print urllib2.urlopen(request_q3).read()


# =============================================================================

def bombardQuery4(target_url):
    # TODO
    pass


# =============================================================================

def bombardQuery5(target_url):
    while True:
        user_id_count = random.randint(0, 1000)
        request_elm_list = list()
        request_elm_list.append("http://")
        request_elm_list.append(target_url)
        request_elm_list.append("/q5?userlist=")
        for i in range(0, user_id_count):
            request_elm_list.append(str(random.randint(0, 99999999)))
            request_elm_list.append(',')
        request_elm_list.append(str(random.randint(0, 99999999)))
        request_elm_list.append("&start=2005-01-01&end=2015-12-31")

        request_q5 = "".join(request_elm_list)
        print urllib2.urlopen(request_q5).read()


# =============================================================================

def bombardQuery6(target_url):
    while True:
        m = random.randint(0, 1000000)
        n = m + random.randint(0, 99999999)
        request_q6 = "http://" + target_url + "/q6?m=" + str(m) + "&n=" + str(n)
        print urllib2.urlopen(request_q6).read()


# =============================================================================

def Main( args ) :
    query_index = args[1]
    target_url = args[2]

    if query_index == "q6":
        bombardQuery6(target_url)
    elif query_index == "q5":
        bombardQuery5(target_url)
    else:
        pass


# =============================================================================

if __name__ == "__main__" :
    Main( sys.argv )