package siliconsloth.miniruler.engine.matching

import siliconsloth.miniruler.engine.FactUpdater
import siliconsloth.miniruler.engine.Rule
import siliconsloth.miniruler.engine.builders.MatchAtomicBuilder
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.bindings.Binding
import siliconsloth.miniruler.engine.filters.Filter
import siliconsloth.miniruler.engine.stores.FactStore
import java.lang.RuntimeException
import kotlin.reflect.KClass

/**
 * Represents a single assignment of values to all the bindings in a rule definition, derived from the current fact base.
 * Note that a single rule may have multiple matches simultaneously, as several sets of values in the fact base
 * may meet the bind criteria.
 *
 * A CompleteMatch instance is created as soon as a match is discovered while updating the match tree of a rule.
 * It is assumed that at time of creation the rule's bindings will have their values set to those represented
 * by the CompleteMatch instance. These values are read and saved by the CompleteMatch for later use.
 */
class CompleteMatch(rule: Rule): MatchNode(rule), FactUpdater<Any> {
    /**
     * Possible states that a match can be in.
     *
     * MATCHED - The match has been created but not yet fired.
     *           There may or may not be a countdown set until firing can occur.
     * FIRED   - The match has been fired, and its bind values still match the fact base.
     * DROPPED - The match has been fired, and its bind values no longer match the fact base.
     *           The match is currently waiting for end() to be called, possibly with a countdown.
     * ENDED   - Either both fire() and end() have been called,
     *           or the match was dropped before fire() was called, so end() was not called either.
     */
    enum class State {
        MATCHED,
        FIRED,
        DROPPED,
        ENDED
    }

    /**
     * The values assigned to each binding in the rule definition by this match.
     * At the time of creation of a CompleteMatch all the bindings' values will have been
     * set to the values represented by this match, so we can save these values to be restored later.
     */
    val bindValues = rule.bindings.map { it to it.value }.toMap()

    /**
     * List of facts inserted with maintain() by the firing (or ending) of this match.
     * These facts will be deleted automatically when this match ends, unless another match is also maintaining them.
     * The deletions occur as soon as the end clause exits.
     */
    val maintaining = mutableListOf<Any>()

    /**
     * Number of iterations of the engine loop remaining until this match can be fired or ended.
     * This countdown is reset to the rule's delay upon entering the MATCHED or DROPPED states.
     */
    var countdown = rule.delay

    /**
     * Current state of this match.
     */
    var state = State.MATCHED

    val id = rule.engine.nextMatchID()

    init {
        rule.engine.recorder?.recordMatchState(this)
        rule.engine.addMatch(this)
    }

    /**
     * Called once on every iteration of the engine loop.
     * Decrements the countdown and fires or ends the match as needed.
     */
    fun tick() {
        if (countdown > 0) {
            countdown--
        } else if (state == State.MATCHED) {
            fire()
        } else if (state == State.DROPPED) {
            end()
        }
    }

    /**
     * Called when the bind values of this match are found to no longer be supported by the fact base.
     * Queues the match to be ended during the next engine firing cycle, if the match has been fired.
     * The match will not be ended if it was never fired.
     */
    override fun drop() {
        state = when (state) {
            State.MATCHED -> State.ENDED    // Don't call end() if fire() wasn't called
            State.FIRED -> State.DROPPED    // Queue calling end() later
            else -> throw RuntimeException("Match already dropped")
        }
        rule.engine.recorder?.recordMatchState(this)

        // Countdown until end() can be called
        if (state == State.DROPPED) {
            countdown = rule.delay
        }
    }

    /**
     * Called during the firing stage of the engine loop on newly created matches with no remaining countdown.
     * Runs the fire clause of the rule definition with the binding values set to those contained by this match.
     */
    fun fire() {
        assert(state == State.MATCHED) { "Match is not ready to fire" }

        // The binding values may be referenced by fire(), so restore this match's values to the bindings.
        restoreBindings()
        rule.fire?.invoke(this)

        state = State.FIRED
        rule.engine.recorder?.recordMatchState(this)
        rule.fireCount++
    }

    /**
     * Called during the firing stage of the engine loop on newly dropped matches with no remaining countdown.
     * Runs the end clause of the rule definition with the binding values set to those contained by this match,
     * and deletes all facts maintained by this match that are not being maintained by another match.
     * Note that matches that were dropped before being fired do not have end() called.
     */
    fun end() {
        assert(state == State.DROPPED) { "Match is not ready to end" }

        // The binding values may be referenced by end(), so restore this match's values to the bindings.
        restoreBindings()
        rule.end?.invoke(this)

        // Un-list this match as a maintainer for the facts it was maintaining, and delete any facts that no longer
        // have a maintainer due to this.
        atomic {
            maintaining.forEach { fact ->
                rule.engine.maintainers[fact]?.let {
                    it.remove(this@CompleteMatch)
                    if (it.isEmpty()) {
                        delete(fact)
                    }
                }
            }
        }

        state = State.ENDED
        rule.engine.recorder?.recordMatchState(this)
    }

    /**
     * Set the values of the rule's bindings to those contained in this match.
     */
    fun restoreBindings() {
        bindValues.forEach { (binding, value) ->
            @Suppress("UNCHECKED_CAST")
            (binding as Binding<*, Any>).value = value
        }
    }

    override fun applyUpdates(updates: Map<KClass<*>, List<RuleEngine.Update<*>>>) {
    }

    fun atomic(updates: MatchAtomicBuilder.() -> Unit) {
        rule.engine.applyUpdates(MatchAtomicBuilder(rule.engine, this).apply(updates).updates)
    }

    override fun insert(fact: Any) = atomic {
        insert(fact)
    }

    override fun delete(fact: Any) = atomic {
        delete(fact)
    }

    override fun replace(old: Any, new: Any) = atomic {
        replace(old, new)
    }

    fun maintain(fact: Any) = atomic {
        maintain(fact)
    }

    inline fun <reified T: Any> all(noinline condition: T.() -> Boolean): Iterable<T> =
            all(Filter(condition))

    /**
     * Retrieve all facts in the engine that match the given filter,
     * without having to bind against it.
     */
    inline fun <reified T: Any> all(filter: Filter<T>): Iterable<T> =
            rule.engine.stores[T::class]?.let {
                @Suppress("UNCHECKED_CAST")
                (it as FactStore<T>).retrieveMatching(filter)
            } ?: listOf()

    inline fun <reified T: Any> exists(noinline condition: T.() -> Boolean): Boolean =
            exists(Filter(condition))

    /**
     * Check whether there are any facts in the engine that match the given filter,
     * without having to bind against it.
     */
    inline fun <reified T: Any> exists(filter: Filter<T>): Boolean =
            all(filter).any()
}