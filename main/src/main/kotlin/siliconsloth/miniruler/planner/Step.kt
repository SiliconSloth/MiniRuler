package siliconsloth.miniruler.planner

data class Step(val before: State, val action: Action, val after: State, val id: Int) {
    override fun toString(): String =
            "$action#$id"
}