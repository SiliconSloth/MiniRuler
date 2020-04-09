package siliconsloth.miniruler.planner

/**
 * Simple action planner that uses a backtracking breadth-first search to find the shortest action sequence
 * from arbitrary states to a fixed goal.
 *
 * @param goal the goal state
 * @param actions all actions that can be taken by the agent
 */
class Planner(goal: State, val actions: List<Action>) {
    // cost is length of path from current state to goal via this action.
    data class ActionProposal(val action: Action?, val cost: Int)
    // cost is length of shortest path to goal from this state.
    data class StateAndCost(val state: State, val cost: Int)

    val chosenActions = mutableMapOf(goal to ActionProposal(null, 0))
    // Used as a FIFO queue so that the lowest-cost states are always visited next.
    val frontier = mutableListOf(StateAndCost(goal, 0))

    // Returns null if already at goal or the goal is unreachable from given state.
    fun chooseAction(state: State): Action? =
            (chosenActions.filter { it.key.supersetOf(state) }.values.minBy { it.cost } ?: searchTo(state)).action

    // Continue the breadth-first search until (a superset of) the given state is visited.
    // Returns the chosen action for that state, or a null action if the search terminates without reaching the state.
    private fun searchTo(target: State): ActionProposal {
        var result: ActionProposal? = null
        while (result == null && frontier.any()) {
            val current = frontier.removeAt(0)
            // Try unapplying every action to see if it ends up in a valid, novel state.
            // If the resulting state is a subset of an already visited state, we can discard this action edge;
            // in a BFS if a superstate has already been visited then a shorter (or equal) path has already been found.
            actions.forEach { act ->
                val before = act.unapply(current.state)
                if (before.isValid() && !chosenActions.keys.any { it.supersetOf(before) }) {
                    // This is the first path to reach this state, so it is the shortest.
                    chosenActions[before] = ActionProposal(act, current.cost + 1)
                    frontier.add(StateAndCost(before, current.cost + 1))

                    if (result == null && before.supersetOf(target)) {
                        result = chosenActions[before]
                    }
                }
            }
        }
        return result ?: ActionProposal(null, -1)
    }
}