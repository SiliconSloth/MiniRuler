package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter

fun RuleEngine.inventoryMemoryRules() {
    // When the inventory is open ensure the memorized counts are accurate.
    rule {
        find<MenuOpen> { menu == Menu.INVENTORY }
        val inv by find<ListItem>()
        val mem by find<InventoryMemory> { item == inv.item && (lower != inv.count || upper != inv.count) }

        fire {
            replace(mem, InventoryMemory(inv.item, inv.count, inv.count))
        }
    }

    // Set counts to zero for items not visible in the inventory.
    rule {
        find<MenuOpen> { menu == Menu.INVENTORY }
        val mem by find<InventoryMemory> { lower > 0 }
        not<ListItem> { item == mem.item }

        delay = 3   // Wait for the item list to load

        fire {
            replace(mem, InventoryMemory(mem.item, 0, 0))
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
                        insert(InventoryMemory(it, 1, 1))
                    } else {
                        insert(InventoryMemory(it, 0, 0))
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
        val stat by find<StationaryItem> { item.intersects(player) && camera.frame - since > 40 }
        not(EqualityFilter { stat.item } )
        val inv by find<InventoryMemory> { item == stat.item.item!! }

        fire {
            delete(stat)
            replace(inv, InventoryMemory(inv.item, inv.lower, inv.upper + 2))
        }
    }

    // When an item other than the power glove stops being held,
    // assume it was placed or consumed and decrement its inventory count.
    // If the item was actually un-held by the inventory being opened, the inventory count will immediately be corrected
    // by the open inventory.
    rule {
        val held by find<HeldItem> { item != Item.POWER_GLOVE }
        val memory by find<InventoryMemory>{ item == held.item }

        end {
            // Ensure the match ended due to the item no longer being held, not the inventory count changing.
            if (exists(EqualityFilter { memory })) {
                replace(memory, InventoryMemory(memory.item, memory.lower - 1, memory.upper - 1))
            }
        }
    }

    // When the Have indicator is visible in a crafting menu, update the inventory count to match it.
    rule {
        val have by find<HaveIndicator>()
        val memory by find<InventoryMemory> { item == have.item && (lower != have.count || upper != have.count) }

        fire {
            replace(memory, InventoryMemory(memory.item, have.count, have.count))
        }
    }

    // When something is picked up with the power glove, add it to the inventory.
    rule {
        val held by find<HeldItem>()
        find<LastHeldItem> { item == Item.POWER_GLOVE }

        fire {
            val memory = all<InventoryMemory> { item == held.item }.iterator().next()
            replace(memory, InventoryMemory(memory.item, memory.lower + 1, memory.upper + 1))
        }
    }

    // Check the inventory when the agent suspects that the item count may have reached the target.
    rule {
        not<MenuOpen>()
        val target by find<ResourceTarget>()
        find<InventoryMemory> { item == target.item && lower < target.count && upper >= target.count }

        fire {
            maintain((KeyPress(Key.MENU)))
        }
    }
}