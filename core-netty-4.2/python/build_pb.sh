#!/bin/bash
#
# creates the python classes for our .proto
#

#project_base="/Users/gash/workspace/messaging/core-netty/python"
project_base="/home/kartik/Documents/workspace/netty/core-netty-4.2/python"


rm ${project_base}/src/comm_pb2.py

protoc -I=${project_base}/resources --python_out=./src ${project_base}/resources/comm.proto 
