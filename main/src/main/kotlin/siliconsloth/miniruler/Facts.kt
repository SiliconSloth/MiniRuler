package siliconsloth.miniruler

import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import siliconsloth.miniruler.planner.Action
import siliconsloth.miniruler.planner.Variable

interface Fact

interface Spatial {
    val pos: Vector
}

interface Perception: Fact

data class MenuOpen(val menu: Menu): Perception
data class TitleSelection(val option: TitleOption): Perception

data class ListItem(val item: Item, val count: Int, val position: Int): Perception
data class ListSelection(val position: Int): Perception
data class HaveIndicator(val item: Item, val count: Int): Perception

data class CameraLocation(val pos: Vector, val frame: Int): Perception
data class StaminaLevel(val stamina: Int): Perception
data class HeldItem(val item: Item): Perception
data class LastHeldItem(val item: Item) : Perception

data class InventoryMemory(val item: Item, val lower: Int, val upper: Int): Fact
data class Sighting(val entity: Entity, override val pos: Vector, val facing: Direction, val item: Item?, val frame: Int): Perception, Spatial
data class Memory(val entity: Entity, override val pos: Vector, val facing: Direction, val item: Item?): Spatial {
    fun intersects(other: Memory): Boolean =
            Box(pos, pos, entity.r).intersects(Box(other.pos, other.pos, other.entity.r))
}

data class KeyRequest(val key: Key)
data class GuardedKeyRequest(val key: Key)
data class GuardedKeyPress(val key: Key)
data class KeyPress(val key: Key)

data class MoveRequest(val direction: Direction)
class JiggleRequest()
data class KeySpam(val key: Key)
class CraftPress()

interface Target: Fact {
    val target: Memory
}

data class PossibleTarget(override val target: Memory): Target
data class Waypoint(override val pos: Vector): Spatial
data class MoveTarget(override val target: Memory): Target
data class FaceRequest(val direction: Direction)

data class StationaryItem(val item: Memory, val since: Int): Fact

data class VariableValue(val variable: Variable<*>, val value: Any?)
data class CurrentAction(val action: Action)
data class ResourceTarget(val item: Item, val count: Int)