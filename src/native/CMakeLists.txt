cmake_minimum_required(VERSION 2.8)

project(test_opengl)

set(CMAKE_CXX_STANDARD 14)

set(BASE_INCLUDE_DIR /usr/include)

include_directories(/usr/lib/jvm/java-11-openjdk-amd64/include)
include_directories(/usr/lib/jvm/java-11-openjdk-amd64/include/linux)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src)

set(BASE_COMPILER_WARN_FLAGS "-pedantic -Wall -Wextra -Wfloat-equal -Wundef -Wcast-align -Wwrite-strings -Wlogical-op -Wmissing-declarations -Wredundant-decls -Wshadow -Woverloaded-virtual")

#C++ compiler flags
set(C_FLAGS_DEBUG "-g3 -Og -fopt-info-vec-missed ${BASE_COMPILER_WARN_FLAGS} -DDEBUG")
set(C_FLAGS_RELEASE "-g3 -Og -fopt-info-vec-missed ${BASE_COMPILER_WARN_FLAGS}")

set(CMAKE_C_FLAGS_DEBUG "${C_FLAGS_DEBUG}" CACHE STRING "" FORCE)
set(CMAKE_C_FLAGS_RELEASE "${C_FLAGS_RELEASE}" CACHE STRING "" FORCE)

set(CXX_FLAGS_DEBUG "-g3 -Og -fopt-info-vec-missed ${BASE_COMPILER_WARN_FLAGS} -DDEBUG")
set(CXX_FLAGS_RELEASE "-g3 -Og -fopt-info-vec-missed ${BASE_COMPILER_WARN_FLAGS}")

set(CMAKE_CXX_FLAGS_DEBUG "${CXX_FLAGS_DEBUG}" CACHE STRING "" FORCE)
set(CMAKE_CXX_FLAGS_RELEASE "${CXX_FLAGS_RELEASE}" CACHE STRING "" FORCE)

#GLFW
set(GLFW_HEADER_FILES ${BASE_INCLUDE_DIR}/GLFW/glfw3.h)
add_library(glfw ${GLFW_HEADER_FILES})
set_target_properties(glfw PROPERTIES LINKER_LANGUAGE CXX)

#GLEW
set(GLEW_HEADER_FILES ${BASE_INCLUDE_DIR}/GL/glew.h)
add_library(glew ${GLEW_HEADER_FILES})
set_target_properties(glew PROPERTIES LINKER_LANGUAGE CXX)

add_library(chunkymapview SHARED src/mapView.cpp src/io_github_notstirred_ChunkyMapView.h src/io_github_notstirred_ChunkyMapView.cpp src/log/logger.h src/log/logger.c src/mapView.h)

target_link_libraries(chunkymapview PRIVATE libglfw.so libGLEW.so libGL.so)