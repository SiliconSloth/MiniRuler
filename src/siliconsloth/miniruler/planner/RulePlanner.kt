package siliconsloth.miniruler.planner

import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.planner.rules.planningRules

class RulePlanner(val engine: RuleEngine, val variables: Array<Variable<*>>, val goal: State) {
    var nextId = 0

    var initialize: Action? = null
    val finalize = Action("FINALIZE", goal, mapOf())

    fun run(start: State) {
        initialize = Action("INITIALIZE", state(),
                start.map { (v,d) -> (d as? Enumeration)?.let { v to SetTo((it.values.first())) } }
                        .filterNotNull().toMap())

        engine.planningRules(this)
        engine.atomic {
            insert(Step(state(), initialize!!, start, nextId++))
            insert(Step(goal, finalize, state(), nextId++))
        }

        while (engine.allMatches.any { it.state == CompleteMatch.State.MATCHED || it.state == CompleteMatch.State.DROPPED }) {
            engine.insert("Dummy")
            engine.delete("Dummy")
        }
    }

    fun state(domains: Map<Variable<*>, Domain<*>>): State =
            State(variables, domains)

    fun state(vararg domains: Pair<Variable<*>, Domain<*>>): State =
            state(mapOf(*domains))

    fun newStep(before: State, action: Action, after: State): Step {
        return Step(before, action, after, nextId++)
    }

    fun newStep(action: Action, stepGoal: State): Step {
        val stepBefore = action.unapply(stepGoal)
        val stepAfter = action.apply(stepBefore).intersect(stepGoal)
        return newStep(stepBefore, action, stepAfter)
    }

    fun newStepFulfilling(action: Action, precondition: Precondition): Step {
        val stepGoal = state(precondition.variable to precondition.step.before[precondition.variable])
        return newStep(action, stepGoal)
    }
}