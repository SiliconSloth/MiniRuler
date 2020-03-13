package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.memoryRules() {
    rule {
        val camera by find<CameraLocation>()
        val sighting by find<TileSighting> { frame == camera.frame }
        fire {
            insert(TileMemory(sighting.tile, camera.x + sighting.x, camera.y + sighting.y))
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val sighting by find<EntitySighting> { frame == camera.frame }
        fire {
            insert(EntityMemory(sighting.entity, camera.x + sighting.x, camera.y + sighting.y, sighting.facing))
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val memory by find<TileMemory>(screenFilter({camera.x}, {camera.y}))
        not<TileSighting>(EqualityFilter { TileSighting(memory.tile, memory.x - camera.x, memory.y - camera.y, camera.frame) })
        fire {
            delete(memory)
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val memory by find<EntityMemory>(screenFilter({camera.x}, {camera.y}))
        not<EntitySighting>(EqualityFilter { EntitySighting(memory.entity, memory.x - camera.x, memory.y - camera.y, memory.facing, camera.frame) })
        fire {
            delete(memory)
        }
    }

    rule {
        find<MenuOpen> { menu == Menu.TITLE }
        val memory by find<TileMemory>()

        fire {
            delete(memory)
        }
    }

    rule {
        find<MenuOpen> { menu == Menu.TITLE }
        val memory by find<EntityMemory>()

        fire {
            delete(memory)
        }
    }
}