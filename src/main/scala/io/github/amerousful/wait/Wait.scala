package io.github.amerousful.wait

import io.gatling.http.check.HttpCheck
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Wait {
  def apply() = new WaitBuilder(new Wait)
}

private class Wait {
  var request: HttpRequestBuilder = _
  var attempts: Int = _
  var pauseDuration: FiniteDuration = 0 seconds
  var failOn: Option[ConditionWait] = None
  var terminalCheck: Seq[HttpCheck] = _
  var conditionToCheckSeq: Seq[ConditionWait] = _

  override def toString: String = {
    val result = new mutable.StringBuilder
    val newLine = java.lang.System.lineSeparator()
    result.append(this.getClass.getName)
    result.append(" Object {")
    result.append(newLine)
    //determine fields declared in this class only (no fields of superclass)
    val fields = this.getClass.getDeclaredFields
    //print field names paired with their values
    for (field <- fields) {
      result.append("  ")
      try {
        result.append(field.getName)
        result.append(": ")
        //requires access to private field:
        result.append(field.get(this))
      } catch {
        case ex: IllegalAccessException =>
          System.out.println(ex)
      }
      result.append(newLine)
    }
    result.append("}")
    result.toString
  }
}

