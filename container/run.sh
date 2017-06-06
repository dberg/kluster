#!/usr/bin/env bash

KLUSTER_JAR=/usr/local/kluster/kluster.jar
KLUSTER_JAVA_MEM=1G

java \
  -Xms${KLUSTER_JAVA_MEM} \
  -Xmx${KLUSTER_JAVA_MEM} \
  ${app_config} \
  -jar ${KLUSTER_JAR} \

