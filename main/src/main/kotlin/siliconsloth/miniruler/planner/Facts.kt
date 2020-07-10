package siliconsloth.miniruler.planner

data class Precondition(val step: Step, val variable: Variable<*>)
data class UnfulfilledPrecondition(val precondition: Precondition)
data class Link(val setter: Step, val precondition: Precondition)
data class Ordering(val before: Step, val after: Step)
data class PossibleOrdering(val before: Step, val after: Step)
data class Conflict(val link: Link, val threat: Step)
data class PreconditionBatch(val preconditions: List<Precondition>, val fulfillmentAction: Action,
                             val aggregator: (List<Domain<*>>) -> Domain<*>, val strictCandidates: Boolean)