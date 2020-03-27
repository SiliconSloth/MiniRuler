package siliconsloth.miniruler.rules

import siliconsloth.miniruler.InventoryItem
import siliconsloth.miniruler.InventoryMemory
import siliconsloth.miniruler.InventorySelection
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.inventoryRules() {
    rule {
        find<InventorySelection>()
        val item by find<InventoryItem>()

        fire {
            insert(InventoryMemory(item.item, item.count))
        }
    }
    rule {
        find<InventorySelection>()
        val mem by find<InventoryMemory>()
        not<InventoryItem> { item == mem.item && count == mem.count }

        fire {
            delete(mem)
        }
    }

    rule {
        val items by all<InventoryMemory>()

        fire {
            println(items)
        }
    }
}