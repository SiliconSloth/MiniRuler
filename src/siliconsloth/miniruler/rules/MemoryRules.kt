package siliconsloth.miniruler.rules

import com.mojang.ld22.Game
import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.math.Vector

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
        delay = 10
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

    rule {
        val camera by find<CameraLocation>()
        val mem1 by find<Memory> { entity == Entity.PLAYER }
        val mem2 by find<Memory> { entity == Entity.PLAYER && pos != mem1.pos }

        fire {
            val center = camera.pos + Vector(Game.WIDTH/2, Game.HEIGHT/2)
            if (mem1.pos.distance(center) < mem2.pos.distance(center)) {
                delete(mem2)
            }
        }
    }
}