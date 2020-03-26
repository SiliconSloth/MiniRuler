package siliconsloth.miniruler.rules

import siliconsloth.miniruler.InventoryItem
import siliconsloth.miniruler.InventorySelection
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.inventoryRules() {
    rule {
        val item by find<InventoryItem>()
        val pos by find<InventorySelection>()

        fire {
            println(item)
            println(pos)
        }
    }
}