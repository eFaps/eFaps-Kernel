@REM --------------------------------------------------------------------------
@REM
@REM Copyright 2003 - 2009 The eFaps Team
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

REM Logging with log4j
set CP=%CP%;%shellPathLib%\log4j-1.2.14.jar
set CP=%CP%;%shellPathLib%\commons-logging-1.1.jar

Rem digester to read xml files
set CP=%CP%;%shellPathLib%\commons-digester-1.8.jar
set CP=%CP%;%shellPathLib%\commons-beanutils-1.7.0.jar

REM command line parser
set CP=%CP%;%shellPathLib%\commons-cli-1.0.jar

REM evaluate expression in the xml files
set CP=%CP%;%shellPathLib%\commons-jexl-1.1.jar

REM used for toString methods
set CP=%CP%;%shellPathLib%\commons-lang-2.3.jar

REM HTTP Servlet Api
set CP=%CP%;%shellPathLib%\servlet-api-2.5.jar

REM Transaction Manager
set CP=%CP%;%shellPathLib%\geronimo-spec-jta-1.0-M1.jar
set CP=%CP%;%shellPathLib%\slide-kernel-2.1.jar

REM Java compiler
set CP=%CP%;%shellPathLib%\commons-jci-core-1.0.jar
set CP=%CP%;%shellPathLib%\commons-jci-fam-1.0.jar
set CP=%CP%;%shellPathLib%\commons-jci-javac-1.0.jar
set CP=%CP%;%shellPathLib%\asm-2.2.jar
set CP=%CP%;%shellPathLib%\asm-util-2.2.1.jar
set CP=%CP%;%shellPathLib%\asm-tree-2.2.1.jar
set CP=%CP%;%shellPathLib%\asm-analysis-2.2.1.jar
set CP=%CP%;%shellPathLib%\dependency-0.2.jar
REM javac
set CP=%CP%;%shellPathLib%\commons-jci-javac-1.0.jar
REM eclipse
set CP=%CP%;%shellPathLib%\commons-jci-eclipse-1.0.jar
set CP=%CP%;%shellPathLib%\core-3.2.0.658.jar
REM groovy
set CP=%CP%;%shellPathLib%\commons-jci-groovy-1.0.jar
set CP=%CP%;%shellPathLib%\groovy-all-1.0-jsr-03.jar
REM rhino
set CP=%CP%;%shellPathLib%\commons-jci-rhino-1.0.jar
set CP=%CP%;%shellPathLib%\js-1.6R5.jar
REM janino
set CP=%CP%;%shellPathLib%\commons-jci-janino-1.0.jar
set CP=%CP%;%shellPathLib%\janino-2.4.3.jar

REM Connection Pooling
set CP=%CP%;%shellPathLib%\commons-dbcp-1.2.2.jar
set CP=%CP%;%shellPathLib%\commons-pool-1.3.jar
set CP=%CP%;%shellPathLib%\commons-collections-3.1.jar

REM Database Driver
set CP=%CP%;%shellPathLib%\derbyclient-10.2.2.0.jar
set CP=%CP%;%shellPathLib%\postgresql-8.2-504.jdbc3.jar

REM VFSStore
set CP=%CP%;%shellPathLib%\commons-vfs-1.0.jar

REM needed for Programs
set CP=%CP%;%shellPathLib%\commons-collections-3.1.jar

REM used for stores
REM set CP=%CP%;%derbyLibPath%\commons-vfs-20050307052300.jar
REM database driver
set CP=%CP%;%JH%\oracle-drivers\ojdbc14_g.jar

echo Classpath:
echo ~~~~~~~~~~
echo %CP%

"%JAVA_HOME%\bin\java" -classpath %CP% org.efaps.js.Shell -bootstrap %BOOTSTRAP% %1 %2 %3 %4 %5 %6 %7 %8 %9
