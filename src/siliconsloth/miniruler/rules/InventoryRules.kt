package siliconsloth.miniruler.rules

import siliconsloth.miniruler.InventoryItem
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.inventoryRules() {
    rule {
        val item by find<InventoryItem>()

        fire {
            println(item)
        }
    }
}