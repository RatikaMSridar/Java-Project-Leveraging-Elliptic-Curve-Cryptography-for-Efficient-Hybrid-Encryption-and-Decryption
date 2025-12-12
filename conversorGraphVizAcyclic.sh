#!/bin/bash

#Convert 'possible' cyclic graph to 'acyclic' graph
acyclic $1 -v -o$2
#Generate PNG output acycli graphic
dot $2 -Tpng -o$3
