# Gatling Wait [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.amerousful/gatling-wait/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.amerousful/gatling-wait/)
A solution built around Gatling DSL for a convenient way to wait for an event. Three key ingredients:
1) A condition to wait for
2) The number of attempts
3) Pauses between attempts

Let's say you have an application with asynchronous logic, and you need to wait for a condition to be met. My real-life example involves creating a process with three statuses: `processing`, `completed`, and `failed`.

For instance, the endpoint to get the process status is `https://application.com/status`.

Here’s a request that polls and saves the status to the variable `status`:
```scala
 val getProcess = http("Get process")
    .get("/status")
    .check(
      jsonPath("$.status") saveAs "status"
    )
```

To wait:

```scala
import io.github.amerousful.wait._

val waitCompleted = Wait()
  .requestForPolling(getProcess)
  .waitUntil(condition("status")(_ == "completed"))
  .failOn(condition("status")(_ == "failed"))
  .terminalCheck(jsonPath("$.status") is "completed")
  .pauseDuration(1 second)
  .attempts(5)
  .init()
```
This waits for the status `completed` with `5` attempts and a `1 second` pause between attempts.
*Note that the pause duration consists of the `pause` plus the response time of a polling request, so the pause duration might fluctuate.

Additionally, it will fail if the status is `failed`, as there’s no need to wait for `completed` in that case.

#### Additional info:
```scala
condition("status")(_ == "completed")
              ↑             ↑ 
              |             | 
      variable name   condition to satisfy
```
OR
```scala
condition("status")(extracted => extracted == "completed")
```

You can also declare multiple conditions:
```scala
.waitUntil(
  condition("link")(_ != "empty"),
  condition("outdated")(_ == "false"),
)

.terminalCheck(
    jsonPath("$.link") is "empty",
    jsonPath("$.state.outdated") is "true",
  )
```
Make sure to add the same conditions in both the wait and the terminal check.

`terminalCheck()` - this check will execute when all attempts are reached, and its purpose is to fail the request if the conditions are not met.
***

This plugin records the number of attempts in the `Session`. The counter is stored as `wait.attemptCounter -> {attemptNumber}`.

## Installation

### Maven:

Add to your `pom.xml`:
```xml
<dependency>
    <groupId>io.github.amerousful</groupId>
    <artifactId>gatling-wait</artifactId>
    <version>1.0</version>
</dependency>
```

### SBT

Add to your `build.sbt`:
```scala
libraryDependencies += "io.github.amerousful" % "gatling-wait" % "1.0"
```

#### Import:

```scala
import io.github.amerousful.wait._
```

***

## Logging
Add this to your logback.xml:

```xml

<logger name="io.github.amerousful.wait" level="ALL"/>
```
