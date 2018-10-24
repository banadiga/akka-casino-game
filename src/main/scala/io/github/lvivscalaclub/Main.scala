package io.github.lvivscalaclub

import akka.actor.{ActorSystem, Props}
import io.github.lvivscalaclub.client.SlotMachine
import io.github.lvivscalaclub.server.PlayerSupervisor

import scala.util.Random

object Main extends App {
  val system = ActorSystem("Game")

  val supervisor = system.actorOf(Props[PlayerSupervisor], "supervisor")
  val webClient = system.actorOf(Props(new SlotMachine(supervisor, "Ihor")),
    s"SlotMatching-${System.currentTimeMillis()}")
}

trait RandomGenerator {
  def win: Boolean
}

object DefaultRandomGenerator extends RandomGenerator {
  override def win: Boolean = Random.nextBoolean()
}