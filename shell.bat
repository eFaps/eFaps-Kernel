@REM --------------------------------------------------------------------------
@REM
@REM Copyright 2006 The eFaps Team
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM
@REM Author:          tmo
@REM Revision:        $Rev$
@REM Last Changed:    $Date$
@REM Last Changed By: $Author$
@REM
@REM --------------------------------------------------------------------------

@echo off

set BOOTSTRAP=%cd%\bootstrap.xml

set shellPath=%cd%\shell\target
set shellPathLib=%shellPath%\eFaps-Shell-1.0-SNAPSHOT\WEB-INF\lib

set CP=

REM eFaps itself
set CP=%CP%;%shellPath%\classes
set CP=%CP%;%shellPathLib%\eFaps-Kernel-1.0-SNAPSHOT.jar

REM Mozillas JavaScript
set CP=%CP%;%shellPathLib%\js-1.6R1.jar

REM Logging
set CP=%CP%;%shellPathLib%\commons-logging-1.0.4.jar

REM used for toString methods
set CP=%CP%;%shellPathLib%\commons-lang-2.1.jar

REM HTTP Servlet Api
set CP=%CP%;%shellPathLib%\servlet-api-2.4.jar

REM Transaction Manager
set CP=%CP%;%shellPathLib%\geronimo-spec-jta-1.0-M1.jar
set CP=%CP%;%shellPathLib%\slide-kernel-2.1.jar

REM Connection Pooling
set CP=%CP%;%shellPathLib%\commons-dbcp-1.2.1.jar
set CP=%CP%;%shellPathLib%\commons-pool-1.2.jar
set CP=%CP%;%shellPathLib%\commons-collections-2.1.jar

REM Database Driver
set CP=%CP%;%shellPathLib%\derbyclient-10.1.2.1.jar



REM used for stores
REM set CP=%CP%;%derbyLibPath%\commons-vfs-20050307052300.jar
REM database driver
REM set CP=%CP%;%JH%\oracle-drivers\ojdbc14_g.jar

echo Classpath:
echo ~~~~~~~~~~
echo %CP%

%JAVA_HOME%\bin\java -classpath %CP% org.efaps.js.Shell -bootstrap %BOOTSTRAP% %1 %2 %3 %4 %5 %6 %7 %8 %9
