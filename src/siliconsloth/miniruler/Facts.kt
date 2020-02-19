package siliconsloth.miniruler

interface Fact

interface Perception: Fact
data class MenuOpen(val menu: Menu): Perception
data class TitleSelection(val option: TitleOption): Perception
data class CameraLocation(val x: Int, val y: Int, val frame: Int): Perception

interface Sighting: Perception {
    val x: Int
    val y: Int
    val frame: Int
}
data class TileSighting(val tile: Tile, override val x: Int, override val y: Int, override val frame: Int): Sighting
data class EntitySighting(val entity: Entity, override val x: Int, override val y: Int, override val frame: Int): Sighting

interface Memory: Fact
interface SpatialMemory: Memory {
    val x: Int
    val y: Int
}
data class TileMemory(val tile: Tile, override val x: Int, override val y: Int): SpatialMemory
data class EntityMemory(val entity: Entity, override val x: Int, override val y: Int): SpatialMemory

interface Action: Fact
data class KeyPress(val key: Key): Action