# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 15619(CC-Proj-15619)
# @author: Team Wishmaster
# @email: yaobinw@andrew.cmu.edu


def read_expected(file_name):
    lines = list()

    expected_file = open( file_name, 'r' )
    for line in expected_file:
        lines.append(line.split('\n')[0])
    expected_file.close()

    return lines


def read_actual(file_name):
    lines = list()

    actual_file = open( file_name, 'r' )
    for line in actual_file:
        lines = line.split("\\n")
    actual_file.close()

    return lines


def main():
    expected_result = read_expected("/Users/robinwen/Desktop/Temp/Cloud/Phase2/Q3/Expected.txt")
    actual_result = read_actual("/Users/robinwen/Desktop/Temp/Cloud/Phase2/Q3/Actual.txt")
    length = len(expected_result)
    print "Lines: " + str(len(expected_result)) + ", " + str(len(actual_result))
    for i in range(0, length):
        expected_line = expected_result[i]
        actual_line = actual_result[i]
        s1 = str(expected_line)
        s2 = str(actual_line)
        if s1 != s2:
            print len(s1), len(s2)
            for j in range(0, len(s1)):
                print s1[j], ord(s1[j]), s2[j], ord(s2[j])
            print "Line #" + str(i) + " is different: " + expected_line + " vs " + actual_line

if __name__ == "__main__":
    main()