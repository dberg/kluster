package kluster

import akka.actor.ActorSystem
import akka.cluster.http.management.ClusterHttpManagement
import com.typesafe.config.ConfigFactory
import java.net.InetAddress

object Main {

  def main(args: Array[String]): Unit = {
    // The hostname is set in the docker run command.
    val hostname: String = {
      val inetAddr = InetAddress.getLocalHost()
      inetAddr.getHostName()
    }

    val config = ConfigFactory
      .parseString(s"akka.remote.netty.tcp.hostname=$hostname")
      .withFallback(ConfigFactory.load())

    implicit val system = ActorSystem("kluster", config)

    Kluster.createCluster(system, hostname) match {
      case Some(cluster) =>
        ClusterHttpManagement(cluster).start()
        ()
      case None =>
    }
  }
}
