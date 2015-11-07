if __name__ == "__main__":
    pkeys1 = dict()
    pkeys2 = dict()
    pkey1 = str()
    pkey2 = str()

    datafile = open( "/Users/robinwen/Desktop/Temp/Cloud/Phase2/Q4_MySQL/part-00000", 'r' )
    for line in datafile:
        parts = line.split(',')
        pkey1 = str(parts[0]+','+parts[1]+','+parts[2]).encode('utf-8')

        if not pkeys1.has_key(pkey1):
            pkeys1[pkey1] = 1
        else:
            print pkey1 + " already exists in pkeys1"
    datafile.close()

    outputfile = open("/Users/robinwen/Desktop/Temp/Cloud/Phase2/Q4_MySQL/query8output.txt", 'r')
    for line in outputfile:
        parts = line.split('\n')[0].split('\t')
        pkey2 = str(parts[0]+','+parts[1]+','+parts[2]).encode('utf-8')

        if not pkeys2.has_key(pkey2):
            pkeys2[pkey2] = 1
        else:
            print pkey2 + " already exists in pkeys2"
    outputfile.close()

    print "pkeys1 has " + str(len(pkeys1)) + " lines"
    print "pkeys2 has " + str(len(pkeys2)) + " lines"

    counter = 0
    for pkey in pkeys1:
        if not pkeys2.has_key(pkey):
            counter += 1
            print str(counter) + ":" + pkey
