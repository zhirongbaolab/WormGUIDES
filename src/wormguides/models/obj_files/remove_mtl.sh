#!/bin/bash

DIR=.
for f in $DIR/*.obj; do
	# echo $f
	sed -i '/mtllib/d' ./$f 
done
