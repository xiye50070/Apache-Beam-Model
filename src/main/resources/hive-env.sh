# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# kv configuration reads the following configuration
# If you need to manually add the configuration, please do not add it to KV Configuration Use Start to End
# KV Configuration Use Start
export HIVE_CONF_DIR=/opt/hive/conf/
export HIVE_AUX_JARS_PATH=/opt/hive/lib/hive-cli-3.1.2.jar,/opt/atlas/hook/hive/atlas-plugin-classloader-2.1.0.jar,/opt/atlas/hook/hive/hive-bridge-shim-2.1.0.jar
export HADOOP_OPTS="$HADOOP_OPTS -Dfile.encoding=UTF-8"

# KV Configuration Use End
#if [ "$SERVICE" = "cli" ]; then
export HADOOP_CLIENT_OPTS="$HADOOP_CLIENT_OPTS -Dlog4j.configuration=$HIVE_CONF_DIR/hive-log4j2.properties"
#fi
#export HADOOP_OPTS="$HADOOP_OPTS -Dlogging.config=$HIVE_CONF_DIR/hive-log4j2.properties"
# Set Hive and Hadoop environment variables here. These variables can be used
# to control the execution of Hive. It should be used by admins to configure
# the Hive installation (so that users do not have to set environment variables
# or set command line parameters to get correct behavior).
#
# The hive service being invoked (CLI etc.) is available via the environment
# variable SERVICE

# Hive Client memory usage can be an issue if a large number of clients
# are running at the same time. The flags below have been useful in 
# reducing memory usage:
#
if [ "$SERVICE" = "cli" ]; then
  if [ -z "$DEBUG" ]; then
    export HADOOP_OPTS="$HADOOP_OPTS -XX:NewRatio=12 -Xms512m -XX:MaxHeapFreeRatio=40 -XX:MinHeapFreeRatio=15 -XX:+UseParNewGC -XX:-UseGCOverheadLimit"
  else
    export HADOOP_OPTS="$HADOOP_OPTS -XX:NewRatio=12 -Xms512m -XX:MaxHeapFreeRatio=40 -XX:MinHeapFreeRatio=15 -XX:-UseGCOverheadLimit"
  fi
fi

if [ "$SERVICE" = "metastore" ]; then
    export HADOOP_CLIENT_OPTS="$HADOOP_CLIENT_OPTS -javaagent:/opt/exporter/jmx/jmx_prometheus_javaagent-0.16.1.jar=9212:/opt/exporter/jmx/hivemetastore.yaml"
fi

if [ "$SERVICE" = "hiveserver2" ]; then
  export HADOOP_CLIENT_OPTS="$HADOOP_CLIENT_OPTS -javaagent:/opt/exporter/jmx/jmx_prometheus_javaagent-0.16.1.jar=9213:/opt/exporter/jmx/hiveserver2.yaml"
fi

# The heap size of the jvm stared by hive shell script can be controlled via:
#
export HADOOP_HEAPSIZE=512
#
# Larger heap size may be required when running queries over large number of files or partitions. 
# By default hive shell scripts use a heap size of 256 (MB).  Larger heap size would also be 
# appropriate for hive server.


# Set HADOOP_HOME to point to a specific hadoop install directory
HADOOP_HOME=/opt/hadoop