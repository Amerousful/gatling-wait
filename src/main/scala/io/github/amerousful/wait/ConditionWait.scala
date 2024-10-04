package io.github.amerousful.wait

final case class ConditionWait(variable: String, conditionToCheck: String => Boolean)
