#------------------------------------------------------------------------------
# Kluster
#------------------------------------------------------------------------------
# sbt clean assembly
# docker build -t kluster --build-arg KLUSTER_VERSION=$(git rev-parse HEAD) .
#------------------------------------------------------------------------------
FROM anapsix/alpine-java:jre8
MAINTAINER Daniel Berg <danibberg@gmail.com>

# setup kluster service
ENV KLUSTER /usr/local/kluster
RUN apk add --update curl jq && rm -rf /var/cache/apk/*
RUN mkdir -p ${KLUSTER}
COPY ./container/run.sh ${KLUSTER}/
ARG KLUSTER_VERSION
COPY ./builddir/kluster-${KLUSTER_VERSION}.jar ${KLUSTER}/kluster.jar

CMD ${KLUSTER}/run.sh
