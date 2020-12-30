cmake_minimum_required(VERSION 3.17)
project(Server C)

set(CMAKE_C_STANDARD 11)

set(GCC_COVERAGE_COMPILE_FLAGS "-Wall")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${GCC_COVERAGE_COMPILE_FLAGS}" )

find_package( Threads )

add_executable(Server src/main.c src/thread.c src/thread.h src/constants.h src/main_server.cpp src/main_server.h src/structs/user.c src/structs/user.h src/structs/list_head.c src/structs/list_head.h src/messaging.c src/messaging.h)
target_link_libraries( Server ${CMAKE_THREAD_LIBS_INIT} )
