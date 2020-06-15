package siliconsloth.miniruler.planner

import siliconsloth.miniruler.DIG_SAND
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.planner.rules.planningRules
import siliconsloth.miniruler.state

val INITIALIZE = Action("INITIALIZE", state(), mapOf())
val FINALIZE = Action("FINALIZE", state(), mapOf())

class RulePlanner(val engine: RuleEngine, val variables: Array<Variable<*>>, val goal: State) {
    var nextId = 0

    fun run(start: State) {
        engine.planningRules(this)
        engine.atomic {
            insert(Step(state(), INITIALIZE, start, nextId++))
            insert(Step(goal, FINALIZE, state(), nextId++))
        }
    }

    fun state(domains: Map<Variable<*>, Domain<*>>): State =
            State(variables, domains)

    fun state(vararg domains: Pair<Variable<*>, Domain<*>>): State =
            state(mapOf(*domains))

    fun newStep(action: Action, stepGoal: State): Step {
        val stepBefore = action.unapply(stepGoal)
        val stepAfter = action.apply(stepGoal).intersect(stepGoal)
        return Step(stepBefore, action, stepAfter, nextId++)
    }

    fun newStepFulfilling(action: Action, precondition: Precondition): Step {
        val stepGoal = state(precondition.variable to precondition.step.before[precondition.variable])
        return newStep(action, stepGoal)
    }
}