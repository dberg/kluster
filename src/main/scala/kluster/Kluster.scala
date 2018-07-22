package kluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import akka.event.Logging
import java.net.InetAddress
import scala.annotation.tailrec
import scala.util.Try

object Kluster {

  /** Create the cluster programatically. */
  def createCluster(implicit system: ActorSystem, hostname: String): Option[Cluster] = {
    if (hostname startsWith system.name) {
      getRunningNode(system) map { addr =>
        val cluster = Cluster(system)
        // The first node joins itself and the remaining nodes join the first node.
        cluster.join(addr)
        cluster
      }
    } else {
      val log = Logging(system, "Kluster")
      log.error(s"Invalid hostname '$hostname'. Are you sure you're trying to run this via run.sh?")
      None
    }
  }

  /** Get the running node with the lowest index. The nodes are created following
    * the pattern kluster{INDEX} where INDEX is a 1 based auto-increment
    * integer. */
  @tailrec
  private def getRunningNode(system: ActorSystem, nodeNumber: Int = 1): Option[Address] = {
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

  private def isReachable(hostname: String): Boolean = Try[Boolean] {
    InetAddress.getByName(hostname).isReachable(1000)
  }.getOrElse(false)
}
