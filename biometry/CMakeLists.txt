cmake_minimum_required(VERSION 3.1)
project(atelier)

set(CMAKE_GXX_FLAGS "-Wall -Wextra -Wconversion  -pedantic -std=gnu11") 

find_package(OpenCV REQUIRED)
message(STATUS "OpenCV_INCLUDE_DIRS = ${OpenCV_INCLUDE_DIRS}")  
message(STATUS "OpenCV_LIBS = ${OpenCV_LIBS}")

include_directories(${OpenCV_INCLUDE_DIRS} "/usr/include/opencv4/opencv2" )

add_executable(atelier atelier.cpp)
target_link_libraries(atelier ${OpenCV_LIBS})





