#!/bin/sh

if [ ! -z "$1" ] ; then
   echo Automatic JUnit tests do not take any arguments.
   exit 1
fi

./run.sh junit $*

