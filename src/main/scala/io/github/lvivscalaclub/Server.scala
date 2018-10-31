package io.github.lvivscalaclub

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.github.lvivscalaclub.server.PlayerSupervisor

object Server extends App {
  val config = ConfigFactory.load("application-server.conf")
  val actorSystem = ActorSystem("Server", config)
  val props = Props[PlayerSupervisor]
  val supervisor = actorSystem.actorOf(props, "Supervisor")
}
