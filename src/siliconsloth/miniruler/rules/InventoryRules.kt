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

    // Release all keys upon opening crafting.
    rule {
        find<MenuOpen> { menu == Menu.CRAFTING }

        fire {
            Key.values().forEach { delete(KeyPress(it)) }
        }
    }

    // Select the desired item in the list.
    rule {
        val action by find<CurrentAction> { action == SELECT_WORKBENCH || action == CRAFT_PCIKAXE }
        val selection by find<ListSelection>()
        val item by find<ListItem> { item == (if (action.action == SELECT_WORKBENCH)
                                                    Item.WORKBENCH else Item.WOOD_PICKAXE) }

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