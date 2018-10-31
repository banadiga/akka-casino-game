package io.github.lvivscalaclub.server

import java.util.UUID

import akka.actor.{Actor, ActorLogging, PoisonPill}
import io.github.lvivscalaclub._
import io.github.lvivscalaclub.models.Card.Card

import scala.util.Random

class Player(uuid: UUID, initBalance: Long = 20, randomGenerator: RandomGenerator = DefaultRandomGenerator) extends Actor with ActorLogging {

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

trait RandomGenerator {
  def win: Boolean
}

object DefaultRandomGenerator extends RandomGenerator {
  override def win: Boolean = Random.nextBoolean()
}