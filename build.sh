#!/bin/bash

if [[ $# == 0 ]]
then
    CMD=""
else
    CMD=$1
fi

case $CMD in
    "")
        mvn assembly:assembly -DdescriptorId=jar-with-dependencies
    ;;
    "skip")
        mvn assembly:assembly -DdescriptorId=jar-with-dependencies -Dmaven.test.skip=true
    ;;
    "ut")
        mvn test -Dtest=`echo "$2" | tr -s \. \#`
    ;;
esac
