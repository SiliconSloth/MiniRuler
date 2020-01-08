package siliconsloth.miniruler

import org.kie.api.runtime.KieSession
import org.kie.api.runtime.rule.FactHandle

abstract class Fact

abstract class Perception: Fact()
data class MenuOpen(var menu: Menu): Perception()
data class TitleSelection(var option: TitleOption): Perception()
data class CameraLocation(var x: Int, var y: Int, var frame: Int): Perception()

abstract class Sighting(var x: Int, var y: Int, var frame: Int): Perception()
class TileSighting(var tile: Tile, x: Int, y: Int, frame: Int): Sighting(x, y, frame)
class EntitySighting(var entity: Entity, x: Int, y: Int, frame: Int): Sighting(x, y, frame)

abstract class Memory: Fact()
abstract class SightingMemory(var x: Int, var y: Int, var frame: Int): Memory()
class TileMemory(var tile: Tile, x: Int, y: Int, frame: Int): SightingMemory(x, y, frame)
class EntityMemory(var entity: Entity, x: Int, y: Int, frame: Int): SightingMemory(x, y, frame)

abstract class Action: Fact()
class KeyPress(val key: Key): Action()