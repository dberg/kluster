package kluster

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.{Member, MemberStatus}
import akka.event.Logging

class KlusterObserver extends Actor {

  val log = Logging(context.system, this)

  def receive = {
    case state: CurrentClusterState =>
      log.info(s"OBSERVER current cluster state $state")
    case MemberUp(member: Member) =>
      log.info(s"OBSERVER member up $member")
    case MemberJoined(member: Member) =>
      log.info(s"OBSERVER member joined $member")
    case MemberExited(member: Member) =>
      log.info(s"OBSERVER member exited $member")
    case MemberRemoved(member: Member, previousStatus: MemberStatus) =>
      log.info(s"OBSERVER member removed $member and previous status $previousStatus")
    case UnreachableMember(member: Member) =>
      log.info(s"OBSERVER unreachable member $member")
    case ReachableMember(member: Member) =>
      log.info(s"OBSERVER reachable member $member")
    case MemberWeaklyUp(member: Member) =>
      log.info(s"OBSERVER member weakly up $member")
    case MemberLeft(member: Member) =>
      log.info(s"OBSERVER member left $member")
    case msg =>
      log.info(s"OBSERVER got a weird message $msg")
  }
}

object KlusterObserver {

  /** Subscribe to the cluster events. */
  def setup(cluster: Cluster, hostname: String): Unit = {
    cluster.subscribe(
      cluster.system.actorOf(Props[KlusterObserver], s"Observer:$hostname"),
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

}
