#!/bin/sh

#
# You can set JAVA_HOME to point ot JDK 1.3 
# or shell will try to deterine java location using which
#

#JAVA_HOME=/l/jdk1.3

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

#POLICY=-Djava.security.policy=${TOP}/src/tests/java.policy
#-Debug=true -Dlog.names=
#JAVA_OPTS=-Debug=true
JAVA_OPTS="$JAVA_OPTS -Djava.compiler=NONE"

TOP=.

CP=`echo lib/*.jar | tr ' ' ':'`
CP=$JAVA_HOME/lib/tools.jar:$CP
CP=${TOP}/build/classes:$CP
#CP=${TOP}/build/samples:$CP
#CP=${TOP}/build/xsoap.jar:$CP

if [ -z "$1" ] ; then
   echo Please specify test name.
   exit 1
fi

NAME=$1
shift

if [ "$NAME" = "registry" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP soaprmi.registry.RegistryImpl  $*"
elif [ "$NAME" = "hello_server" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.hello.HelloServiceImpl $*"
elif [ "$NAME" = "hello_client" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.hello.HelloClient $*"
elif [ "$NAME" = "counter_server" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.counter.CounterServiceImpl $*"
elif [ "$NAME" = "counter_client" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.counter.CounterClient $*"
elif [ "$NAME" = "at_server" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.allTypes.ServerImpl $*"
elif [ "$NAME" = "at_client" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.allTypes.ClientImpl $*"
elif [ "$NAME" = "ping_client" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.pingpong.Client $*"
elif [ "$NAME" = "ping_server" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.pingpong.ServerImpl $*"
elif [ "$NAME" = "im_server" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.im.ServerImpl $*"
elif [ "$NAME" = "im_client" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.im.ClientImpl $*"
elif [ "$NAME" = "junit" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP tests.AllTests $*"
elif [ "$NAME" = "parser_test" -o "$NAME" = "xpp_test" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP tests.XppTest $*"
elif [ "$NAME" = "xppcount" -o "$NAME" = "xpp_count"  ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.pullparser.XPPCount  $*"
elif [ "$NAME" = "tokenizer" -o "$NAME" = "sxtcount"  -o "$NAME" = "sxt_count" -o "$NAME" = "sxt" ] ; then
  CMD="$JAVA $JAVA_OPTS -cp $CP samples.pullparser.SXTCount  $*"
else
  CMD="$JAVA $JAVA_OPTS -cp $CP $NAME $*"
fi

echo $CMD
$CMD
