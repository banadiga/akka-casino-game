package io.github.lvivscalaclub

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.japi.Option.Some
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import io.github.lvivscalaclub.server.{DefaultRandomGenerator, Player, RandomGenerator}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

class PlayerSpec
  extends TestKit(ActorSystem("slog-game"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Player Action" - {

    def createPlayer(balance: Long, generator: RandomGenerator = DefaultRandomGenerator) = {
      val props = Props(new Player(UUID.randomUUID(), initBalance = balance, randomGenerator = generator))
      system.actorOf(props, UUID.randomUUID().toString)
    }

    "InitState " - {

      "press NewGameRequest" in {
        val player = createPlayer(20)
        player ! NewGameRequest(UUID.randomUUID(), "Ihor")

        expectMsg(NewGameResponse(status = Success))
        expectMsg(Balance(20))
        expectNoMessage()
      }

      "press NewGameRequest with zero balance" in {
        val player = createPlayer(0)
        player ! NewGameRequest(UUID.randomUUID(), "Ihor")

        watch(player)

        expectMsg(NewGameResponse(status = Failure, error = Some(ZeroBalance)))
        expectTerminated(player)
        expectNoMessage()
      }

      "press NewGameRequest two times and get IllegalRequest" in {
        val player = createPlayer(20)
        player ! NewGameRequest(UUID.randomUUID(), "Ihor")

        expectMsg(NewGameResponse(status = Success))
        expectMsg(Balance(20))
        expectNoMessage()

        watch(player)

        player ! NewGameRequest(UUID.randomUUID(), "Ihor")
        expectMsg(IllegalRequest)
        expectTerminated(player)
        expectNoMessage()
      }
    }

    "RollState" - {

      def goToRoll(userId: UUID, player: ActorRef): Unit = {
        player ! NewGameRequest(userId, "Ihor")

        expectMsg(NewGameResponse(status = Success))
        expectMsgType[Balance]
        expectNoMessage()
      }

      "press RollResponse with win" in {
        val generator = new RandomGenerator {
          override def win: Boolean = true
        }

        val player = createPlayer(20, generator)
        val userId = UUID.randomUUID()

        goToRoll(userId, player)

        player ! RollRequest

        expectMsgType[RollResponse]
        expectNoMessage()
      }

      "press RollResponse with not win" in {
        val generator = new RandomGenerator {
          override def win: Boolean = false
        }

        val player = createPlayer(20, generator)
        val userId = UUID.randomUUID()

        goToRoll(userId, player)

        player ! RollRequest

        expectMsgType[RollResponse]
        expectMsg(Balance(19))
        expectNoMessage()

      }
    }
  }

  "An Echo actor" - {
    "send back messages unchanged" in {
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }
  }
}