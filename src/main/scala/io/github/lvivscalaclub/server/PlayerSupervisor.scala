package io.github.lvivscalaclub.server

import akka.actor.{Actor, ActorLogging, Props}
import io.github.lvivscalaclub.{Failure, NewGameRequest, NewGameResponse, PlayerAlreadyConnected}

class PlayerSupervisor extends Actor with ActorLogging {

  override def receive: Receive = {
    case game@NewGameRequest(userId, name) =>
      context.child(userId.toString) match {
        case Some(_) =>
          sender ! NewGameResponse(Failure, Some(PlayerAlreadyConnected))
        case None =>
          val playerActor = context.actorOf(Props(new Player(userId)), s"user-${userId.toString}")
          log.info(s"Supervisor forward $name to player")
          playerActor.forward(game)
      }
  }
}