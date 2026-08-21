cmake_minimum_required(VERSION 4.1.2)

project("Hilorama")

add_library(Hilorama SHARED engine.cpp native-lib.cpp)

find_library(log-lib log)
target_link_libraries(Hilorama ${log-lib})

find_library(jnigraphics-lib jnigraphics)
target_link_libraries(Hilorama ${log-lib} ${jnigraphics-lib})

target_include_directories(Hilorama PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})