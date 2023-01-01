@echo off
REM $Id: run.bat,v 1.5 2001/08/01 19:55:11 aslom Exp $
REM
REM REQUIRED: Please make sure that JAVA_HOME points to JDK1.3
REM

REM set JAVA_HOME=c:\jdk1.3

REM 
REM No need to modify anything after this line.
REM --------------------------------------------------------------------

set JAVA=%JAVA_HOME%\bin\java
set TOP=.

REM set CP=%TOP%\lib\xerces112.jar
REM set CP=%1 %2 %3 %4 %5 %6 %7 %8 %9
REM set CP=%TOP%\lib\junit32.jar;%CP%
REM set CP=%TOP%\lib\minisoap.jar;%CP%
REM set CP=%TOP%\build\classes;%CP%
REM set CP=%TOP%\build\samples;%CP%
REM set CP=%TOP%\build\xsoap.jar;%CP%
REM set CP=%TOP%\build\soaprmi11.jar;%CP%

REM set JAVA=%JAVA_HOME%\bin\java
REM set CP=%JAVA_HOME%\lib\tools.jar;%CP%
REM set CP=%TOP%\build\samples;%CP%

set cp=%CLASSPATH%
REM for %%i in (lib\*.jar) do call cp.bat %%i
set CP=lib\junit37.jar;%CP%
set CP=%TOP%\build\classes;%CP%
set CP=%TOP%\build\soaprmi11.jar;%CP%

REM set POLICY=-Djava.security.policy=D:\java\Janus\src\resman\janus\archive\java.policy
REM set JAVA_OPTS=-Dlog.names=
REM set JAVA_OPTS="%JAVA_OPTS%"

REM echo NAME=$NAME
set NAME=%1
shift

if "%NAME%" == "registry" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% soaprmi.registry.RegistryImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "hello_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.hello.HelloServiceImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "hello_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.hello.HelloClient %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "interop_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.interop.Client %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "counter_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.counter.CounterServiceImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "counter_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.counter.CounterClient %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "at_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.allTypes.ServerImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "at_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.allTypes.ClientImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "ping_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pingpong.Client %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "ping_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pingpong.ServerImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "sort_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.sortsearch.Client %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "sort_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.sortsearch.ServerImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "im_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.im.ServerImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "im_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.im.ClientImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "im2_server" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.im2.ServerImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "im2_client" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.im2.ClientImpl %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "junit" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% tests.AllTests %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "xpp_test" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% tests.XppTest %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "parser_test" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% tests.XppTest %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "xppcount" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pullparser.XPPCount  %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "xpp_count" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pullparser.XPPCount  %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "sxtcount" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pullparser.SXTCount %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "sxt_count" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pullparser.SXTCount %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "tokenizer" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pullparser.SXTCount  %1 %2 %3 %4 %5 %6 %7 %8 %9
) else if "%NAME%" == "sxt" (
   set CMD=%JAVA% %JAVA_OPTS% -cp %CP% samples.pullparser.SXTCount %1 %2 %3 %4 %5 %6 %7 %8 %9
) else (
  set CMD=%JAVA% %JAVA_OPTS% -cp %CP% %NAME% %1 %2 %3 %4 %5 %6 %7 %8 %9
)
echo %CMD%
%CMD%

