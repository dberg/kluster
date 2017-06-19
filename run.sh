#!/usr/bin/env bash

#------------------------------------------------------------------------------
# kluster
#------------------------------------------------------------------------------
# Create an akka cluster via docker using a user-defined network.
#------------------------------------------------------------------------------
network=kluster

usage() {
    echo "
Usage: $0 [-f] [-k]

Examples:

# create cluster
n$0

# rebuild cluster
$0 -f

# kill cluster
$0 -k
"
    exit 1;
}

force=0
kill=0
while getopts ":fkh" o; do
    case "${o}" in
	f)
	    force=1
	    ;;
	k)
	    kill=1
	    ;;
	h)
	    usage
	    ;;
	*)
	    ;;
    esac
done

kill_cluster() {
    echo "killing cluster $network"
    docker kill `docker ps -q -f name=$network` 2> /dev/null
}

run_container() {
    node_name=$1 # for example, kluster1
    docker run --rm -itd \
	   --expose=8080 --expose=2550 --expose=19999 \
	   --network=kluster --hostname=$node_name --name=$node_name kluster
}

if [[ $kill == 1 ]]; then
    kill_cluster
    exit 0
fi

# assert that docker is available
docker info > /dev/null 2>&1
if [[ $? -ne 0 ]]; then
    echo "docker daemon not available"
    exit 1
fi

# create network if needed
docker network ls | grep " $network " > /dev/null 2>&1
if [[ $? -ne 0 ]]; then
    echo "creating docker network $network"
    docker network create $network
fi

# kill running cluster if "force" is set.
if [[ $force == 1 && "$(docker ps -f name=kluster 2> /dev/null)" != "" ]]; then
    kill_cluster
fi

# build image if necessary.
if [[ $force == 1 || "$(docker images -q kluster 2> /dev/null)" == "" ]]; then
    echo "building container image"
    sbt -batch clean assembly
    docker build -t kluster --build-arg KLUSTER_VERSION=$(git rev-parse HEAD) .
fi

# run cluster with 3 nodes
run_container kluster1
# the first seed node (kluster1) need to be started before all others.
# give it some time to start
sleep 30
run_container kluster2
run_container kluster3
