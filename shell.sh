#!/bin/sh
#
#--------------------------------------------------------------------------
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
# Revision:        $Rev: 264 $
# Last Changed:    $Date: 2006-07-18 12:17:07 +0200 (Di, 18 Jul 2006) $
# Last Changed By: $Author: tmo $
#
# --------------------------------------------------------------------------

BOOTSTRAP=$PWD/bootstrap.xml

shellPath=$PWD/shell/target
shellPathLib=$shellPath/eFaps-Shell-1.0-SNAPSHOT/WEB-INF/lib

CP=.

# eFaps itself
CP=$CP:$shellPath/classes
CP=$CP:$shellPathLib/eFaps-Kernel-1.0-SNAPSHOT.jar

# Mozillas JavaScript
CP=$CP:$shellPathLib/js-1.6R1.jar

# Logging
CP=$CP:$shellPathLib/commons-logging-1.0.4.jar

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

echo Classpath:
echo ~~~~~~~~~~
echo $CP

java -classpath $CP org.efaps.js.Shell -bootstrap $BOOTSTRAP $1 $2 $3 $4 $5 $6 $7 $8 $9
