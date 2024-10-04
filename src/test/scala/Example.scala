import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.github.amerousful.wait._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class Example extends Simulation {

  // It's a mock that returns a random value. 4 - processing, 1 - failed, 1 - completed
  // {
  // "status": "{{oneOf  'processing' 'processing' 'processing' 'processing'  'failed' 'completed'}}"
  //}
  val protocol: Protocol = http
    .baseUrl("https://gatling-wait.free.beeceptor.com")

  val getProcess = http("Get process")
    .get("/status")
    .check(
      jsonPath("$.status") saveAs "status"
    )

  // It will wait for the status 'completed' for 5 attempts, with a 1-second pause between each attempt. It will fail if the status is 'failed'.
  val waitCompleted = Wait()
    .requestForPolling(getProcess)
    .waitUntil(condition("status")(_ == "completed"))
    .failOn(condition("status")(_ == "failed"))
    .terminalCheck(jsonPath("$.status") is "completed")
    .pauseDuration(1 second)
    .attempts(5)
    .init()

  val scn: ScenarioBuilder = scenario("Wait completed")
    .exec(waitCompleted)

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(protocol)

}
