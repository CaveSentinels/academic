#!/bin/sh
export mapreduce_map_input_file="pagecounts-20141101-000000"
cat pagecounts-20141101-000000 | python mapper.py > test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141101-010000"
cat pagecounts-20141101-010000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141101-020000"
cat pagecounts-20141101-020000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141104-090000"
cat pagecounts-20141104-090000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141104-100000"
cat pagecounts-20141104-100000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141104-110000"
cat pagecounts-20141104-110000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141106-220000"
cat pagecounts-20141106-220000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141107-010000"
cat pagecounts-20141107-010000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141107-080000"
cat pagecounts-20141107-080000 | python mapper.py >> test_middle_output.txt

export mapreduce_map_input_file="pagecounts-20141130-210000"
cat pagecounts-20141130-210000 | python mapper.py >> test_middle_output.txt

cat test_middle_output.txt | LC_ALL='C' sort | python reducer.py > test_final_output.txt
