package kluster

import akka.actor.ActorSystem
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory
import java.net.InetAddress

import akka.http.scaladsl.Http
import akka.management.cluster.scaladsl.ClusterHttpManagementRoutes
import com.typesafe.scalalogging.Logger

object Main {

  val logger = Logger("Main")

  def main(args: Array[String]): Unit = {
    val hostname: String = getHostname()
    val port = 8080

    val config = ConfigFactory
      .parseString(s"akka.remote.netty.tcp.hostname=$hostname")
      .withFallback(ConfigFactory.load())

    implicit val system: ActorSystem = ActorSystem("kluster", config)

    Kluster.createCluster(system, hostname) match {
      case Some(cluster) =>
        KlusterObserver.setup(cluster, hostname)
        AkkaManagement(system).start()
        //val route = getAkkaHttpManagementRoute(system, hostname, port)
        val route = ClusterHttpManagementRoutes.readOnly(cluster)
        Http().bindAndHandle(route, hostname, port)
        ()
      case None =>
        logger.error("Failed to create cluster")
    }
  }

  // The hostname is set in the docker run command: kluster{INDEX}.
  private def getHostname(): String = {
    val inetAddr = InetAddress.getLocalHost
    inetAddr.getHostName
  }
}
