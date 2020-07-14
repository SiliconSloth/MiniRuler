package siliconsloth.miniruler.planner

import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.matching.CompleteMatch
import siliconsloth.miniruler.engine.stores.FactStore
import siliconsloth.miniruler.planner.rules.planningRules
import java.io.File
import java.io.FileWriter

class RulePlanner(val engine: RuleEngine, val variables: Array<Variable<*>>, val goal: State) {
    var nextId = 0

    var initialize: Action? = null
    val finalize = Action("FINALIZE", goal, mapOf())

    fun run(start: State) {
        initialize = Action("INITIALIZE", state(),
                start.map { (v,d) -> (d as? Enumeration)?.let { v to SetTo((it.values.first())) } }
                        .filterNotNull().toMap())

        try {
            engine.planningRules(this)
            engine.atomic {
                insert(Step(state(), initialize!!, start, nextId++))
                insert(Step(goal, finalize, state(), nextId++))
            }

            while (engine.allMatches.any { it.state == CompleteMatch.State.MATCHED || it.state == CompleteMatch.State.DROPPED }) {
                engine.insert("Dummy")
                engine.delete("Dummy")
            }

        } finally {
            val gexf = GexfImpl()
            val graph = gexf.graph.apply {
                defaultEdgeType = EdgeType.DIRECTED
                mode = Mode.STATIC
            }

            @Suppress("UNCHECKED_CAST")
            val nodes = (engine.stores[Step::class] as FactStore<Step>).allFacts().associateWith {
                graph.createNode().apply {
                    label = it.toString()
                }
            }

            @Suppress("UNCHECKED_CAST")
            val labels = (engine.stores[Link::class] as FactStore<Link>).allFacts().groupBy { Ordering(it.setter, it.precondition.step) }.mapValues { (_, v) ->
                v.joinToString(", ") {
                    it.precondition.variable.toString()
                }
            }

            var edgeId = 0
            @Suppress("UNCHECKED_CAST")
            (engine.stores[Ordering::class] as FactStore<Ordering>).allFacts().forEach { o ->
                try {
                    nodes[o.before]!!.connectTo((edgeId++).toString(), labels[o]
                            ?: "", EdgeType.DIRECTED, nodes[o.after]!!)
                } catch (ex: Exception) {

                }
            }

            val graphWriter = StaxGraphWriter()
            val file = File("output.gexf")
            FileWriter(file, false).use {
                graphWriter.writeToStream(gexf, it, "UTF-8")
            }
        }
    }

    fun state(domains: Map<Variable<*>, Domain<*>>): State =
            State(variables, domains)

    fun state(vararg domains: Pair<Variable<*>, Domain<*>>): State =
            state(mapOf(*domains))

    fun newStep(before: State, action: Action, after: State): Step {
        assert(action != initialize && action != finalize)
        return Step(before, action, after, nextId++)
    }

    fun newStep(action: Action, stepGoal: State): Step {
        val stepBefore = action.unapply(stepGoal)
        val stepAfter = action.apply(stepBefore).intersect(stepGoal)
        return newStep(stepBefore, action, stepAfter)
    }

    fun newStepFulfilling(action: Action, precondition: Precondition): Step {
        val stepGoal = state(precondition.variable to precondition.step.before[precondition.variable])
        return newStep(action, stepGoal)
    }
}