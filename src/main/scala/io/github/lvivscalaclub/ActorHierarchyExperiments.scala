package io.github.lvivscalaclub

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import io.github.lvivscalaclub.Card.Card

import scala.util.Random

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
    case game@NewGameRequest(userId, name) =>
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

class Player extends Actor with ActorLogging {

  var balance: Long = 20

  val RollCost = 1
  val InitState: Receive = {
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
  val RollState: Receive = {
    case RollRequest(_: UUID) =>
      balance = balance - RollCost

      val screen = getScreen

      if (isWin(screen)) {
        val win = getWin(screen)
        sender ! RollResponse(screen, win)
        context.become(takeWinOrGoToDouble(win))
      } else {
        sender ! RollResponse(screen, 0)
      }

      sender ! Balance(balance)
  }

  def takeWinOrGoToDouble(win: Long, step: Int = 5): Receive = {
    case TakeWinRequest =>
      takeWin(win)
    case GoToDoubleRequest =>
      context.become(takeDouble(win, step))
  }

  def takeWin(win: Long): Unit = {
    balance = balance + win
    sender ! Balance(balance)
    context.become(RollState)
  }

  def takeDouble(win: Long, step: Int): Receive = {
    case DoubleRequest(card) =>
      if (isWin(card)) {
        val newWin = win * 2
        sender ! DoubleResponse(newWin)
        if (step > 1) {
          context.become(takeWinOrGoToDouble(newWin, step - 1))
        } else {
          takeWin(newWin)
        }
      } else {
        takeWin(0)
      }
  }

  override def receive: Receive = InitState

  private def getScreen: Seq[Seq[Int]] = {
    Seq.fill(3)(Seq.fill(5)(Random.nextInt(9)))
  }

  private def isWin(screen: Seq[Seq[Int]]): Boolean = {
    Random.nextBoolean()
  }

  private def isWin(card: Card): Boolean = {
    Random.nextBoolean()
  }

  private def getWin(screen: Seq[Seq[Int]]): Long = {
    Random.nextInt(9) + 1
  }
}

object ActorHierarchyExperiments extends App {
  val system = ActorSystem("Game")

  val supervisor = system.actorOf(Props[Supervisor], "supervisor")
  val webClient = system.actorOf(Props(new SlotMachine(supervisor)), s"SlotMatching-${System.currentTimeMillis()}")
}
