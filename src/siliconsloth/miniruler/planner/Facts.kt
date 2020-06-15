package siliconsloth.miniruler.planner

data class Precondition(val step: Step, val variable: Variable<*>)
data class UnfulfilledPrecondition(val precondition: Precondition)
data class Link(val setter: Step, val precondition: Precondition)