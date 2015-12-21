cmake_minimum_required(VERSION 3.1)
project(CCProj43)

set(CMAKE_C_COMPILER "/usr/local/bin/clang-omp")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp")

set(CMAKE_C_COMPILER "/usr/local/bin/clang-omp++")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp -std=c++11")

include_directories(/usr/local/Cellar/boost/1.57.0/include/)
include_directories(/usr/local/include/libiomp)
include_directories(../../src/)

set(SOURCE_FILES bfs.cpp)
add_executable(CCProj43 ${SOURCE_FILES})