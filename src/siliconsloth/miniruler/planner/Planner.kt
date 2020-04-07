package siliconsloth.miniruler.planner

class Planner(goal: State, val actions: List<Action>) {
    data class ActionProposal(val action: Action?, val cost: Int)
    data class StateAndCost(val state: State, val cost: Int)

    val chosenActions = mutableMapOf(goal to ActionProposal(null, 0))
    val frontier = mutableListOf(StateAndCost(goal, 0))

    fun chooseAction(state: State): Action? =
            (chosenActions.filter { it.key.supersetOf(state) }.values.minBy { it.cost } ?: searchTo(state)).action

    fun searchTo(target: State): ActionProposal {
        var requested: ActionProposal? = null
        while (requested == null && frontier.any()) {
            val current = frontier.removeAt(0)
            actions.forEach { act ->
                val before = act.unapply(current.state)
                if (before.isValid() && !chosenActions.keys.any { it.supersetOf(before) }) {
                    chosenActions[before] = ActionProposal(act, current.cost + 1)
                    frontier.add(StateAndCost(before, current.cost + 1))

                    if (requested == null && before.supersetOf(target)) {
                        requested = chosenActions[before]
                    }
                }
            }
        }
        return requested ?: ActionProposal(null, -1)
    }
}