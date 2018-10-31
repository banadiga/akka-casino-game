package io.github.lvivscalaclub

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.github.lvivscalaclub.client.SlotMachine

object Client extends App {
  val config = ConfigFactory.load("application-client.conf")
  val actorSystem = ActorSystem("GameActorSystemClient", config)
  val supervisor = actorSystem.actorSelection("akka.tcp://Server@127.0.0.1:2552/user/Supervisor")

  createSlotMachine(supervisor, "user1")

  def createSlotMachine(supervisor: ActorSelection, user: String): ActorRef = {
    val propsWebClient = Props(new SlotMachine(supervisor, user))
    actorSystem.actorOf(propsWebClient, s"slot-machine-${System.nanoTime()}")
  }
}
