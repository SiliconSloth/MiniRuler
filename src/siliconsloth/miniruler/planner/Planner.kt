package siliconsloth.miniruler.planner

import siliconsloth.miniruler.*

/**
 * Simple action planner that uses a backtracking breadth-first search to find the shortest action sequence
 * from arbitrary states to a fixed goal.
 *
 * @param goal the goal state
 * @param actions all actions that can be taken by the agent
 */
class Planner(val goal: State, val actions: List<Action>) {
    // cost is length of path from current state to goal via this action.
    data class ActionProposal(val action: Action?, val cost: Int, val resourceTargets: List<ResourceTarget>,
            val source: State?)
    // cost is length of shortest path to goal from this state.
    data class StateAndCost(val state: State, val cost: Int)

    val chosenActions = mutableMapOf(goal to ActionProposal(null, 0, listOf(), goal))
    // Used as a FIFO queue so that the lowest-cost states are always visited next.
    val frontier = mutableListOf(StateAndCost(goal, 0))
    val finalized = mutableSetOf<State>()

    // Returns null if already at goal or the goal is unreachable from given state.
    fun chooseAction(state: State): ActionProposal =
            chosenActions.filter { it.key.supersetOf(state) && it.key in finalized }.values.minBy { it.cost } ?: searchTo(state)

    // Continue the breadth-first search until (a superset of) the given state is visited.
    // Returns the chosen action for that state, or a null action if the search terminates without reaching the state.
    private fun searchTo(target: State): ActionProposal {
        var result: ActionProposal? = null
        var step = 0
        while (result == null && frontier.any()) {
            val current = frontier.minBy { it.cost }!!
            finalized.add(current.state)
            if (current.state.supersetOf(target)) {
                result = chosenActions[current.state]
                break
            }
            frontier.remove(current)
            if (step % 1000 == 0) {
                println(current.cost)
            }
            step++

            // Try unapplying every action to see if it ends up in a valid, novel state.
            // If the resulting state is a subset of an already visited state, we can discard this action edge;
            // in a BFS if a superstate has already been visited then a shorter (or equal) path has already been found.
            actions.forEach { act ->
                val before = act.unapply(current.state)
                if (before.isValid() && !finalized.any { it.supersetOf(before) }) {
                    val actCost = current.cost + act.cost(before, current.state)
                    val cheapest = chosenActions.filter { it.key.supersetOf(before) }.values.map { it.cost }.min()
                    if (cheapest?.let { it > actCost } != false) {
                        chosenActions[before] = ActionProposal(act, actCost, act.resourceTarget(before, current.state),
                                current.state)
                        frontier.add(StateAndCost(before, actCost))
                    }
                }
            }
        }
        return result ?: ActionProposal(null, -1, listOf(), null)
    }

    fun printPlan(start: State) {
        var current: State? = start
        while (current != null && !goal.supersetOf(current)) {
            val prop = chosenActions.filter { it.key.supersetOf(current!!) && it.key in finalized }.values
                    .minBy { it.cost } ?: searchTo(current)
            println(prop.action)
            current = prop.source
        }
    }
}