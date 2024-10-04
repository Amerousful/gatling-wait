package io.github.amerousful

package object wait {
  def condition(variable: String)(conditionToCheck: String => Boolean): ConditionWait = ConditionWait(variable, conditionToCheck)
}
