package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.planner.Planner
import siliconsloth.miniruler.planner.SingleValue
import siliconsloth.miniruler.planner.State

fun RuleEngine.planningRules(planner: Planner) {
    rule {
        val inv by find<InventoryMemory> { item == Item.WOOD }

        fire {
            maintain(VariableValue(WOOD_COUNT, inv.count))
        }
    }

    rule {
        val inv by find<InventoryMemory> { item == Item.WOOD_PICKAXE }

        fire {
            maintain(VariableValue(PICK_COUNT, inv.count))
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

    // If the agent is aware of any workbenches existing anywhere in the world,
    // that counts as "next to".
    rule {
        val benches by all<Memory> { entity == Entity.WORKBENCH }

        fire {
            if (benches.any()) {
                maintain(VariableValue(NEXT_TO, Entity.WORKBENCH))
            } else {
                maintain(VariableValue(NEXT_TO, null))
            }
        }
    }

    rule {
        val varValues by all<VariableValue>()

        fire {
            val state = State(varValues.map { it.variable to SingleValue(it.value) }.toMap())
            val action = planner.chooseAction(state)
            println(action)
            action?.let { maintain(CurrentAction(it)) }
        }
    }
}