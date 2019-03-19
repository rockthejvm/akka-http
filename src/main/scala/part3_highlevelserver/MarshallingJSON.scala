package part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

case class Player(nickname: String, characterClass: String, level: Int)

object GameAreaMap {
  case object GetAllPlayers
  case class GetPlayer(nickname: String)
  case class GetPlayersByClass(characterClass: String)
  case class AddPlayer(player: Player)
  case class RemovePlayer(player: Player)
  case object OperationSuccess
}

class GameAreaMap extends Actor with ActorLogging {
  import GameAreaMap._

  var players = Map[String, Player]()

  override def receive: Receive = {
    case GetAllPlayers =>
      log.info("Getting all players")
      sender() ! players.values.toList

    case GetPlayer(nickname) =>
      log.info(s"Getting player with nickname $nickname")
      sender() ! players.get(nickname)

    case GetPlayersByClass(characterClass) =>
      log.info(s"Getting all players with the character class $characterClass")
      sender() ! players.values.toList.filter(_.characterClass == characterClass)

    case AddPlayer(player) =>
      log.info(s"Trying to add player $player")
      players = players + (player.nickname -> player)
      sender() ! OperationSuccess

    case RemovePlayer(player) =>
      log.info(s"Trying to remove $player")
      players = players - player.nickname
      sender() ! OperationSuccess
  }
}


object MarshallingJSON extends App {

  implicit val system = ActorSystem("MarshallingJSON")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  import GameAreaMap._

  val rtjvmGameMap = system.actorOf(Props[GameAreaMap], "rockTheJVMGameAreaMap")
  val playersList = List(
    Player("martin_killz_u", "Warrior", 70),
    Player("rolandbraveheart007", "Elf", 67),
    Player("daniel_rock03", "Wizard", 30)
  )

  playersList.foreach { player =>
    rtjvmGameMap ! AddPlayer(player)
  }

  /*
    - GET /api/player, returns all the players in the map, as JSON
    - GET /api/player/(nickname), returns the player with the given nickname (as JSON)
    - GET /api/player?nickname=X, does the same
    - GET /api/player/class/(charClass), returns all the players with the given character class
    - POST /api/player with JSON payload, adds the player to the map
    - (Exercise) DELETE /api/player with JSON payload, removes the player from the map
   */

  val rtjvmGameRouteSkel =
    pathPrefix("api" / "player") {
      get {
        path("class" / Segment) { characterClass =>
          // TODO 1: get all the players with characterClass
          reject
        } ~
        (path(Segment) | parameter('nickname)) { nickname =>
          // TODO 2: get the player with the nickname
          reject
        } ~
        pathEndOrSingleSlash {
          // TODO 3: get ALL the players
          reject
        }
      } ~
      post {
        // TODO 4: add a player
        reject
      } ~
      delete {
        // TODO 5 (exercise): delete a player
        reject
      }
    }

}
