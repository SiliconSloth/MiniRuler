package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.memoryRules() {
    // Memorize all visible tiles and entities.
    rule {
        val camera by find<CameraLocation>()
        val sighting by find<Sighting> { frame == camera.frame }
        fire {
            insert(Memory(sighting.entity, camera.pos + sighting.pos, sighting.facing, sighting.item))
        }
    }

    // Delete memories that are within the boundaries of the screen, but do not have the corresponding entity present.
    rule {
        val camera by find<CameraLocation>()
        val memory by find<Memory>(screenFilter {camera.pos})
        not(EqualityFilter { Sighting(memory.entity, memory.pos - camera.pos, memory.facing, memory.item, camera.frame) })
        fire {
            delete(memory)
        }
    }

    // Clear all memories when the game restarts.
    rule {
        find<MenuOpen> { menu == Menu.TITLE }
        val memory by find<Memory>()

        fire {
            delete(memory)
        }
    }

    // If a dropped item does not have a corresponding StationaryItem yet,
    // create one marking the current frame as the frame the item appeared at its current location.
    rule {
        val camera by find<CameraLocation>()
        val memory by find<Memory> { entity == Entity.ITEM }
        not<StationaryItem> { item.item == memory.item!! && item.pos == memory.pos }

        fire {
            insert(StationaryItem(memory, camera.frame))
        }
    }

    // When a dropped item disappears delete the corresponding StationaryItem.
    rule {
        val stat by find<StationaryItem>()
        not(EqualityFilter { stat.item } )

        fire {
            delete(stat)
        }
    }

    rule {
        val item by find<HeldItem>()

        end {
            insert(LastHeldItem(item.item))
        }
    }

    // Only remember the last held item for a short period of time.
    rule {
        val item by find<LastHeldItem>()
        delay = 3

        fire {
            delete(item)
        }
    }
}