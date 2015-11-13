#!/bin/bash
FILE="${SRCROOT}/${PROJECT_NAME}/precopy.txt"
while read line; do
stringarray=($line)
cp -r ${stringarray[0]} ${stringarray[1]}
done < $FILE
