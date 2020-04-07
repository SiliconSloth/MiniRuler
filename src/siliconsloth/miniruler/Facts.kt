package siliconsloth.miniruler

import siliconsloth.miniruler.math.Vector

interface Fact

interface Spatial {
    val pos: Vector
}

interface Perception: Fact

data class MenuOpen(val menu: Menu): Perception
data class TitleSelection(val option: TitleOption): Perception

data class InventoryItem(val item: Item, val count: Int, val position: Int): Perception
data class InventorySelection(val position: Int): Perception

data class CameraLocation(val pos: Vector, val frame: Int): Perception
data class StaminaLevel(val stamina: Int): Perception
data class HeldItem(val item: Item): Perception

data class Sighting(val entity: Entity, override val pos: Vector, val facing: Direction, val item: Item?, val frame: Int): Perception, Spatial
data class Memory(val entity: Entity, override val pos: Vector, val facing: Direction, val item: Item?): Spatial
data class InventoryMemory(val item: Item, val count: Int): Fact

interface Action: Fact
data class KeyPress(val key: Key): Action

interface Target: Fact {
    val target: Memory
}

data class PossibleTarget(override val target: Memory): Target
data class TargetProposal(override val target: Memory, val distance: Float): Target
data class MoveTarget(override val target: Memory): Target

data class StationaryItem(val item: Memory, val since: Int): Fact