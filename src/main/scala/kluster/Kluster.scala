package kluster

import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import akka.event.Logging
import java.net.InetAddress
import scala.annotation.tailrec
import scala.util.Try

object Kluster {

  def isReachable(hostname: String): Boolean = Try[Boolean] {
    InetAddress.getByName(hostname).isReachable(1000)
  }.getOrElse(false)

  @tailrec
  def getRunningNode(system: ActorSystem, nodeNumber: Int = 1): Option[Address] = {
    if (nodeNumber < 1 || nodeNumber > 1000) {
      None
    } else {
      val nodeName = s"kluster$nodeNumber"
      val reachable = isReachable(nodeName)
      if (reachable) {
        Some(Address("akka.tcp", system.name, nodeName, 2550))
      } else {
        getRunningNode(system, nodeNumber + 1)
      }
    }
  }

  // Create cluster programatically.
  def createCluster(implicit system: ActorSystem, hostname: String): Option[Cluster] = {
    if (hostname startsWith system.name) {
      getRunningNode(system) map { addr =>
        val cluster = Cluster(system)
        // The first node joins itself and the remaining nodes join the first node.
        cluster.join(addr)
        // Subscribe to the cluster events.
        cluster.subscribe(
          system.actorOf(Props[KlusterObserver], s"Observer:$hostname"),
          classOf[MemberEvent],
          classOf[UnreachableMember]
        )
        cluster
      }
    } else {
      val log = Logging(system, "Kluster")
      log.error(s"Invalid hostname '$hostname'. Are you sure you're trying to run this via run.sh?")
      None
    }
  }

}
