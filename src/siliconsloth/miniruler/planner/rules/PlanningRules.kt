package siliconsloth.miniruler.planner.rules

import siliconsloth.miniruler.CRAFT_ACTIONS
import siliconsloth.miniruler.DIG_SAND
import siliconsloth.miniruler.Item
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.itemCount
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

    rule {
        val ucs by all<UnfulfilledPrecondition> { precondition.variable == itemCount(Item.SAND) }
        delay = 6

        fire {
            if (ucs.any()) {
                val needed = ucs.map { (it.precondition.step.before[it.precondition.variable] as LowerBounded).min }.sum()
                val newStep = planner.newStep(DIG_SAND, planner.state(itemCount(Item.SAND) to LowerBounded(needed)))
                insert(newStep)
                for (uc in ucs) {
                    insert(Link(newStep, uc.precondition))
                }
            }
        }
    }
}

fun RuleEngine.fulfillmentRule(planner: RulePlanner, variable: Variable<*>, action: Action) = rule {
    val uc by find<UnfulfilledPrecondition> { precondition.variable == variable }

    fire {
        val newStep = planner.newStepFulfilling(action, uc.precondition)
        insert(newStep)
        insert(Link(newStep, uc.precondition))
    }
}