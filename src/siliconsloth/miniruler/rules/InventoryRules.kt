package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.inventoryRules() {
    // If trying to open the inventory, first turn away from any nearby workbenches.
    faceClear({
        find<CurrentAction> { action == OPEN_INVENTORY }
        not<MenuOpen>()
    }, {
        it.entity == Entity.WORKBENCH
    }, {
        maintain(KeyPress(Key.MENU))
    })

    // Release all keys upon opening menu.
    rule {
        find<MenuOpen>()

        fire {
            Key.values().forEach { delete(KeyPress(it)) }
        }
    }

    // Select the desired item in the list.
    rule {
        val action by find<CurrentAction> { action is Select || action == CRAFT_PCIKAXE }
        val selection by find<ListSelection>()
        val item by find<ListItem> { item == (if (action.action is Select)
                                                    (action.action as Select).item else Item.WOOD_PICKAXE) }

        fire {
            if (item.position < selection.position) {
                maintain(KeyPress(Key.UP))
            } else if (item.position > selection.position) {
                maintain(KeyPress(Key.DOWN))
            } else {
                maintain(KeyPress(Key.ATTACK))
            }
        }
    }

    rule {
        find<CurrentAction> { action == CLOSE_INVENTORY }
        find<MenuOpen> { menu == Menu.INVENTORY }

        fire {
            maintain(KeyPress(Key.MENU))
        }
    }

    rule {
        find<CurrentAction> { action == CLOSE_CRAFTING }
        find<MenuOpen> { menu == Menu.CRAFTING }

        fire {
            maintain(KeyPress(Key.MENU))
        }
    }
}