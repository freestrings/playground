package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BootLoadSimulation extends Simulation {

  val baseUrl = System.getProperty("TARGET_URL")
  val sim_users = System.getProperty("SIM_USERS").toInt

  val httpConf = http.baseURL(baseUrl)

  val loadTestPage = repeat(30) {
    exec(http("-")
      .get("/")
      .body(StringBody("ok")))
      .pause(1 second, 2 seconds)
  }

  val scn = scenario("Just Load Test").exec(loadTestPage)

  setUp(scn.inject(rampUsers(sim_users).over(30 seconds)).protocols(httpConf))
}
