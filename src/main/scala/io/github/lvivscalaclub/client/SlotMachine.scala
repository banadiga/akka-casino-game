package io.github.lvivscalaclub.client

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorSelection}
import io.github.lvivscalaclub._
import io.github.lvivscalaclub.models.Card

import scala.util.Random

class SlotMachine(supervisor: ActorSelection, name: String) extends Actor with ActorLogging {
  private val uuid: UUID = UUID.randomUUID()
  supervisor ! NewGameRequest(uuid, name)

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

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
    if (win > 0) {
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

