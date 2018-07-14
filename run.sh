#!/usr/bin/env bash

#------------------------------------------------------------------------------
# kluster
#------------------------------------------------------------------------------
# Create an akka cluster via docker using a user-defined network.
#------------------------------------------------------------------------------
network=kluster
akka_management_host=localhost
akka_management_port=8558

usage() {
    echo "
Usage: $0 [-f] [-k] [-a] [-r]

Examples:

# create cluster
$0

# rebuild cluster
$0 -f

# kill cluster
$0 -k

# add 2 nodes
$0 -a 2

# remove node kluster3
$0 -r 3
"
    exit 1;
}

force=0
kill=0
add_nodes=0
add_nodes_count=0
remove_node=0
remove_node_name=""

while getopts ":fkhr:a:" o; do
    case "${o}" in
        f)
            force=1
            ;;
        k)
            kill=1
            ;;
        a)
            add_nodes=1
            add_nodes_count=${OPTARG}
            ;;
        r)
            remove_node=1
            remove_node_name="kluster${OPTARG}"
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
    node_name=$1 # for example: kluster1
    docker run --rm -itd \
           --expose=8080 --expose=2550 --expose=$akka_management_port \
           --network=kluster --hostname=$node_name --name=$node_name kluster
}

cluster_containers=""
set_cluster_containers() {
    cluster_containers=`docker ps --filter "name=kluster" --format "{{.Names}}" | sort | xargs echo -n | tr '\n' ' '`
}

assert_cluster_is_running() {
    if [[ -z $cluster_containers ]]; then
        echo "cluster is not running."
        exit 1
    fi
}

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

set_cluster_containers

# kill cluster
if [[ $kill == 1 ]]; then
    kill_cluster
    exit 0
fi

# add nodes
regex_ints='^[0-9]+$'
if [[ $add_nodes == 1 ]]; then
    if ! [[ $add_nodes_count =~ $regex_ints ]]; then
        echo "invalid number of nodes to add to the cluster."
        exit 1
    fi
    assert_cluster_is_running
    last_node=`echo ${cluster_containers##* }`
    base_index="${last_node/kluster/}"
    echo "adding $add_nodes_count node(s) to the cluster"
    for ((i=1; i<=$add_nodes_count; i++)); do
        node_number=$(($i + $base_index))
        node_name="kluster$node_number"
        echo "starting $node_name"
        run_container $node_name
    done
    exit 0
fi

# remove node from the cluster
if [[ $remove_node == 1 ]]; then
    assert_cluster_is_running
    # execute commands on last node
    echo "removing node $remove_node_name"
    node=`echo ${cluster_containers##* }`
    docker exec -ti $node curl -X DELETE http://$akka_management_host:$akka_management_port/cluster/members/akka.tcp://kluster@${remove_node_name}:2550
    exit 0;
fi

# run the cluster. default execution.
# kill running cluster if "force" is set.
if [[ $force == 1 && "$(docker ps -f name=kluster 2> /dev/null)" != "" ]]; then
    kill_cluster
fi

# build image if necessary
if [[ $force == 1 || "$(docker images -q kluster 2> /dev/null)" == "" ]]; then
    echo "building container image"
    sbt -batch clean assembly
    docker build -t kluster --build-arg KLUSTER_VERSION=$(git rev-parse HEAD) .
fi

# check if cluster is already running
if [[ $force == 0 && ! -z $cluster_containers ]]; then
    echo "cluster is already running with the following nodes: $cluster_containers."
    echo "nothing to be done."
    exit 1;
fi

# run cluster with 3 nodes
run_container kluster1
# the first seed node (kluster1) need to be started before all others.
# give it some time to start
sleep 30
run_container kluster2
run_container kluster3
