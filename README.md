# kluster

Dockerized Akka cluster.

This is minimal application of an akka cluster running on docker.

For more information see [kluster](http://daniberg.com/2017/06/06/kluster.html) and
[kluster updates](http://daniberg.com/2017/06/20/kluster-updates.html).

## building and developing

The command below builds the kluster container and starts a cluster with three nodes: `kluster1`, `kluster2` and `kluster3`.

```bash
./run.sh
```

If you run `docker ps` you should see the three nodes running

```
CONTAINER ID  IMAGE    PORTS              NAMES
e6736ffb43f9  kluster  2550/tcp, 8080/tcp kluster3
57010bcd1afe  kluster  2550/tcp, 8080/tcp kluster2
1df1583dfb4f  kluster  2550/tcp, 8080/tcp kluster1
```

You can check the logs of a single node running `docker logs`, for example:

```bash
docker logs -f kluster1
```

To kill the kluster run

```bash
./run.sh -k
```

To recompile the code and redeploy the cluster execute:

```bash
./run.sh -f
```

You can remove a node from the cluster using the `-r` option. For example, to remove `kluster2`, run:

```
./run.sh -r 2
```

Use the `-a COUNT` option to add new nodes to the cluster. For example, to add 2
new nodes to the cluster, run:

```
./run.sh -a 2
```

The nodes run the akka http manager api. The containers have installed curl and
jq so it's possible to trigger the api via `docker exec`. For example:

```
docker exec -ti kluster1 curl http://localhost:19999/members | jq .
```

The response of the command above is similar to this

```json
{
  "selfNode": "akka.tcp://kluster@kluster1:2550",
  "leader": "akka.tcp://kluster@kluster1:2550",
  "oldest": "akka.tcp://kluster@kluster1:2550",
  "unreachable": [],
  "members": [
    {
      "node": "akka.tcp://kluster@kluster1:2550",
      "nodeUid": "-1569330506",
      "status": "Up",
      "roles": [
        "cruncher"
      ]
    },
    {
      "node": "akka.tcp://kluster@kluster2:2550",
      "nodeUid": "2119541425",
      "status": "Up",
      "roles": [
        "cruncher"
      ]
    },
    {
      "node": "akka.tcp://kluster@kluster3:2550",
      "nodeUid": "865465060",
      "status": "Up",
      "roles": [
        "cruncher"
      ]
    }
  ]
}
```