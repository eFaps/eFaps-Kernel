@echo off

set derbyHost=localhost
set derbyPort=1527
set derbyLibPath=%cd%\derby\target\Derby-Tools-1.0-SNAPSHOT\WEB-INF\lib

set CLASSPATH=%derbyLibPath%\derby-10.1.2.1.jar;%derbyLibPath%\derbytools-10.1.2.1.jar;%derbyLibPath%\derbynet-10.1.2.1.jar;%CLASSPATH%

@REM ---------------------------------------------------------
@REM -- start Derby as a Network server
@REM ---------------------------------------------------------
%JAVA_HOME%\bin\java org.apache.derby.drda.NetworkServerControl shutdown -h %derbyHost% -p %derbyPort%
