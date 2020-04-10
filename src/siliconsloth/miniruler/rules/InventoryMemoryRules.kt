package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter

fun RuleEngine.inventoryMemoryRules() {
    // When the inventory is open ensure the memorized counts are accurate.
    rule {
        val inv by find<InventoryItem>()
        val mem by find<InventoryMemory> { item == inv.item && count != inv.count }

        fire {
            replace(mem, InventoryMemory(inv.item, inv.count))
        }
    }

    // Set counts to zero for items not visible in the inventory.
    rule {
        find<InventorySelection>()  // Indicates that menu is open
        val mem by find<InventoryMemory> { count > 0 }
        not<InventoryItem> { item == mem.item }

        fire {
            replace(mem, InventoryMemory(mem.item, 0))
        }
    }

    // Erase memory of inventory when the game is restarted.
    rule {
        find<MenuOpen> { menu == Menu.TITLE }
        val memory by find<InventoryMemory>()

        fire {
            delete(memory)
        }
    }

    // Initialize memories for every item type that reflect the player's starting inventory.
    rule {
        not<MenuOpen> { menu == Menu.TITLE }

        fire {
            atomic {
                Item.values().forEach {
                    if (it == Item.WORKBENCH || it == Item.POWER_GLOVE) {
                        insert(InventoryMemory(it, 1))
                    } else {
                        insert(InventoryMemory(it, 0))
                    }
                }
            }
        }
    }

    // When an item disappears near the player, assume it was picked up and so increment the inventory count.
    rule {
        val camera by find<CameraLocation>()
        val player by find<Memory> { entity == Entity.PLAYER }
        // Only detect items that have stopped moving; moving items will "disappear" frequently without being picked up.
        val stat by find<StationaryItem> { item.pos.distanceSquared(player.pos) < 100 && camera.frame - since > 40 }
        not(EqualityFilter { stat.item } )
        val inv by find<InventoryMemory> { item == stat.item.item!! }

        fire {
            replace(inv, InventoryMemory(inv.item, inv.count + 1))
        }
    }
}