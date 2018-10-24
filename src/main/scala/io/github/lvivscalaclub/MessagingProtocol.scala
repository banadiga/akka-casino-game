package io.github.lvivscalaclub

import java.util.UUID

import io.github.lvivscalaclub.models.Card.Card

sealed trait MessagingProtocol

case class NewGameRequest(userId: UUID, name: String) extends MessagingProtocol

case class NewGameResponse(status: Status, error: Option[Error] = None) extends MessagingProtocol

case class Balance(credits: Long) extends MessagingProtocol

sealed trait Status

case object Success extends Status

case object Failure extends Status

sealed trait Error

case object ZeroBalance extends Error

case object PlayerAlreadyConnected extends Error

case object RollRequest extends MessagingProtocol

case class RollResponse(screen: Seq[Seq[Int]], win: Long) extends MessagingProtocol

case object TakeWinRequest extends MessagingProtocol

case object GoToDoubleRequest extends MessagingProtocol

case class DoubleRequest(card: Card) extends MessagingProtocol

case class DoubleResponse(win: Long) extends MessagingProtocol

case class IllegalRequest(request: String) extends MessagingProtocol