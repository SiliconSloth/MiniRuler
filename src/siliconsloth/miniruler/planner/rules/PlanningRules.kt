package siliconsloth.miniruler.planner.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter
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
        val link by find<Link>()

        fire {
            insert(Ordering(link.setter, link.precondition.step))
        }
    }

    rule {
        val oa by find<Ordering>()
        val ob by find<Ordering> { oa.after == before }

        fire {
            insert(Ordering(oa.before, ob.after))
        }
    }

    rule {
        val sa by find<Step>()
        val sb by find<Step> { this != sa }
        not(EqualityFilter { Ordering(sb, sa) })

        fire {
            maintain(PossibleOrdering(sa, sb))
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
    fulfillmentRule(planner, itemCount(Item.STONE), MINE_ROCK_WITH_HAND, null, CRAFT_ACTIONS[Item.ROCK_PICKAXE]!!)
    fulfillmentRule(planner, itemCount(Item.WORKBENCH), planner.initialize!!)

    fulfillmentRule(planner, nextTo(Entity.FURNACE), PLACE_ACTIONS[Item.FURNACE]!!)
    fulfillmentRule(planner, HOLDING, Select(Item.FURNACE), Item.FURNACE)

    aggregateFulfillmentRule(planner, listOf(itemCount(Item.WOOD)), CHOP_TREES)
    aggregateFulfillmentRule(planner, listOf(itemCount(Item.SAND)), DIG_SAND)
    aggregateFulfillmentRule(planner, listOf(itemCount(Item.COAL), itemCount(Item.STONE)),
            MINE_ROCK_WITH_ROCK, CRAFT_ACTIONS[Item.ROCK_PICKAXE]!!)

    multiFulfillmentRule(planner, MENU, Menu.FURNACE, OPEN_ACTIONS[Menu.FURNACE]!!)
}

fun <T> RuleEngine.fulfillmentRule(planner: RulePlanner, variable: Variable<T>,
                               action: Action, value: T? = null, condAction: Action? = null) = rule {
    val uc by find<UnfulfilledPrecondition> { precondition.variable == variable &&
            (value == null || precondition.step.before[variable] == Enumeration(value)) &&
            (condAction == null || precondition.step.action == condAction) }

    val candidates by all<PossibleOrdering> { after == uc.precondition.step && before.action == action &&
            uc.precondition.step.before[variable].supersetOf(before.after[variable]) }

    delay = 3

    fire {
        val chosen: Step
        if (candidates.any()) {
            chosen = candidates.first().before
        } else {
            chosen = planner.newStepFulfilling(action, uc.precondition)
            insert(chosen)
        }
        insert(Link(chosen, uc.precondition))
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

fun <T> RuleEngine.multiFulfillmentRule(planner: RulePlanner, variable: Variable<T>, value: T, action: Action) = rule {
    val ucs by all<UnfulfilledPrecondition> { precondition.variable == variable &&
            precondition.step.before[variable] == Enumeration(value) }
    val candidates by all<Step> { this.action == action }
    this.delay = 6

    fire {
        if (ucs.any()) {
            val stepGoal = planner.state(variable to Enumeration(value))

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