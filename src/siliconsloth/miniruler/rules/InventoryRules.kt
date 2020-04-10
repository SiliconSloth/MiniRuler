package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.inventoryRules() {
    rule {
        find<CurrentAction> { action == OPEN_INVENTORY }
        not<MenuOpen>()

        fire {
            maintain(KeyPress(Key.MENU))
        }
    }

    rule {
        find<CurrentAction> { action == SELECT_WORKBENCH }
        val selection by find<InventorySelection>()
        val item by find<InventoryItem> { item == Item.WORKBENCH }

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
}