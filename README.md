# kluster

Dockerized Akka cluster.

This is minimal application of an akka cluster running on docker.

For more information see [kluster](http://daniberg.com/2017/06/06/kluster.html).

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
