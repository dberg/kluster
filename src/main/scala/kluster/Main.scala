package kluster

import akka.actor.ActorSystem
import akka.management.AkkaManagement
import com.typesafe.config.ConfigFactory
import java.net.InetAddress

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Route
import akka.management.cluster.ClusterHttpManagement
import akka.management.http.ManagementRouteProviderSettings
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger

object Main {

  val logger = Logger("Main")

  def main(args: Array[String]): Unit = {
    val hostname: String = getHostname()
    val port = 8080

    val config = ConfigFactory
      .parseString(s"akka.remote.netty.tcp.hostname=$hostname")
      .withFallback(ConfigFactory.load())

    implicit val system = ActorSystem("kluster", config)
    implicit val materializer = ActorMaterializer()

    Kluster.createCluster(system, hostname) match {
      case Some(cluster) =>
        KlusterObserver.setup(cluster, hostname)
        AkkaManagement(system).start()
        val route = getAkkaHttpManagementRoute(system, hostname, port)
        Http().bindAndHandle(route, hostname, port)
        ()
      case None =>
        logger.error("Failed to create cluster")
    }
  }

  // The hostname is set in the docker run command: kluster{INDEX}.
  private def getHostname(): String = {
    val inetAddr = InetAddress.getLocalHost()
    inetAddr.getHostName()
  }

  private def getAkkaHttpManagementRoute(system: ActorSystem, hostname: String, port: Int): Route = {
    val routeSettings = new ManagementRouteProviderSettings {
      override def selfBaseUri: Uri = s"http://$hostname:$port"
    }
    ClusterHttpManagement(system).routes(routeSettings)
  }
}
