package kluster

import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import akka.event.Logging

object Kluster {

  def createCluster(implicit system: ActorSystem, hostname: String): Option[Cluster] = {
    // Create cluster programatically.
    val addr = Address("akka.tcp", system.name, "kluster1", 2550)
    if (hostname startsWith system.name) {
      val cluster = Cluster(system)
      // The first node joins itself and the remaining nodes join the first node.
      cluster.join(addr)
      // Subscribe to the cluster events.
      cluster.subscribe(
        system.actorOf(Props[KlusterObserver], s"Observer:$hostname"),
        classOf[MemberEvent],
        classOf[UnreachableMember]
      )
      Some(cluster)
    } else {
      val log = Logging(system, "Kluster")
      log.error("Wrong hostname. No cluster will be created.")
      None
    }
  }

}
