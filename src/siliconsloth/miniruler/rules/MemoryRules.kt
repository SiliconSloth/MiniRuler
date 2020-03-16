package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.memoryRules() {
    rule {
        val camera by find<CameraLocation>()
        val sighting by find<TileSighting> { frame == camera.frame }
        fire {
            insert(TileMemory(sighting.tile, camera.pos + sighting.pos))
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val sighting by find<EntitySighting> { frame == camera.frame }
        fire {
            insert(EntityMemory(sighting.entity, camera.pos + sighting.pos, sighting.facing))
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val memory by find<TileMemory>(screenFilter({camera.pos}))
        not(EqualityFilter { TileSighting(memory.tile, memory.pos - camera.pos, camera.frame) })
        fire {
            delete(memory)
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val memory by find<EntityMemory>(screenFilter({camera.pos}))
        not(EqualityFilter { EntitySighting(memory.entity, memory.pos - camera.pos, memory.facing, camera.frame) })
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

    rule {
        val camera by find<CameraLocation>()
        val sighting by find<EntitySighting> { entity == Entity.ITEM }

        fire {
            val memory = EntityMemory(sighting.entity, camera.pos + sighting.pos, sighting.facing)
            if (exists(EqualityFilter { memory })) {
                insert(StationaryItem(memory, camera.frame))
            }
        }
    }

    rule {
        val stat by find<StationaryItem>()
        not(EqualityFilter { stat.item } )

        fire {
            delete(stat)
        }
    }
}