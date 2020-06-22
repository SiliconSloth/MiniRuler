package siliconsloth.miniruler.engine

import kotlin.reflect.KClass
import siliconsloth.miniruler.engine.builders.AtomicBuilder
import siliconsloth.miniruler.engine.builders.RuleBuilder
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.stores.FactSet
import siliconsloth.miniruler.engine.stores.FactStore

class RuleEngine(val reportInterval: Int = 0, timelinePath: String? = null): FactUpdater<Any> {
    data class Update<T: Any>(val fact: T, val isInsert: Boolean, val maintain: Boolean, val producer: CompleteMatch?)

    var reportCountdown = reportInterval
    val recorder = timelinePath?.let { TimelineRecorder(it) }

    /**
     * All the rules in the engine, grouped by the fact types they bind to.
     * Note that a rule can appear more than once, if it binds to multiple types.
     * Rules with no bindings are not stored, as they fire once and never do anything again.
     */
    val rules = mutableMapOf<KClass<*>, MutableList<Rule>>()
    
    /**
     * A fact store for each type of fact present in the engine.
     * FactSets are generated by default when new fact types are added, however users can
     * add stores of other types to improve search performance in conjunction with specialized filters.
     */
    val stores = mutableMapOf<KClass<*>, FactStore<out Any>>()

    /**
     * An optional list of rule matches maintaining each fact in the engine.
     * If all the matches maintaining a fact are ended, the fact is automatically deleted.
     * Facts with no maintainers in the first place are not affected.
     */
    val maintainers = mutableMapOf<Any, MutableList<CompleteMatch>>()

    var nextMatchID = 0

    // Some variables used by applyUpdates().
    var running = false
    val updateQueue = mutableMapOf<KClass<*>, MutableList<Update<*>>>()
    val allMatches = mutableListOf<CompleteMatch>()

    inline fun <reified T: Any> addFactStore(store: FactStore<T>) {
        stores[T::class] = store
    }

    fun rule(definition: RuleBuilder.() -> Unit) {
        val callLine = Throwable().stackTrace[1]
        val rule = RuleBuilder(callLine.toString(), this).apply(definition).build()

        rule.bindings.forEach {
            rules.getOrPut(it.type) { mutableListOf() }.add(rule)
        }
    }

    fun applyUpdates(updates: Map<KClass<*>, List<Update<*>>>) {
        // All updates caused by the same firing cycle are merged into a single atomic update.
        // This helps maintain the illusion that the rules fire in parallel, as all updates they perform occur
        // simultaneously.
        updates.forEach { (type, ups) ->
            updateQueue.getOrPut(type) { mutableListOf() }.addAll(ups)
        }

        // If this update was triggered outside the engine, start a new update loop to apply the updates,
        // fire any rules that match the updated fact base, apply the updates triggered by those rules and so on.
        // If the loop was already running, meaning this update was triggered by a rule,
        // just queue the updates as above and leave the existing loop to handle it once the firing phase is over.
        if (!running) {
            running = true
            // Keep going until the rules stop triggering updates.
            while (updateQueue.isNotEmpty()) {
                // Remove any insertions of facts maintained by matches that have already ended.
                val batch = updateQueue.mapValues { it.value.filter {
                    !(it.isInsert && it.maintain && it.producer!!.state == CompleteMatch.State.ENDED) } }
                    // Remove any insertions that are immediately undone by a deletion or vice versa.
                    .mapValues { (_,ups) ->
                        ups.filter { a -> !ups.any { b -> a.fact == b.fact && a.isInsert != b.isInsert } }
                    }
                updateQueue.clear()

                if (recorder != null) {
                    batch.values.flatten().forEach { recorder.recordUpdate(it) }
                }

                // Update the fact stores.
                batch.forEach {
                    @Suppress("UNCHECKED_CAST")
                    applyUpdates(it.key as KClass<Any>, it.value as List<Update<Any>>)
                }

                // Update the match trees of every rule that binds with fact types updated by this update.
                // This process will cause new matches to be added to allMatches.
                val applicable = batch.keys.map { rules[it] ?: mutableListOf() }.flatten().distinct()
                applicable.forEach {
                    it.applyUpdates(batch)
                }

                // Decrement the countdowns of all matches and fire/end matches as needed.
                // This may cause the queuing of new fact updates.
                allMatches.forEach { it.tick() }
                // Ended matches will not perform any further actions and so can be discarded.
                allMatches.filter { it.state != CompleteMatch.State.ENDED }

                if (reportInterval > 0) {
                    if (reportCountdown > 1) {
                        reportCountdown--
                    } else {
                        reportCountdown = reportInterval

                        println("Rule Firings:")
                        println("-------------------------")
                        rules.values.flatten().forEach { rule ->
                            if (rule.fireCount > 0) {
                                println(rule.fireCount.toString().padEnd(5) + " " + rule.name)
                                rule.fireCount = 0
                            }
                        }
                    }
                }

                recorder?.tick()
            }
            running = false
        }
    }

    /**
     * Apply a list of updates of one fact type to the appropriate fact store,
     * creating a new FactSet for that type if no store exists for it yet.
     */
    private fun <T: Any> applyUpdates(type: KClass<T>, updates: List<Update<T>>) {
        @Suppress("UNCHECKED_CAST")
        val store = stores.getOrPut(type) { FactSet<T>() } as FactStore<T>
        updates.forEach {
            if (it.isInsert) {
                store.insert(it.fact)
                if (it.maintain) {
                    maintainers.getOrPut(it.fact) { mutableListOf() }.add(it.producer!!)
                }
            } else {
                store.delete(it.fact)
                maintainers.remove(it.fact)
            }
        }
    }

    fun addMatch(match: CompleteMatch) {
        allMatches.add(match)
    }

    fun nextMatchID(): Int =
            nextMatchID++

    fun atomic(updates: AtomicBuilder.() -> Unit) =
        applyUpdates(AtomicBuilder(this, null).apply(updates).updates)

    override fun insert(fact: Any) = atomic {
        insert(fact)
    }

    override fun delete(fact: Any) = atomic {
        delete(fact)
    }

    override fun replace(old: Any, new: Any) = atomic {
        replace(old, new)
    }
}