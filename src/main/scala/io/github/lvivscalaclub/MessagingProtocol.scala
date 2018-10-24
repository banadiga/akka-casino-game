package io.github.lvivscalaclub

import java.util.UUID

import io.github.lvivscalaclub.Card.Card

import scala.util.Random


sealed trait Status

case object Success extends Status

case object Failure extends Status


sealed trait Error

case object ZeroBalance extends Error

case object InvalidUser extends Error
case object IllegalRequest extends Error

case object PlayerAlreadyConnected extends Error


sealed trait MessagingProtocol


case class NewGameRequest(userId: UUID, name: String) extends MessagingProtocol

case class NewGameResponse(status: Status, error: Option[Error] = None) extends MessagingProtocol


case class Balance(credits: Long) extends MessagingProtocol


case object RollRequest extends MessagingProtocol

case class RollResponse(screen: Seq[Seq[Int]], win: Long) extends MessagingProtocol

case object TakeWinRequest extends MessagingProtocol
case object GoToDoubleRequest extends MessagingProtocol

object Card extends Enumeration {
  type Card = Value
  val Black:Card = Value
  val Red:Card = Value
}

case class DoubleRequest(card: Card) extends MessagingProtocol
case class DoubleResponse(win: Long) extends MessagingProtocol


trait RandomGenerator {
  def win: Boolean
}

object DefaultRandomGenerator extends RandomGenerator {
  override def win: Boolean = Random.nextBoolean()
}