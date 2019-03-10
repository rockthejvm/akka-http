package part2_lowlevelserver

import akka.pattern.ask
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import part2_lowlevelserver.GuitarDB.{CreateGuitar, FindAllGuitars, GuitarCreated}

import scala.concurrent.Future
import scala.concurrent.duration._

// step 1
import spray.json._

case class Guitar(make: String, model: String)

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case object FindAllGuitars
}

class GuitarDB extends Actor with ActorLogging {
  import GuitarDB._

  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId: Int = 0

  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars")
      sender() ! guitars.values.toList

    case FindGuitar(id) =>
      log.info(s"Searching guitar by id: $id")
      sender() ! guitars.get(id)

    case CreateGuitar(guitar) =>
      log.info(s"Adding guitar $guitar with id $currentGuitarId")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1
  }
}

// step 2
trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  // step 3
  implicit val guitarFormat = jsonFormat2(Guitar)
}

object LowLevelRest extends App with GuitarStoreJsonProtocol {

  implicit val system = ActorSystem("LowLevelRest")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  /*
    - GET on localhost:8080/api/guitar => ALL the guitars in the store
    POST on localhost:8080/api/guitar => insert the guitar into the store
   */

  // JSON -> marshalling
  val simpleGuitar = Guitar("Fender", "Stratocaster")
  println(simpleGuitar.toJson.prettyPrint)

  // unmarshalling
  val simpleGuitarJsonString =
    """
      |{
      |  "make": "Fender",
      |  "model": "Stratocaster"
      |}
    """.stripMargin
  println(simpleGuitarJsonString.parseJson.convertTo[Guitar])

}
