package part4_client

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success, Try}
import spray.json._

object HostLevel extends App with PaymentJsonProtocol {

  implicit val system = ActorSystem("HostLevel")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val poolFlow = Http().cachedHostConnectionPool[Int]("www.google.com")

  Source(1 to 10)
    .map(i => (HttpRequest(), i))
    .via(poolFlow)
    .map {
      case (Success(response), value) =>
        // VERY IMPORTANT
        response.discardEntityBytes()
        s"Request $value has received response: $response"
      case (Failure(ex), value) =>
        s"Request $value has failed: $ex"
    }
//    .runWith(Sink.foreach[String](println))


  import PaymentSystemDomain._
  val creditCards = List(
    CreditCard("4242-4242-4242-4242", "424", "tx-test-account"),
    CreditCard("1234-1234-1234-1234", "123", "tx-daniels-account"),
    CreditCard("1234-1234-4321-4321", "321", "my-awesome-account")
  )

  val paymentRequests = creditCards.map(creditCard => PaymentRequest(creditCard, "rtjvm-store-account", 99))
  val serverHttpRequests = paymentRequests.map(paymentRequest =>
    (
      HttpRequest(
        HttpMethods.POST,
        uri = Uri("/api/payments"),
        entity = HttpEntity(
          ContentTypes.`application/json`,
          paymentRequest.toJson.prettyPrint
        )
      ),
      UUID.randomUUID().toString
    )
  )

  Source(serverHttpRequests)
    .via(Http().cachedHostConnectionPool[String]("localhost", 8080))
    .runForeach { // (Try[HttpResponse], String)
      case (Success(response@HttpResponse(StatusCodes.Forbidden, _, _, _)), orderId) =>
        println(s"The order ID $orderId was not allowed to proceed: $response")
      case (Success(response), orderId) =>
        println(s"The order ID $orderId was successful and returned the response: $response")
        // do something with the order ID: dispatch it, send a notification to the customer, etc
      case (Failure(ex), orderId) =>
        println(s"The order ID $orderId could not be completed: $ex")
    }

  // high-volume, low-latency requests
}
