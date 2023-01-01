@echo off
REM $Id: make.bat,v 1.2 2001/06/21 21:10:18 aslom Exp $
REM
REM REQUIRED: Please make sure that JAVA_HOME points to JDK1.3 
REM   and make sure that JAVA_HOME\lib contains tools.jar !!!!
REM 

set JAVA_HOME=c:\jdk13

REM 
REM No need to modify anything after this line.
REM --------------------------------------------------------------------


echo JAVA_HOME=%JAVA_HOME%
set JAVA=%JAVA_HOME%\bin\java
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%
REM %JAVA% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 -buildfile src/build.xml
set CMD=%JAVA% -classpath %CP% -Dant.home=lib org.apache.tools.ant.Main -buildfile src/build.xml %1 %2 %3 %4 %5 %6 %7 %8 %9 
echo %CMD%
%CMD%
