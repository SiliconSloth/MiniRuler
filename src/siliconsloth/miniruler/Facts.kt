package siliconsloth.miniruler

interface Fact

interface Perception: Fact
data class MenuOpen(val menu: Menu): Perception
data class TitleSelection(val option: TitleOption): Perception
data class CameraLocation(val x: Int, val y: Int, val frame: Int): Perception

interface Spatial {
    val x: Int
    val y: Int
}

interface Sighting: Perception, Spatial {
    val frame: Int
}
data class TileSighting(val tile: Tile, override val x: Int, override val y: Int, override val frame: Int): Sighting
data class EntitySighting(val entity: Entity, override val x: Int, override val y: Int, override val frame: Int): Sighting

interface Memory: Fact
data class TileMemory(val tile: Tile, override val x: Int, override val y: Int): Memory, Spatial
data class EntityMemory(val entity: Entity, override val x: Int, override val y: Int): Memory, Spatial

interface Action: Fact
data class KeyPress(val key: Key): Action

data class KeyProposal(val key: Key, val strength: Float, val cause: TileMemory): Fact
data class KeyDesire(val key: Key, val strength: Float): Fact