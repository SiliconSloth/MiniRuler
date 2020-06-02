package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.planner.Enumeration
import siliconsloth.miniruler.planner.PartialOrderPlanner
import siliconsloth.miniruler.planner.Planner
import kotlin.system.exitProcess

fun RuleEngine.planningRules(planner: Planner) {
    rule {
        val inv by find<InventoryMemory>()

        fire {
            maintain(VariableValue(itemCount(inv.item), inv.lower))
        }
    }

    rule {
        val menu by find<MenuOpen>()

        fire {
            maintain(VariableValue(MENU, menu.menu))
        }
    }

    rule {
        not<MenuOpen>()

        fire {
            maintain(VariableValue(MENU, null))
        }
    }

    rule {
        val item by find<HeldItem>()

        fire {
            maintain(VariableValue(HOLDING, item.item))
        }
    }

    rule {
        not<HeldItem>()

        fire {
            maintain(VariableValue(HOLDING, null))
        }
    }

    // If the agent is aware of any instance of an entity existing anywhere in the world,
    // that counts as "next to".
    Entity.values().forEach { e ->
        rule {
            find<Memory> { entity == e }

            fire {
                maintain(VariableValue(nextTo(e), true))
            }
        }

        rule {
            not<Memory> { entity == e }

            fire {
                maintain(VariableValue(nextTo(e), false))
            }
        }
    }

    rule {
        val varValues by all<VariableValue>()
        delay = 6   // Allow time for all values to be updated

        fire {
            val state = state(varValues.map { it.variable to Enumeration(it.value) }.toMap())
            PartialOrderPlanner(planner.goal, planner.actions).run(state)
            exitProcess(0)
            val action = planner.chooseAction(state)

            action.action?.let { maintain(CurrentAction(it)) }
            action.resourceTargets.forEach { maintain(it) }

            println(action.action)
            println(action.cost)
            println(action.resourceTargets)
            println(varValues.filter { it.value != 0 && it.value != false })
            planner.printPlan(state)
        }
    }
}