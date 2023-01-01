#!/bin/sh
# $Id: make.sh,v 1.1 2001/03/11 22:17:00 aslom Exp $

# You can set JAVA_HOME to point ot JDK 1.3 
# or shell will try to deterine java location using which
# IMPPORTANT:  and make sure that JAVA_HOME\lib contains tools.jar !!!!


# 
# No need to modify anything after this line.
# --------------------------------------------------------------------

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`/usr/bin/which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
else
  JAVA=$JAVA_HOME/bin/java
fi

#echo "JAVA= $JAVA"

if [ ! "`$JAVA -version 2>&1 | grep "\ 1\.3"`" ]; then 
    echo Required 1.3 verion of JDK: can not use $JAVA
    echo Current version is:
    $JAVA -version
    exit 1
fi 

CLASSPATH=.

#JIKES_DIR=/l/extreme/bin
if [ -x "$JIKES_DIR/jikes" ] ; then
  PATH=$JIKES_DIR:$PATH
  export PATH
  OPTS=-Dbuild.compiler=jikes
  CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/rt.jar
  echo "Jikes enabled" 
fi

CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:$CLASSPATH
CLASSPATH=$JAVA_HOME/lib/tools.jar:$CLASSPATH

CMD="$JAVA $OPTS -classpath $CLASSPATH -Dant.home=lib org.apache.tools.ant.Main $@ -buildfile src/build.xml"
echo $CMD
$CMD
