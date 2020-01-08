package siliconsloth.miniruler

import com.mojang.ld22.Game
import org.kie.api.event.rule.ObjectDeletedEvent
import org.kie.api.event.rule.ObjectInsertedEvent
import org.kie.api.event.rule.ObjectUpdatedEvent
import org.kie.api.event.rule.RuleRuntimeEventListener
import org.kie.api.runtime.KieSession

class SpatialMemoryStore(val kSession: KieSession): RuleRuntimeEventListener {
    val BLOCK_SIZE = 64
    val MARGIN = 3

    val memories = mutableMapOf<Pair<Int, Int>, MutableSet<SightingMemory>>()
    val loadedMemories = mutableSetOf<SightingMemory>()

    fun loadMemories(cameraX: Int, cameraY: Int) {
        synchronized(this) {
            val nearby = (-MARGIN..Game.WIDTH/BLOCK_SIZE + MARGIN/BLOCK_SIZE).map { x ->
                (-MARGIN..Game.HEIGHT/BLOCK_SIZE + MARGIN/BLOCK_SIZE).map { y ->
                    memories.getOrDefault(Pair(cameraX/BLOCK_SIZE + x, cameraY/BLOCK_SIZE + y), mutableSetOf())
                }.flatten()
            }.flatten()

//            println("> " + loadedMemories.size)
//            println("- " + loadedMemories.subtract(nearby).size)
//            println("+ " + nearby.subtract(loadedMemories).size)
            loadedMemories.subtract(nearby).forEach { kSession.deleteIfPresent(it) }
            nearby.subtract(loadedMemories).forEach { kSession.insert(it) }
        }
    }

    fun insert(memory: SightingMemory) {
        synchronized(this) {
            println(memories.values.map { it.size }.sum())
            println(kSession.factCount)
            val coords = Pair(memory.x/BLOCK_SIZE, memory.y/BLOCK_SIZE)
            memories.put(coords, memories.getOrDefault(coords, mutableSetOf())
                    .plus(memory).toMutableSet())
        }
    }

    fun retract(memory: SightingMemory) {
        synchronized(this) {
            val coords = Pair(memory.x/BLOCK_SIZE, memory.y/BLOCK_SIZE)
            memories.get(coords)?.remove(memory)
        }
    }

    override fun objectInserted(event: ObjectInsertedEvent) {
        (event.`object` as? CameraLocation)?.let {
            loadMemories(it.x, it.y)
        }

        (event.`object` as? SightingMemory)?.let {
            loadedMemories.add(it)
//            synchronized(this) {
//                memories.put(Pair(it.x, it.y), memories.getOrDefault(Pair(it.x, it.y), mutableSetOf())
//                        .plus(it).toMutableSet())
//            }
        }
    }

    override fun objectDeleted(event: ObjectDeletedEvent) {
        (event.oldObject as? SightingMemory)?.let {
            loadedMemories.remove(it)
//            synchronized(this) {
//                memories.get(Pair(it.x, it.y))?.remove(it)
//            }
        }
    }

    override fun objectUpdated(event: ObjectUpdatedEvent) {
        (event.`object` as? CameraLocation)?.let {
            loadMemories(it.x, it.y)
        }

//        (event.oldObject as? SightingMemory)?.let {
//            synchronized(this) {
//                memories.get(Pair(it.x, it.y))?.remove(it)
//            }
//        }

//        (event.`object` as? SightingMemory)?.let {
//            synchronized(this) {
//                memories.put(Pair(it.x, it.y), memories.getOrDefault(Pair(it.x, it.y), mutableSetOf())
//                        .plus(it).toMutableSet())
//            }
//        }
    }
}