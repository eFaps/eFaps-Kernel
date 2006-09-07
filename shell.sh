#!/bin/sh
#
#------------------------------------------------------------------------------
#
# Copyright 2006 The eFaps Team
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Author:          tmo
# Revision:        $Rev$
# Last Changed:    $Date$
# Last Changed By: $Author$
#
# -----------------------------------------------------------------------------
# Script to run the eFaps shell
#
# Environment Variable Prequisites
#
#   EFAPS_HOME      (Optional) May point at your eFaps "build" directory.
#
#   EFAPS_OPTS      (Optional) Java runtime options when the shell is executed.
#
#   EFAPS_CLASSPATH (Optional) Java class path options used the shell is 
#                   executed.
# -----------------------------------------------------------------------------

# OS specific support
cygwin=false
os400=false
darwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
Darwin*) darwin=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set EFAPS_HOME if not already set
[ -z "$EFAPS_HOME" ] && EFAPS_HOME=`cd "$PRGDIR" ; pwd`

# bootstrap file
BOOTSTRAP=$EFAPS_HOME/bootstrap.xml

shellPath=$EFAPS_HOME/shell/target
shellPathLib=$shellPath/eFaps-Shell-1.0-SNAPSHOT/WEB-INF/lib

CP=.

# eFaps itself
CP=$CP:$shellPath/classes
CP=$CP:$shellPathLib/eFaps-Kernel-1.0-SNAPSHOT.jar

# Mozillas JavaScript
CP=$CP:$shellPathLib/js-1.6R1.jar

# Logging with log4j
CP=$CP:$shellPathLib/log4j-1.2.9.jar
CP=$CP:$shellPathLib/commons-logging-1.0.4.jar

# digester to read xml files
CP=$CP:$shellPathLib/commons-digester-1.7.jar
CP=$CP:$shellPathLib/commons-beanutils-1.6.jar

# used for toString methods
CP=$CP:$shellPathLib/commons-lang-2.1.jar

# HTTP Servlet Api
CP=$CP:$shellPathLib/servlet-api-2.4.jar

# Transaction Manager
CP=$CP:$shellPathLib/geronimo-spec-jta-1.0-M1.jar
CP=$CP:$shellPathLib/slide-kernel-2.1.jar

# Connection Pooling
CP=$CP:$shellPathLib/commons-dbcp-1.2.1.jar
CP=$CP:$shellPathLib/commons-pool-1.2.jar
CP=$CP:$shellPathLib/commons-collections-2.1.jar

# Database Driver
CP=$CP:$shellPathLib/derbyclient-10.1.2.1.jar
CP=$CP:$shellPathLib/postgresql-8.1-407.jdbc3.jar

# used for stores
# set CP=%CP%;%derbyLibPath%/commons-vfs-20050307052300.jar
# database driver
# set CP=%CP%;%JH%/oracle-drivers/ojdbc14_g.jar

# add external defined class paths
[ -z "$EFAPS_CLASSPATH" ] & CP=$EFAPS_CLASSPATH:$CP

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  EFAPS_HOME=`cygpath --absolute --windows "$EFAPS_HOME"`
  BOOTSTRAP=`cygpath --absolute --windows "$BOOTSTRAP"`
  CP=`cygpath --path --windows "$CP"`
fi

echo Classpath:
echo ~~~~~~~~~~
echo $CP

java $EFAPS_OPTS -classpath $CP org.efaps.js.Shell -bootstrap $BOOTSTRAP $1 $2 $3 $4 $5 $6 $7 $8 $9
