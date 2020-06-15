package siliconsloth.miniruler.planner.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.planner.*

fun RuleEngine.planningRules(planner: RulePlanner) {
    rule {
        val step by find<Step>()

        fire {
            step.before.forEach { (v,d) ->
                if (d != v.initializeDomain()) {
                    insert(Precondition(step, v))
                }
            }
        }
    }

    rule {
        val cond by find<Precondition>()
        not<Link> { precondition == cond }

        fire {
            maintain(UnfulfilledPrecondition(cond))
        }
    }

    rule {
        val unfulfilled by all<UnfulfilledPrecondition>()

        fire {
            println(unfulfilled)
        }
    }

    fulfillmentRule(planner, itemCount(Item.GLASS), CRAFT_ACTIONS[Item.GLASS]!!)
    fulfillmentRule(planner, itemCount(Item.FURNACE), CRAFT_ACTIONS[Item.FURNACE]!!)
    fulfillmentRule(planner, itemCount(Item.ROCK_PICKAXE), CRAFT_ACTIONS[Item.ROCK_PICKAXE]!!)

    aggregateFulfillmentRule(planner, listOf(itemCount(Item.SAND)), DIG_SAND)
    aggregateFulfillmentRule(planner, listOf(itemCount(Item.COAL), itemCount(Item.STONE)),
            MINE_ROCK_WITH_ROCK, CRAFT_ACTIONS[Item.ROCK_PICKAXE]!!)
}

fun RuleEngine.fulfillmentRule(planner: RulePlanner, variable: Variable<*>, action: Action) = rule {
    val uc by find<UnfulfilledPrecondition> { precondition.variable == variable }

    fire {
        val newStep = planner.newStepFulfilling(action, uc.precondition)
        insert(newStep)
        insert(Link(newStep, uc.precondition))
    }
}

fun RuleEngine.aggregateFulfillmentRule(planner: RulePlanner, variables: List<Variable<*>>, action: Action,
                                        blacklist: Action? = null) = rule {
    val ucs by all<UnfulfilledPrecondition> { precondition.variable in variables && precondition.step.action != blacklist }
    val candidates by all<Step> { this.action == action }
    this.delay = 6

    fire {
        if (ucs.any()) {
            val needed = ucs.groupBy { it.precondition.variable }.mapValues { (v,us) ->
                LowerBounded(us.map { (it.precondition.step.before[v] as LowerBounded).min }.sum()) }
            val stepGoal = planner.state(needed)

            val newStep = if (candidates.any()) {
                candidates.first().let {
                    delete(it)
                    planner.newStep(it.before, it.action, it.after.intersect(stepGoal))
                }
            } else {
                planner.newStep(action, stepGoal)
            }

            insert(newStep)
            for (uc in ucs) {
                insert(Link(newStep, uc.precondition))
            }
        }
    }
}