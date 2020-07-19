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
        val ordering by find<Ordering>()
        not(EqualityFilter { ordering.before })

        fire {
            delete(ordering)
        }
    }

    rule {
        val ordering by find<Ordering>()
        not(EqualityFilter { ordering.after })

        fire {
            delete(ordering)
        }
    }

    rule {
        val batches by all<PreconditionBatch>()

        @Suppress("UNCHECKED_CAST")
        val candidates by all<Step> { batches.any() &&
            !batches.first().preconditions.any { it.step == this } && batches.first().preconditions.any {
                this.action[it.variable] != null &&
                        ((it.step.before[it.variable] as Domain<Any?>).supersetOf(this.after[it.variable]) ||
                                this.action[it.variable] is AddArbitrary)
            } && (!batches.first().strictCandidates || action == batches.first().fulfillmentAction)
        }
        val links by all<Link>()
        val orderings by all<Ordering>()
        delay = 15

        fire {
            if (batches.any()) {
                val batch = batches.first()
                val beforeCands = candidates.filter { c -> !orderings.any { o -> o.after == c && batch.preconditions.any { it.step == o.before } } }
                val candidate = beforeCands.firstOrNull { c -> !orderings.any { o -> o.before == c && o.after in beforeCands } }

                val needed = batch.preconditions.groupBy { it.variable }.mapValues { (v, us) ->
                    batch.aggregator(
                            us.map { it.step.before[v] }.let { domains ->
                                if (candidate == null) {
                                    domains
                                } else {
                                    domains + candidate.after[v]
                                }
                            }
                    )
                }
                val stepGoal = planner.state(needed)

                val fulfiller = if (candidate != null) {
                    if (stepGoal.supersetOf(candidate.after)) {
                        candidate
                    } else {
                        delete(candidate)
                        planner.newStep(candidate.before, candidate.action, candidate.after.intersect(stepGoal))
                    }
                } else {
                    planner.newStep(batch.fulfillmentAction, stepGoal)
                }

                if (fulfiller != candidate) {
                    if (candidate != null) {
                        for (link in links) {
                            if (link.setter == candidate) {
                                replace(link, Link(fulfiller, link.precondition))
                            }
                            if (link.precondition.step == candidate) {
                                replace(link, Link(link.setter, Precondition(fulfiller, link.precondition.variable)))
                            }
                        }
                    }

                    insert(fulfiller)
                }

                for (uc in batch.preconditions) {
                    insert(Link(fulfiller, uc))
                }
            }
        }
    }

    rule {
        val link by find<Link>()
        val threat by find<Step> { this != link.setter && this != link.precondition.step &&
                action[link.precondition.variable] != null &&
                @Suppress("UNCHECKED_CAST")
                !(link.precondition.step.before[link.precondition.variable] as Domain<Any?>)
                        .supersetOf(after[link.precondition.variable]) }
        not(EqualityFilter { Ordering(link.precondition.step, threat) })
        not(EqualityFilter { Ordering(threat, link.setter) })

        fire {
            maintain(PossibleConflict(link, threat))
        }
    }

    rule {
        val conflict by find<PossibleConflict>()
        find(EqualityFilter { Ordering(conflict.link.setter, conflict.threat) })
        find(EqualityFilter { Ordering(conflict.threat, conflict.link.precondition.step) })

        fire {
            maintain(Conflict(conflict.link, conflict.threat))
        }
    }

    rule {
        val conflicts by all<Conflict> { link.precondition.variable.let {
                it == itemCount(Item.SAND) || it == itemCount(Item.COAL) || it == itemCount(Item.WOOD) ||
                it == itemCount(Item.STONE)} }
        val links by all<Link>()
        delay = 6

        fire {
            val conflict = conflicts.firstOrNull() ?: return@fire

            val threat = conflict.threat
            val dependent = conflict.link.precondition.step
            val variable = conflict.link.precondition.variable

            val stepGoal = threat.after.intersect(planner.state(variable to dependent.before[variable]))
            val newStep = planner.newStep(threat.action, stepGoal)

            replace(threat, newStep)

            delete(conflict.link)
            // Don't insert link from setter to newStep, since the threat will already have a link for this variable.
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
        val conflicts by all<Conflict> { link.setter.action is Select && link.precondition.variable == HOLDING }

        fire {
            if (conflicts.any()) {
                val conflict = conflicts.first()
                val selectStep = conflict.link.setter
                val replacement = planner.newStep(selectStep.before, selectStep.action, selectStep.after)
                replace(selectStep, replacement)
                replace(conflict.link, Link(replacement, conflict.link.precondition))
                insert(Ordering(conflict.threat, replacement))
            }
        }
    }

    rule {
        val conflicts by all<Conflict> { link.precondition.variable == MENU && link.precondition.step.before[MENU] == Enumeration<Menu?>(null) }
        val orderings by all<Ordering>()

        delay = 2

        fire {
            if (conflicts.any()) {
                val conflict = conflicts.first { c -> !orderings.any { o -> o.before == c.threat && conflicts.any { it.threat == o.after } } }
                if (conflict.threat.after[MENU] == Enumeration(Menu.INVENTORY)) {
                    delete(conflict.link)
                    val closeStep = planner.newStep(CLOSE_INVENTORY, state())
                    insert(closeStep)
                    insert(Link(conflict.threat, Precondition(closeStep, MENU)))
                    insert(Link(closeStep, conflict.link.precondition))
                }
            }
        }
    }

    rule {
        val conflicts by all<Conflict> { link.precondition.variable == MENU && link.precondition.step.before[MENU] == Enumeration(Menu.INVENTORY) }
        val orderings by all<Ordering>()

        delay = 2

        fire {
            if (conflicts.any()) {
                val conflict = conflicts.first { c -> !orderings.any { o -> o.before == c.threat && conflicts.any { it.threat == o.after } } }
                if (conflict.threat.after[MENU] == Enumeration<Menu?>(null)) {
                    delete(conflict.link)
                    val openStep = planner.newStep(OPEN_INVENTORY, state())
                    insert(openStep)
                    insert(Link(conflict.threat, Precondition(openStep, MENU)))
                    insert(Link(openStep, conflict.link.precondition))
                }
            }
        }
    }

    rule {
        val placeStep by find<Step> { action == PLACE_ACTIONS[Item.FURNACE]!! }
        val openLink by find<Link> { setter == placeStep && precondition.variable == nextTo(Entity.FURNACE) &&
                precondition.step.action == OPEN_ACTIONS[Menu.FURNACE]!! }
        val pickUpLink by find<Link> { setter == placeStep && precondition.variable == nextTo(Entity.FURNACE) &&
                precondition.step.action == PICK_UP_ACTIONS[Entity.FURNACE]!! }

        fire {
            val openStep = openLink.precondition.step
            val pickUpStep = pickUpLink.precondition.step
            val closeStep = planner.newStepFulfilling(CLOSE_CRAFTING, Precondition(pickUpStep, MENU))

            insert(closeStep)
            insert(Link(openStep, Precondition(pickUpStep, MENU)))
            insert(Link(pickUpStep, Precondition(closeStep, MENU)))
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
        val la by find<Link>()
        val lb by find<Link> { precondition == la.precondition && setter != la.setter }

        fire {
            error("Duplicate links: $la, $lb")
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
        val ordering by find<Ordering> { after.action == planner.initialize || before.action == planner.finalize }

        fire {
            error("Bad ordering $ordering")
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

    rule {
        val steps by all<Step>()

        fire {
            println(steps.groupBy { it.action }.mapValues { (_,v) -> v.size })
        }
    }

    fulfillmentRule(variablePredicate(itemCount(Item.GLASS)), CRAFT_ACTIONS[Item.GLASS]!!, ::uniformAggregator)
    fulfillmentRule(variablePredicate(itemCount(Item.FURNACE)), CRAFT_ACTIONS[Item.FURNACE]!!, ::uniformAggregator)
    fulfillmentRule(variablePredicate(itemCount(Item.ROCK_PICKAXE)), CRAFT_ACTIONS[Item.ROCK_PICKAXE]!!, ::uniformAggregator)
    fulfillmentRule(variablePredicate(itemCount(Item.ROCK_SHOVEL)), CRAFT_ACTIONS[Item.ROCK_SHOVEL]!!, ::uniformAggregator)
    fulfillmentRule({ it.variable == itemCount(Item.STONE) &&
            it.step.action == CRAFT_ACTIONS[Item.ROCK_PICKAXE]!! }, MINE_ROCK_WITH_HAND, ::uniformAggregator, strictCandidates = true)
    fulfillmentRule(variablePredicate(itemCount(Item.WORKBENCH)), planner.initialize!!, ::uniformAggregator)
    fulfillmentRule(variablePredicate(itemCount(Item.POWER_GLOVE)), planner.initialize!!, ::uniformAggregator)

    fulfillmentRule(variablePredicate(nextTo(Entity.FURNACE)), PLACE_ACTIONS[Item.FURNACE]!!, ::uniformAggregator)
    fulfillmentRule(variablePredicate(HOLDING, Item.FURNACE), Select(Item.FURNACE), ::uniformAggregator)
    fulfillmentRule(variablePredicate(HOLDING, Item.ROCK_PICKAXE), Select(Item.ROCK_PICKAXE), ::uniformAggregator)
    fulfillmentRule(variablePredicate(HOLDING, Item.ROCK_SHOVEL), Select(Item.ROCK_SHOVEL), ::uniformAggregator)
    fulfillmentRule(variablePredicate(HOLDING, Item.POWER_GLOVE), Select(Item.POWER_GLOVE), ::uniformAggregator)
    fulfillmentRule(variablePredicate(HOLDING, null), planner.initialize!!, ::uniformAggregator, strictCandidates = true)

    fulfillmentRule(variablePredicate(itemCount(Item.WOOD)), CHOP_TREES, ::summationAggregator)
    fulfillmentRule(variablePredicate(itemCount(Item.SAND)), DIG_SAND, ::summationAggregator)
    fulfillmentRule({ it.variable in listOf(itemCount(Item.COAL), itemCount(Item.STONE)) &&
            it.step.action != CRAFT_ACTIONS[Item.ROCK_PICKAXE]!! }, MINE_ROCK_WITH_ROCK, ::summationAggregator)

    fulfillmentRule(variablePredicate(MENU, null), planner.initialize!!, ::uniformAggregator, strictCandidates = true)
    fulfillmentRule(variablePredicate(MENU, Menu.FURNACE), OPEN_ACTIONS[Menu.FURNACE]!!, ::uniformAggregator)
    fulfillmentRule(variablePredicate(MENU, Menu.INVENTORY), OPEN_INVENTORY, ::uniformAggregator)
}

fun variablePredicate(variable: Variable<*>): (Precondition) -> Boolean = {
    it.variable == variable
}

fun <T> variablePredicate(variable: Variable<T>, value: T): (Precondition) -> Boolean = {
    it.variable == variable && it.step.before[variable].supersetOf(Enumeration(value))
}

fun RuleEngine.fulfillmentRule(preconditionPredicate: (Precondition) -> Boolean, action: Action,
                               aggregator: (List<Domain<*>>) -> Domain<*>, strictCandidates: Boolean = false) = rule {
    val ucs by all<UnfulfilledPrecondition> { preconditionPredicate(precondition) }

    fire {
        if (ucs.any()) {
            maintain(PreconditionBatch(ucs.map { it.precondition }, action, aggregator, strictCandidates))
        }
    }
}

fun summationAggregator(domains: List<Domain<*>>): Domain<*> =
        LowerBounded(domains.map { (it as LowerBounded).min }.sum())

fun uniformAggregator(domains: List<Domain<*>>): Domain<*> =
        domains.first()