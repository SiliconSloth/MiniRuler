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
                    maintain(Precondition(step, v))
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
        not(EqualityFilter { link.setter })

        fire {
            delete(link)
        }
    }

    rule {
        val link by find<Link>()
        not(EqualityFilter { link.precondition })
        delay = 2

        fire {
            delete(link)
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
        val link by find<Link>()
        val threat by find<Step> { this != link.setter && this != link.precondition.step &&
                action[link.precondition.variable] != null &&
                @Suppress("UNCHECKED_CAST")
                !(link.precondition.step.before[link.precondition.variable] as Domain<Any?>)
                        .supersetOf(after[link.precondition.variable]) }
        find(EqualityFilter { Ordering(link.setter, threat) })
        find(EqualityFilter { Ordering(threat, link.precondition.step) })

        fire {
            maintain(Conflict(link, threat))
        }
    }

    rule {
        val conflicts by all<Conflict> { link.precondition.variable.let {
                it == itemCount(Item.SAND) || it == itemCount(Item.COAL) || it == itemCount(Item.WOOD) } }
        val links by all<Link>()
        delay = 6

        fire {
            val conflict = conflicts.firstOrNull() ?: return@fire

            val setter = conflict.link.setter
            val threat = conflict.threat
            val dependent = conflict.link.precondition.step
            val variable = conflict.link.precondition.variable

            val stepGoal = threat.after.intersect(planner.state(variable to dependent.before[variable]))
            val newStep = planner.newStep(threat.action, stepGoal)

            replace(threat, newStep)

            insert(Link(setter, Precondition(newStep, variable)))
            insert(Link(newStep, Precondition(dependent, variable)))

            links.filter { it.setter == threat }.forEach { replace(it, Link(newStep, it.precondition)) }

            links.filter { it.precondition.step == threat }.forEach { replace(it,
                    Link(it.setter, Precondition(newStep, it.precondition.variable))) }
        }
    }

    rule {
        val conflict by find<Conflict> { link.precondition.variable == itemCount(Item.FURNACE) &&
                threat.action == PLACE_ACTIONS[Item.FURNACE] }

        fire {
            val pickupStep = planner.newStep(PICK_UP_ACTIONS[Entity.FURNACE]!!, state())
            insert(pickupStep)

            delete(conflict.link)
            insert(Link(conflict.threat, Precondition(pickupStep, nextTo(Entity.FURNACE))))
            insert(Link(pickupStep, conflict.link.precondition))
        }
    }

    rule {
        val conflict by find<Conflict>()
        find(EqualityFilter { Ordering(conflict.link.precondition.step, conflict.threat) })

        fire {
            error("Bad conflict $conflict")
        }
    }

    rule {
        val oa by find<Ordering>()
        find(EqualityFilter { Ordering(oa.after, oa.before) })

        fire {
            error("Bad ordering $oa")
        }
    }

    rule {
        val unfulfilled by all<UnfulfilledPrecondition>()
        delay = 30

        fire {
            println(unfulfilled)
        }
    }

    rule {
        val conflicts by all<Conflict>()
        delay = 30

        fire {
            println(conflicts)
        }
    }

    fulfillmentRule(planner, variablePredicate(itemCount(Item.GLASS)), CRAFT_ACTIONS[Item.GLASS]!!)
    fulfillmentRule(planner, variablePredicate(itemCount(Item.FURNACE)), CRAFT_ACTIONS[Item.FURNACE]!!)
    fulfillmentRule(planner, variablePredicate(itemCount(Item.ROCK_PICKAXE)), CRAFT_ACTIONS[Item.ROCK_PICKAXE]!!)
    fulfillmentRule(planner, variablePredicate(itemCount(Item.ROCK_SHOVEL)), CRAFT_ACTIONS[Item.ROCK_SHOVEL]!!)
    fulfillmentRule(planner, { it.variable == itemCount(Item.STONE) &&
            it.step.action == CRAFT_ACTIONS[Item.ROCK_PICKAXE]!! }, MINE_ROCK_WITH_HAND)
    fulfillmentRule(planner, variablePredicate(itemCount(Item.WORKBENCH)), planner.initialize!!)
    fulfillmentRule(planner, variablePredicate(itemCount(Item.POWER_GLOVE)), planner.initialize!!)

    fulfillmentRule(planner, variablePredicate(nextTo(Entity.FURNACE)), PLACE_ACTIONS[Item.FURNACE]!!)
    fulfillmentRule(planner, variablePredicate(HOLDING, Item.FURNACE), Select(Item.FURNACE))
    fulfillmentRule(planner, variablePredicate(HOLDING, Item.ROCK_PICKAXE), Select(Item.ROCK_PICKAXE))
    fulfillmentRule(planner, variablePredicate(HOLDING, Item.ROCK_SHOVEL), Select(Item.ROCK_SHOVEL))
    fulfillmentRule(planner, variablePredicate(HOLDING, Item.POWER_GLOVE), Select(Item.POWER_GLOVE))

    aggregateFulfillmentRule(planner, variablePredicate(itemCount(Item.WOOD)), CHOP_TREES)
    aggregateFulfillmentRule(planner, variablePredicate(itemCount(Item.SAND)), DIG_SAND)
    aggregateFulfillmentRule(planner, { it.variable in listOf(itemCount(Item.COAL), itemCount(Item.STONE)) &&
            it.step.action != CRAFT_ACTIONS[Item.ROCK_PICKAXE]!! }, MINE_ROCK_WITH_ROCK)

    multiFulfillmentRule(planner, variablePredicate(MENU, null), planner.initialize!!)
    multiFulfillmentRule(planner, variablePredicate(MENU, Menu.FURNACE), OPEN_ACTIONS[Menu.FURNACE]!!)
}

fun variablePredicate(variable: Variable<*>): (Precondition) -> Boolean = {
    it.variable == variable
}

fun <T> variablePredicate(variable: Variable<T>, value: T): (Precondition) -> Boolean = {
    it.variable == variable && it.step.before[variable].supersetOf(Enumeration(value))
}

fun RuleEngine.fulfillmentRule(planner: RulePlanner, preconditionPredicate: (Precondition) -> Boolean, action: Action) = rule {
    val uc by find<UnfulfilledPrecondition> { preconditionPredicate(precondition) }

    @Suppress("UNCHECKED_CAST")
    val candidates by all<PossibleOrdering> { after == uc.precondition.step && before.action == action &&
            (uc.precondition.step.before[uc.precondition.variable] as Domain<Any?>).supersetOf(before.after[uc.precondition.variable]) }

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

fun RuleEngine.aggregateFulfillmentRule(planner: RulePlanner, preconditionPredicate: (Precondition) -> Boolean, action: Action) = rule {
    val ucs by all<UnfulfilledPrecondition> { preconditionPredicate(precondition) }
    @Suppress("UNCHECKED_CAST")
    val candidates by all<Step> { ucs.any { this.action[it.precondition.variable] !=  null &&
            (it.precondition.step.before[it.precondition.variable] as Domain<Any?>).supersetOf(this.after[it.precondition.variable]) } }
    val links by all<Link>()
    this.delay = 10

    fire {
        if (ucs.any()) {
            val candidate = candidates.firstOrNull()
            val needed = ucs.groupBy { it.precondition.variable }.mapValues { (v,us) ->
                LowerBounded(us.map { (it.precondition.step.before[v] as LowerBounded).min }.sum() +
                        ((candidate?.after?.get(v) as? LowerBounded)?.min ?: 0)) }
            val stepGoal = planner.state(needed)

            val newStep = if (candidate != null) {
                delete(candidate)
                planner.newStep(candidate.before, candidate.action, candidate.after.intersect(stepGoal))
            } else {
                planner.newStep(action, stepGoal)
            }

            if (candidate != null) {
                for (link in links) {
                    if (link.setter == candidate) {
                        replace(link, Link(newStep, link.precondition))
                    }
                    if (link.precondition.step == candidate) {
                        replace(link, Link(link.setter, Precondition(newStep, link.precondition.variable)))
                    }
                }
            }

            insert(newStep)
            for (uc in ucs) {
                insert(Link(newStep, uc.precondition))
            }
        }
    }
}

fun RuleEngine.multiFulfillmentRule(planner: RulePlanner, preconditionPredicate: (Precondition) -> Boolean, action: Action) = rule {
    val ucs by all<UnfulfilledPrecondition> { preconditionPredicate(precondition) }
    @Suppress("UNCHECKED_CAST")
    val candidates by all<Step> { ucs.any { this.action[it.precondition.variable] !=  null &&
            (it.precondition.step.before[it.precondition.variable] as Domain<Any?>).supersetOf(this.after[it.precondition.variable]) } }
    val orderings by all<Ordering>()
    this.delay = 10

    fire {
        if (ucs.any()) {
            val fulfiller = if (candidates.any()) {
                candidates.first { c -> !orderings.any { it.after == c && it.before in candidates }  }
            } else {
                val stepGoal = planner.state(ucs.groupBy { it.precondition.variable }.mapValues { (v,us) ->
                    us.first().precondition.step.before[v] })

                planner.newStep(action, stepGoal).also { insert(it) }
            }

            for (uc in ucs) {
                insert(Link(fulfiller, uc.precondition))
            }
        }
    }
}