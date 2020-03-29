package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.filters.EqualityFilter
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.memoryRules() {
    rule {
        val camera by find<CameraLocation>()
        val sighting by find<Sighting> { frame == camera.frame }
        fire {
            insert(Memory(sighting.entity, camera.pos + sighting.pos, sighting.facing, sighting.item))
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val memory by find<Memory>(screenFilter {camera.pos})
        not(EqualityFilter { Sighting(memory.entity, memory.pos - camera.pos, memory.facing, memory.item, camera.frame) })
        fire {
            delete(memory)
        }
    }

    rule {
        find<MenuOpen> { menu == Menu.TITLE }
        val memory by find<Memory>()

        fire {
            delete(memory)
        }
    }

    rule {
        val camera by find<CameraLocation>()
        val sighting by find<Sighting> { entity == Entity.ITEM }
        not<StationaryItem> { item.item == sighting.item!! && item.pos == camera.pos + sighting.pos }

        fire {
            val memory = Memory(sighting.entity, camera.pos + sighting.pos, sighting.facing, sighting.item)
            insert(StationaryItem(memory, camera.frame))
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