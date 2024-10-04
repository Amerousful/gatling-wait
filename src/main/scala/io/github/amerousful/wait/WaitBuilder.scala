package io.github.amerousful.wait

import com.typesafe.scalalogging.LazyLogging
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.check.HttpCheck
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class WaitBuilder(wait: Wait) extends LazyLogging {

  def requestForPolling(request: HttpRequestBuilder): WaitBuilder = {
    wait.request = request
    this
  }

  def attempts(attempts: Int): WaitBuilder = {
    wait.attempts = attempts
    this
  }

  def pauseDuration(pause: FiniteDuration): WaitBuilder = {
    wait.pauseDuration = pause
    this
  }

  def failOn(failOn: ConditionWait): WaitBuilder = {
    wait.failOn = Some(failOn)
    this
  }

  def waitUntil(conditionWaits: ConditionWait*): WaitBuilder = {
    wait.conditionToCheckSeq = conditionWaits.toSeq
    this
  }

  def terminalCheck(check: HttpCheck*): WaitBuilder = {
    wait.terminalCheck = check
    this
  }

  private val incrementCounter: Expression[Session] = (session: Session) => {
    val attemptCounter = session("wait.attemptCounter").as[Int] + 1
    session.set("wait.attemptCounter", attemptCounter)
  }

  private val flushCounter: Expression[Session] = (session: Session) => {
    session.set("wait.attemptCounter", 0)
  }

  private val allConditions = (session: Session) => wait.conditionToCheckSeq.map { cw =>
    val variableFromSession = session(cw.variable).as[String]
    val result: Boolean = cw.conditionToCheck(variableFromSession)
    logger.debug(s"Condition for variable '${cw.variable}' is [$result]")
    result
  }

  private val loopCondition: Expression[Boolean] = (session: Session) => {
    val allConditionsResults = allConditions(session)

    if (allConditionsResults.contains(false)) {
      logger.debug("Not all conditions occurred")
    } else {
      val numAttempt = session("wait.attemptCounter").as[Int]
      val pauseDuration: FiniteDuration = wait.pauseDuration

      logger.debug(s"Finally, all conditions are true. $numAttempt attempts or ${numAttempt * pauseDuration}")
    }

    allConditionsResults.contains(false) && session("wait.attemptCounter").as[Int] < wait.attempts
  }

  private val checkAttempts: Session => Boolean = (session: Session) => {
    val outOfAttempts = session("wait.attemptCounter").as[Int] >= wait.attempts
    if (outOfAttempts) logger.trace("[Terminal] All retry attempts have been exhausted.")
    outOfAttempts
  }

  private val endCondition: Expression[Boolean] = (session: Session) => {
    checkAttempts(session) || {
      wait.failOn match {
        case Some(cw) =>
          val variableFromSession = session(cw.variable).asOption[String]
          variableFromSession match {
            case Some(value) =>
              val result: Boolean = cw.conditionToCheck(value)
              if (result) logger.error(s"Failure Condition for Variable'${cw.variable}'")
              result
            case None => false
          }
        case None => false
      }
    }
  }

  def debug(): WaitBuilder = {
    logger.debug(wait.toString)
    this
  }

  private val pauseCondition: Expression[FiniteDuration] = (session: Session) => {
    // Add a pause only if the condition wasn't met.
    // It's unnecessary to add pause in the end.

    val allTrue = !allConditions(session).contains(false)
    if (allTrue) 0 seconds
    else {
      logger.debug(s"Pause... ${wait.pauseDuration}")
      wait.pauseDuration
    }
  }

  def init(): ChainBuilder = {
    exec(flushCounter)
      .doWhile(loopCondition) {
        exec(incrementCounter)
          .exec(wait.request
            .checkIf(endCondition)(wait.terminalCheck: _*)
          ).exitHereIfFailed
          .pause(pauseCondition)
      }
      .exec(flushCounter)
  }

}
