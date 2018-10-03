package io.github.lvivscalaclub

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}

trait Protocol

class SlotMachine(supervisor: ActorRef) extends Actor with ActorLogging {
  supervisor ! NewGameRequest(UUID.randomUUID(), "Ihor")

  override def receive: Receive = {
    case NewGameResponse(Success, None) => log.info("Response ok!")
    case NewGameResponse(Failure, Some(err)) => log.info(s"Response fail: $err!")
    case Balance(balance: Long) => log.info(s"Balance $balance")
  }
}

class Supervisor extends Actor with ActorLogging {

  override def receive: Receive = {
    case game@NewGameRequest(userId, name) => {
      context.child(userId.toString) match {
        case Some(_) =>
          sender ! NewGameResponse(Failure, Some(PlayerAlreadyConnected))
        case None =>
          val playerActor = context.actorOf(Props[Player], userId.toString)
          log.info(s"Supervisor forward $name to player")
          playerActor.forward(game)
      }
    }
  }
}

class Player extends Actor with ActorLogging {

  var balance: Long = 1

  val Init: Receive = {
    case NewGameRequest(userId, name) =>
      if (balance <= 0) {
        sender ! NewGameResponse(Failure, Some(ZeroBalance))
        self ! PoisonPill
      } else {
        log.info(s"New player created $name (id: $userId)")
        sender ! NewGameResponse(Success)
        sender ! Balance(balance)
      }
  }

  val Roll: Receive = {
    case RollRequest(_: UUID) =>
      ???
  }

  override def receive: Receive = Init
}


object ActorHierarchyExperiments extends App {
  val system = ActorSystem("Game")

  val supervisor = system.actorOf(Props[Supervisor], "supervisor")
  val webClient = system.actorOf(Props(new SlotMachine(supervisor)), s"SlotMatching-${System.currentTimeMillis()}")
}
