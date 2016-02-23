package task

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Future

/**
  * Minimal solution
  */
object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val service = new WeatherService()

  val route =
    get {
        path("forecast" / Rest) { city =>
          complete(service.getWeek(city))
        } ~
        path("weather" / Rest) { city =>
          complete(service.getDay(city))
        } ~
        path(Rest) { _ =>
          complete {
            <html>
              <body>Use:
                <ul>
                  <li>weather/&lt;city&gt; - current weather</li>
                  <li>forecast/&lt;city&gt; - week forecast</li>
                </ul>
              </body>
            </html>
          }
        }
    }

  Http().bindAndHandle(route, "localhost", 8080)
}

class WeatherService(implicit system: ActorSystem, materializer: ActorMaterializer) {
  private val appId = "0e455fb9e50ed423e0f4e87ddf4f639e"

  def getDay(city: String): Future[HttpResponse] = get("weather", city)

  def getWeek(city: String): Future[HttpResponse] = get("forecast/daily", city)

  private def get(request: String, city: String): Future[HttpResponse] =
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = s"http://api.openweathermap.org/data/2.5/$request?q=$city&appid=$appId&mode=xml&units=metric&cnt=7"
      )
    )
}
