package siliconsloth.miniruler.planner

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode
import it.uniroma1.dis.wsngroup.gexf4j.core.Node
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.SpellImpl
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter
import siliconsloth.miniruler.VARIABLES
import siliconsloth.miniruler.math.PartialOrder
import siliconsloth.miniruler.state
import java.io.File
import java.io.FileWriter
import java.lang.IllegalArgumentException

val INITIALIZE = Action("INITIALIZE", state(), mapOf())
val FINALIZE = Action("FINALIZE", state(), mapOf())

class PartialOrderPlanner(val goal: State, val actions: List<Action>) {
    var nextId = 0
    var timeStep = 0
    var edgeId = 0
    var lastPlan = Plan(listOf(), setOf(), PartialOrder())

    val gexf = GexfImpl()
    val graph = gexf.graph.apply {
        defaultEdgeType = EdgeType.DIRECTED
        mode = Mode.DYNAMIC
        timeType = "integer"
    }
    val nodes = mutableMapOf<Step, Node>()
    val edges = mutableMapOf<Link, Edge>()

    data class Step(val before: State, val action: Action, val after: State, val id: Int) {
        override fun toString(): String =
                "$action#$id"
    }

    data class Link(val setter: Step, val dependent: Step, val variable: Int)

    data class Plan(val steps: List<Step>, val links: Set<Link>, val orderings: PartialOrder<Step>)

    fun run(start: State) {
        val initialStep = Step(state(), INITIALIZE, start, nextId++)
        val finalStep = Step(goal, FINALIZE, state(), nextId++)

        val plan = fulfillPreconditions(Plan(
                listOf(initialStep, finalStep),
                setOf(),
                PartialOrder<Step>().apply { add(initialStep, finalStep) }
        ))
        println(plan)
    }

    fun fulfillPreconditions(plan: Plan): Plan? {
        println("$timeStep ${plan.steps.size} ${plan.links.size}")
//        println(plan)

        (plan.steps - lastPlan.steps).forEach { step ->
            nodes.getOrPut(step) { graph.createNode() }.apply {
                label = step.toString()
            }.spells.add(SpellImpl().apply {
                startValue = timeStep
            })
        }

        (lastPlan.steps - plan.steps).forEach { step ->
            nodes[step]!!.spells.last().endValue = timeStep
        }

        (plan.links - lastPlan.links).forEach { link ->
            try {
                edges.getOrPut(link) {
                    nodes[link.setter]!!.connectTo((edgeId++).toString(),
                            link.setter.before.variables[link.variable].name, EdgeType.DIRECTED, nodes[link.dependent]!!)
                }.spells.add(SpellImpl().apply {
                    startValue = timeStep
                })
            } catch (e: IllegalArgumentException) {

            }
        }

        (lastPlan.links - plan.links).forEach { link ->
            edges[link]?.let { it.spells.last().endValue = timeStep }
        }

//        if (timeStep >= 15000) {
//            val graphWriter = StaxGraphWriter()
//            val file = File("logs/graph${timeStep}.gexf")
//            val out = FileWriter(file, false)
//            graphWriter.writeToStream(gexf, out, "UTF-8")
////            System.exit(0)
//        }
        timeStep++
        lastPlan = plan

        val precondition = choosePrecondition(plan.steps, plan.links) ?: return null
        val needStep = precondition.first
        val varInd = precondition.second
        val variable = needStep.before.variables[varInd]

        val candidates = plan.steps.filter { it != needStep && (needStep.before.domains[varInd] as Domain<Any>)
                .supersetOf(it.after.domains[varInd] as Domain<Any>) &&
                (it.action == INITIALIZE || it.action.operations[varInd] != null)
                && !plan.orderings.precedes(needStep, it) } +
                actions.filter { it.operations[varInd] != null && canAchieve(it.operations[varInd] as Operation<Any>,
                        needStep.before.domains[varInd] as Domain<Any>) }

//        println(plan)
//        println(needStep)
//        println(variable)
//        candidates.forEachIndexed() { i,cand ->
//            println("$i: $cand")
//        }
//        print(">: ")
//        val choice = readLine()!!.toInt()

        candidates.forEachIndexed() { i,candidate ->
//            if (i == choice) {
                val newLink: Link
                var newStep: Step? = null
                val newPlan = when (candidate) {
                    is Step -> {
                        newLink = Link(candidate, needStep, varInd)
                        assert(needStep != candidate)
                        assert(!plan.orderings.precedes(needStep, candidate))
                        Plan(plan.steps, plan.links + newLink,
                                plan.orderings.plus(candidate, needStep))
                    }
                    is Action -> {
                        val stepPre = candidate.unapply(state(variable to needStep.before.domains[varInd]))
                        newStep = Step(stepPre, candidate, candidate.apply(stepPre), nextId++)
                        newLink = Link(newStep, needStep, varInd)

                        Plan(plan.steps + newStep, plan.links + newLink,
                                plan.orderings.plus(plan.steps.find { it.action == INITIALIZE }!!, newStep)
                                        .plus(newStep, needStep))
                    }
                    else -> error("Unexpected candidate type")
                }

                val threats = plan.steps.filter { step -> threatens(step, newLink, newPlan.orderings) }
                        .map { newLink to it }.toMutableList()
                assert(threats.all { it.first.setter != it.second && it.first.dependent != it.second }) { threats.map { (it.first.setter.hashCode() to it.first.dependent.hashCode()) to it.second.hashCode() } }
                if (newStep != null) {
                    newPlan.orderings.precedes(needStep, needStep)
                    threats += plan.links.filter { link -> threatens(newStep, link, newPlan.orderings) }
                            .map { it to newStep }
                }
                assert(threats.all { it.first.setter != it.second && it.first.dependent != it.second }) { threats.map { (it.first.setter.hashCode() to it.first.dependent.hashCode()) to it.second.hashCode() } }

                val completePlan = if (threats.isEmpty()) {
                    fulfillPreconditions(newPlan)
                } else {
                    resolveThreatsAndConditions(newPlan, threats)
                }

                if (completePlan != null) {
                    return completePlan
                }
//            }
        }
        return null
    }

    fun resolveThreatsAndConditions(plan: Plan, threats: List<Pair<Link, Step>>): Plan? {
//        println("$timeStep")
//        timeStep++
        val threat = threats[0]

//        println("Link: ${threat.first}")
//        println("Threat: ${threat.second}")
//        println("Variable: ${threat.first.setter.before.variables[threat.first.variable]}")

        if (!plan.orderings.precedes(threat.first.setter, threat.second)) {
//            print("Demote? ")
//            val choice = readLine()!!.startsWith("y")
//            if (choice) {
                val demotePlan = Plan(plan.steps, plan.links, plan.orderings.plus(threat.second, threat.first.setter))
                demotePlan.orderings.precedes(threat.second, threat.second)
                val completePlan = if (threats.size > 1) {
                    resolveThreatsAndConditions(demotePlan, threats.drop(1))
                } else {
                    fulfillPreconditions(demotePlan)
                }
                if (completePlan != null) {
                    return completePlan
                }
//            }
        }

        if (!plan.orderings.precedes(threat.second, threat.first.dependent)) {
//            print("Promote? ")
//            val choice = readLine()!!.startsWith("y")
//            if (choice) {
                val promotePlan = Plan(plan.steps, plan.links, plan.orderings.plus(threat.first.dependent, threat.second))
                promotePlan.orderings.precedes(threat.first.dependent, threat.first.dependent)
                val completePlan = if (threats.size > 1) {
                    resolveThreatsAndConditions(promotePlan, threats.drop(1))
                } else {
                    fulfillPreconditions(promotePlan)
                }
                if (completePlan != null) {
                    return completePlan
                }
//            }
        }

        return null
    }

    fun choosePrecondition(steps: List<Step>, links: Set<Link>): Pair<Step, Int>? {
        steps.forEach { step ->
            step.before.variables.forEachIndexed { index, variable ->
                if (step.before.domains[index] != variable.initializeDomain() &&
                        !links.any { it.variable == index && it.dependent == step }) {
                    return step to index
                }
            }
        }
        return null
    }

    fun <T> canAchieve(operation: Operation<T>, domain: Domain<T>): Boolean =
            if (domain is AnyValue) {
                error("Trying to achieve AnyValue")
            } else if (domain is Enumeration<*> && domain.values.isEmpty()) {
                error("Empty domain")
            } else {
                (operation is Add && operation.value > 0) || operation is AddArbitrary ||
                        (operation is SetTo && domain.supersetOf(Enumeration(operation.value)))
            }

    fun threatens(step: Step, link: Link, orderings: PartialOrder<Step>): Boolean =
                !(step == link.setter || step == link.dependent ||
                (link.dependent.before.domains[link.variable] as Domain<Any>)
                        .supersetOf(step.after.domains[link.variable] as Domain<Any>) ||
                link.setter.action.operations[link.variable] is AddArbitrary ||
                step.action.operations[link.variable] == null ||
                orderings.precedes(step, link.setter) || orderings.precedes(link.dependent, step))
}