#!/bin/sh
#------------------------------------------------------------------------------
#
# Copyright 2003 - 2009 The eFaps Team
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
#   JAVA_HOME       (Optional) location of a JDK home dir.
#   EFAPS_HOME      (Optional) May point at your eFaps "build" directory.
#
#   EFAPS_OPTS      (Optional) Java runtime options when the shell is executed.
#
# -----------------------------------------------------------------------------

# OS specific support
cygwin=false
darwin=false
case "`uname`" in
  CYGWIN*) cygwin=true;;
  Darwin*) darwin=true 
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

# only set EFAPS_HOME if not already defined
if [ -z "$EFAPS_HOME" ] ; then
  ## resolve links - $0 may be a link to eFaps's home
  PRG="$0"

  # resolve links - $PRG may be a softlink
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done

  EFAPS_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  saveddir=`pwd`
  EFAPS_HOME=`cd "$EFAPS_HOME" && pwd`
  cd "$saveddir"

fi

###############################################################################

# define JAVA command
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=java
  fi
fi

# test if JAVACMD file exists
if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

# warning if JAVA_HOME not defined
if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

###############################################################################

# For Cygwin, ensure paths are in UNIX format before anything is touched
#if $cygwin ; then
#  [ -n "$EFAPS_HOME" ] && EFAPS_HOME=`cygpath --unix "$EFAPS_HOME"`
#  [ -n "$JAVA_HOME" ] &&  JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
#  [ -n "$CLASSPATH" ] &&  CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
#fi


# bootstrap file
#BOOTSTRAP=$EFAPS_HOME/bootstrap.xml

#shellPath=$EFAPS_HOME/shell/target
#shellPathLib=$shellPath/eFaps-Shell-1.0-SNAPSHOT/WEB-INF/lib

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVACMD=`cygpath --absolute --windows "$JAVACMD"`
#  EFAPS_HOME=`cygpath --absolute --windows "$EFAPS_HOME"`
#  BOOTSTRAP=`cygpath --absolute --windows "$BOOTSTRAP"`
#  CP=`cygpath --path --windows "$CP"`
fi

echo Using Settings:
echo ===============
echo EFAPS_HOME = $EFAPS_HOME

exec "$JAVACMD" \
    -classpath "$EFAPS_HOME"/lib/classworlds-*.jar \
    -Dclassworlds.conf=$EFAPS_HOME/etc/classworlds.conf \
    -DeFaps.home=$EFAPS_HOME \
    -Dshell.parameter.jaasConfigFile=$EFAPS_HOME/conf/jaas.config \
    -Dshell.parameter.configFile=file:$EFAPS_HOME/conf/jetty.xml \
    org.codehaus.classworlds.Launcher \
    $*
