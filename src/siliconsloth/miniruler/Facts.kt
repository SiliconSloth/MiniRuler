package siliconsloth.miniruler

import siliconsloth.miniruler.math.Vector

interface Fact

interface Perception: Fact
data class MenuOpen(val menu: Menu): Perception
data class TitleSelection(val option: TitleOption): Perception
data class CameraLocation(val pos: Vector, val frame: Int): Perception
data class StaminaLevel(val stamina: Int): Perception

interface Spatial {
    val pos: Vector
}

interface Sighting: Perception, Spatial {
    val frame: Int
}
data class TileSighting(val tile: Tile, override val pos: Vector, override val frame: Int): Sighting
data class EntitySighting(val entity: Entity, override val pos: Vector, val facing: Direction, override val frame: Int): Sighting

interface Memory: Fact
data class TileMemory(val tile: Tile, override val pos: Vector): Memory, Spatial
data class EntityMemory(val entity: Entity, override val pos: Vector, val facing: Direction): Memory, Spatial

interface Action: Fact
data class KeyPress(val key: Key): Action

data class MoveTarget(val target: Spatial): Fact
data class StationaryItem(val item: EntityMemory, val since: Int): Fact