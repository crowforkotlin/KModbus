cmake_minimum_required(VERSION 3.4.1)

add_library(SerialPort SHARED src/cpp/SerialPort.h src/cpp/SerialPort.c)

# Include libraries needed for libserial_port lib
target_link_libraries(SerialPort android log)