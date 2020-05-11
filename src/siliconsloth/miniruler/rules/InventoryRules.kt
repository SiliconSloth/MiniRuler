package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.inventoryRules() {
    // If trying to open the inventory, first turn away from any nearby workbenches.
    faceClear({
        find<CurrentAction> { action == OPEN_INVENTORY }
        not<MenuOpen>()
    }, { obstacle, player ->
        obstacle.entity == Entity.WORKBENCH && aimingAt(player, obstacle)
    }, {
        maintain(GuardedKeyRequest(Key.MENU))
    })

    // Select the desired item in the list.
    rule {
        val action by find<CurrentAction> { action is Select || action == CRAFT_PCIKAXE }
        val selection by find<ListSelection>()
        val item by find<ListItem> { item == (if (action.action is Select)
                                                    (action.action as Select).item else Item.WOOD_PICKAXE) }

        fire {
            if (item.position < selection.position) {
                maintain(KeyRequest(Key.UP))
            } else if (item.position > selection.position) {
                maintain(KeyRequest(Key.DOWN))
            } else {
                maintain(GuardedKeyRequest(Key.ATTACK))
            }
        }
    }

    rule {
        find<CurrentAction> { action == CLOSE_INVENTORY }
        find<MenuOpen> { menu == Menu.INVENTORY }

        fire {
            maintain(GuardedKeyRequest(Key.MENU))
        }
    }

    rule {
        find<CurrentAction> { action == CLOSE_CRAFTING }
        find<MenuOpen> { menu == Menu.CRAFTING }

        fire {
            maintain(GuardedKeyRequest(Key.MENU))
        }
    }
}