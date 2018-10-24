package io.github.lvivscalaclub

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import io.github.lvivscalaclub.Card.Card

import scala.util.Random

trait Protocol

class SlotMachine(supervisor: ActorRef) extends Actor with ActorLogging {
  private val uuid: UUID = UUID.randomUUID()
  supervisor ! NewGameRequest(uuid, "Ihor")

  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case NewGameResponse(Success, None) =>
      log.info("NewGameResponse ok!")
    case NewGameResponse(Failure, Some(err)) => log.info(s"NewGameResponse fail: $err!")
    case Balance(balance: Long) =>
      log.info(s"Balance $balance")
      context.system.scheduler.scheduleOnce(4.seconds, sender, RollRequest)
    case RollResponse(screen, win) =>
      log.info(s"RollResponse ${win}!")
      bonusGame(win)
    case DoubleResponse(win) =>
      log.info(s"DoubleResponse ${win}!")
      bonusGame(win)
  }

  private def bonusGame(win: Long): Unit = {
    if (win>0) {
      if (Random.nextBoolean()) {
        log.info(s"GoToDoubleRequest....")
        sender ! GoToDoubleRequest
        if (Random.nextBoolean()) {
          sender ! DoubleRequest(Card.Black)
        } else {
          sender ! DoubleRequest(Card.Red)
        }
      } else {
        sender ! TakeWinRequest
      }
    }
  }
}

class Supervisor extends Actor with ActorLogging {

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

class Player(uuid: UUID, initBalance: Long = 0, randomGenerator: RandomGenerator = DefaultRandomGenerator) extends Actor with ActorLogging {

  val RollCost = 1
  var balance: Long = initBalance
  val InitState: Receive = {
    case NewGameRequest(userId, name) =>
      if (balance <= 0) {
        sender ! NewGameResponse(Failure, Some(ZeroBalance))
        self ! PoisonPill
      } else {
        log.info(s"New player created $name (id: $userId)")
        sender ! NewGameResponse(Success)
        sender ! Balance(balance)
        context.become(RollState)
      }
  }
  val RollState: Receive = {
    case RollRequest =>
      log.info(s"RollState with ${balance}")
      if (balance < RollCost) {
        context.self ! PoisonPill
      } else {
        balance = balance - RollCost

        val screen = getScreen

        if (isWin(screen)) {
          val win = getWin(screen)
          sender ! RollResponse(screen, win)
          context.become(takeWinOrGoToDouble(win))
        } else {
          sender ! RollResponse(screen, 0)
          sender ! Balance(balance)
        }
      }
  }

  def takeWinOrGoToDouble(win: Long, step: Int = 5): Receive = {
    case TakeWinRequest =>
      log.info(s"TakeWinRequest!")
      takeWin(win)
    case GoToDoubleRequest =>
      log.info(s"GoToDoubleRequest!")
      context.become(takeDouble(win, step))
  }

  def takeWin(win: Long): Unit = {
    log.info(s"takeWin ${win}!")
    balance = balance + win
    sender ! Balance(balance)
    context.become(RollState)
  }

  def takeDouble(win: Long, step: Int): Receive = {
    case DoubleRequest(card) =>
      log.info(s"takeDouble ${win} and ${step}!")
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

  override def unhandled(message: Any): Unit = {
    sender ! IllegalRequest
    self ! PoisonPill
  }

  private def getScreen: Seq[Seq[Int]] = {
    Seq.fill(3)(Seq.fill(5)(Random.nextInt(9)))
  }

  private def isWin(screen: Seq[Seq[Int]]): Boolean = {
    randomGenerator.win
  }

  private def isWin(card: Card): Boolean = {
    Random.nextBoolean() && Random.nextBoolean()
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
