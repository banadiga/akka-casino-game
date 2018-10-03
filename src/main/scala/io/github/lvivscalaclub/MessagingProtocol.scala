package io.github.lvivscalaclub

import java.util.UUID

sealed trait MessagingProtocol

sealed trait Status

case object Success extends Status

case object Failure extends Status

sealed trait Error

case object ZeroBalance extends Error

case object InvalidUser extends Error

case object PlayerAlreadyConnected extends Error

case class NewGameRequest(userId: UUID, name: String) extends MessagingProtocol

case class NewGameResponse(status: Status, error: Option[Error] = None) extends MessagingProtocol


case class Balance(credits: Long) extends MessagingProtocol

case class RollRequest(userId: UUID) extends MessagingProtocol

case class RollResponse(screen: Seq[Seq[Int]], win: Long) extends MessagingProtocol