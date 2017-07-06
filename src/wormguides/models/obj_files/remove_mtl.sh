#!/bin/bash

DIR=.

for f in $DIR/*.obj; do
	sed -i '/usemtl/d' ./$f 
done
