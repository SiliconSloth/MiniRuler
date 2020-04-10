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
}