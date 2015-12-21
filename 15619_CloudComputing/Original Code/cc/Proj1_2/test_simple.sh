#!/bin/sh
# The log files at 00:00:00
export mapreduce_map_input_file="test-20141101-000000.txt"
cat test-20141101-000000.txt | python mapper.py > test_middle_output.txt
export mapreduce_map_input_file="test-20141102-000000.txt"
cat test-20141102-000000.txt | python mapper.py >> test_middle_output.txt
export mapreduce_map_input_file="test-20141103-000000.txt"
cat test-20141103-000000.txt | python mapper.py >> test_middle_output.txt
export mapreduce_map_input_file="test-20141129-000000.txt"
cat test-20141129-000000.txt | python mapper.py >> test_middle_output.txt
export mapreduce_map_input_file="test-20141130-000000.txt"
cat test-20141130-000000.txt | python mapper.py >> test_middle_output.txt

# The log files at 01:00:00
export mapreduce_map_input_file="test-20141102-010000.txt"
cat test-20141102-010000.txt | python mapper.py >> test_middle_output.txt
export mapreduce_map_input_file="test-20141103-010000.txt"
cat test-20141103-010000.txt | python mapper.py >> test_middle_output.txt
export mapreduce_map_input_file="test-20141129-010000.txt"
cat test-20141129-010000.txt | python mapper.py >> test_middle_output.txt
export mapreduce_map_input_file="test-20141130-010000.txt"
cat test-20141130-010000.txt | python mapper.py >> test_middle_output.txt

cat test_middle_output.txt | LC_ALL='C' sort | python reducer.py > test_final_output.txt
