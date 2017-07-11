#!/bin/bash

DIR=.
for f in $DIR/*.obj; do
	sed -i '/mtllib/d' ./$f 
done
