package siliconsloth.miniruler.planner

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode
import it.uniroma1.dis.wsngroup.gexf4j.core.Node
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.SpellImpl
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter
import siliconsloth.miniruler.Item
import siliconsloth.miniruler.VARIABLES
import siliconsloth.miniruler.itemCount
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
    var lastPlan = Plan(listOf(), setOf(), PartialOrder(), "Empty")

    val choices = mutableListOf(0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 4, 1, 0, 1, 0, 1, 1, 4, 0, 1)
    val demotions = mutableListOf(true, true, true, true, false, true, true, true, true, true, true, true, true, true)

    data class Step(val before: State, val action: Action, val after: State, val id: Int) {
        override fun toString(): String =
                "$action#$id"
    }

    data class Link(val setter: Step, val dependent: Step, val variable: Int)

    data class Plan(val steps: List<Step>, val links: Set<Link>, val orderings: PartialOrder<Step>, val label: String) {
        val cost = steps.map { it.action.cost(it.before, it.after) }.sum()

        fun replaced(oldStep: Step, newStep: Step): Plan =
                Plan(steps - oldStep + newStep, links.map {
                        Link(if (it.setter == oldStep) newStep else it.setter,
                             if (it.dependent == oldStep) newStep else it.dependent,
                             it.variable)
                }.toSet(), PartialOrder(orderings).apply { replace(oldStep, newStep) }, "Replace $oldStep")

        override fun toString(): String = label
    }

    fun run(start: State) {
        val initialStep = Step(state(), INITIALIZE, start, nextId++)
        val finalStep = Step(goal, FINALIZE, state(), nextId++)

        val frontier = mutableListOf(Plan(
                listOf(initialStep, finalStep),
                setOf(),
                PartialOrder<Step>().apply { add(initialStep, finalStep) }, "Initial") to 0)

        var plan: Plan? = null
        while (frontier.isNotEmpty()) {
//            println(frontier.size)
//            val current = frontier.minBy { it.second }!!
//            frontier.remove(current)
            val current = frontier.removeAt(frontier.size-1)

            val children = fulfillPreconditions(current.first)
            if (children == null) {
                plan = current.first
                break
            } else {
                println(children.map { it.first }.sortedBy { it.cost })
                frontier.addAll(children.map { it.first to it.second + current.second }.sortedByDescending { it.first.cost })
            }
        }

        if (plan == null) {
            println("No plan found.")
        } else {
            println("Done!")

            val gexf = GexfImpl()
            val graph = gexf.graph.apply {
                defaultEdgeType = EdgeType.DIRECTED
                mode = Mode.STATIC
            }

            val nodes = plan.steps.associateWith {
                graph.createNode().apply {
                    label = it.toString()
                }
            }

            var edgeId = 0
            plan.links.groupBy { it.setter to it.dependent }.mapValues { (_,v) ->
                v.joinToString(", ") {
                    initialStep.before.variables[it.variable].toString()
                }
            }.forEach { (k,v) ->
                nodes[k.first]!!.connectTo((edgeId++).toString(), v, EdgeType.DIRECTED, nodes[k.second]!!)
            }

            val graphWriter = StaxGraphWriter()
            val file = File("graphs/output.gexf")
            val out = FileWriter(file, false)
            graphWriter.writeToStream(gexf, out, "UTF-8")
        }
    }

    fun fulfillPreconditions(plan: Plan): List<Pair<Plan, Int>>? {
        println("$timeStep ${plan.steps.size} ${plan.links.size}")
//        println(plan)

//        (plan.steps - lastPlan.steps).forEach { step ->
//            nodes.getOrPut(step) { graph.createNode() }.apply {
//                label = step.toString()
//            }.spells.add(SpellImpl().apply {
//                startValue = timeStep
//            })
//        }
//
//        (lastPlan.steps - plan.steps).forEach { step ->
//            nodes[step]!!.spells.last().endValue = timeStep
//        }
//
//        (plan.links - lastPlan.links).forEach { link ->
//            try {
//                edges.getOrPut(link) {
//                    nodes[link.setter]!!.connectTo((edgeId++).toString(),
//                            link.setter.before.variables[link.variable].name, EdgeType.DIRECTED, nodes[link.dependent]!!)
//                }.spells.add(SpellImpl().apply {
//                    startValue = timeStep
//                })
//            } catch (e: IllegalArgumentException) {
//
//            }
//        }
//
//        (lastPlan.links - plan.links).forEach { link ->
//            edges[link]?.let { it.spells.last().endValue = timeStep }
//        }

//        if (timeStep >= 5000) {
//            val graphWriter = StaxGraphWriter()
//            val file = File("logs/graph${timeStep}.gexf")
//            val out = FileWriter(file, false)
//            graphWriter.writeToStream(gexf, out, "UTF-8")
//            System.exit(0)
//        }
        timeStep++
        lastPlan = plan

        val preconditionAndCandidates = openPreconditions(plan.steps, plan.links).map {
            it to fulfillmentCandidates(it.first, it.second, plan)
        }.minBy { it.second.size }
                ?: return null

        val needStep = preconditionAndCandidates.first.first
        val varInd = preconditionAndCandidates.first.second
        val variable = needStep.before.variables[varInd]
        val candidates = preconditionAndCandidates.second

        println(candidates)

//        val choice = if (candidates.size != 1) {
//            println(plan)
            println(needStep)
            println(variable)
//            candidates.fo rEachIndexed() { i, cand ->
//                println("$i: $cand")
//            }
//            print(">: ")
//            readLine()!!.toInt()
//        } else {
//            0
//        }

        val newPlans = mutableListOf<Pair<Plan, Int>>()
        candidates.forEachIndexed() { i, (candidate, isNew) ->
//            if (i == choice) {
                val newCandidate: Step
                val newPlan = if (isNew) {
                        Plan(plan.steps + candidate, plan.links + Link(candidate, needStep, varInd),
                                plan.orderings.plus(plan.steps.find { it.action == INITIALIZE }!!, candidate)
                                        .plus(candidate, needStep), "Add $candidate")
                    } else {
                        assert(needStep != candidate)
                        assert(!plan.orderings.precedes(needStep, candidate))
                        Plan(plan.steps, plan.links + Link(candidate, needStep, varInd),
                                plan.orderings.plus(candidate, needStep), "Link $candidate")
                    }.let { plan ->
                        if (!(needStep.before.domains[varInd] as Domain<Any>)
                                        .supersetOf(candidate.after.domains[varInd] as Domain<Any>)) {
                            val newAfter = candidate.after.intersect(state(variable to needStep.before.domains[varInd]))
                            val newSetter = Step(candidate.before, candidate.action, newAfter, nextId++)

                            newCandidate = newSetter
                            plan.replaced(candidate, newSetter)
                        } else {
                            newCandidate = candidate
                            plan
                        }
                    }

                val newLink = Link(newCandidate, needStep, varInd)

                assert((newLink.dependent.before.domains[newLink.variable] as Domain<Any>).supersetOf(
                        newLink.setter.after.domains[newLink.variable] as Domain<Any>
                )) { "$newLink ${newCandidate.before.variables[newLink.variable]}"}

                if (isNew && canPrune(plan, newCandidate, needStep)) {
                    println("Prune!")
                    return@forEachIndexed
                }

                val threats = plan.steps.filter { step -> threatens(step, newLink, newPlan.orderings) }
                        .map { newLink to it }.toMutableList()
                assert(threats.all { it.first.setter != it.second && it.first.dependent != it.second }) { threats.map { (it.first.setter.hashCode() to it.first.dependent.hashCode()) to it.second.hashCode() } }
                if (isNew) {
                    newPlan.orderings.precedes(needStep, needStep)
                    threats += plan.links.filter { link -> threatens(newCandidate, link, newPlan.orderings) }
                            .map { it to newCandidate }
                }
                assert(threats.all { it.first.setter != it.second && it.first.dependent != it.second }) { threats.map { (it.first.setter.hashCode() to it.first.dependent.hashCode()) to it.second.hashCode() } }

                if (threats.isEmpty()) {
                    newPlans.add(newPlan to candidates.size)
                } else {
                    newPlans.addAll(resolveThreatsAndConditions(newPlan, threats).map { it to candidates.size })
                }
//            }
        }
        return newPlans
    }

    fun resolveThreatsAndConditions(plan: Plan, threats: List<Pair<Link, Step>>): List<Plan> {
//        println("$timeStep")
//        timeStep++
        val threat = threats[0]
        val variable = threat.first.variable
        val newPlans = mutableListOf<Plan>()

        if (threat.first.setter.action.operations[variable] is AddArbitrary &&
                threat.second.action.operations[variable] is Add) {
            println(threat)
            println(threat.first.setter.before.variables[variable])
            val setter = threat.first.setter
            val newAfter = setter.after.intersect(state(setter.before.variables[variable] to
                    LowerBounded((setter.after.domains[variable] as LowerBounded).min -
                            (threat.second.action.operations[variable] as Add).value)))
            val replacement = Step(setter.before, setter.action, newAfter, nextId++)
            val replacePlan = plan.replaced(setter, replacement)

            if (threats.size > 1) {
                newPlans.addAll(resolveThreatsAndConditions(replacePlan, threats.drop(1)))
            } else {
                newPlans.add(replacePlan)
            }
        }

        if (!plan.orderings.precedes(threat.first.setter, threat.second)) {
//            val choice = if (!plan.orderings.precedes(threat.second, threat.first.dependent)) {
//                println("Link: ${threat.first}")
//                println("Threat: ${threat.second}")
//                println("Variable: ${threat.first.setter.before.variables[threat.first.variable]}")
//
//                print("Demote? ")
//                readLine()!!.startsWith("y")
//            } else {
//                true
//            }
//            if (choice) {
                val demotePlan = Plan(plan.steps, plan.links, plan.orderings.plus(threat.second, threat.first.setter), "D $plan")
                if (threats.size > 1) {
                    newPlans.addAll(resolveThreatsAndConditions(demotePlan, threats.drop(1)))
                } else {
                    newPlans.add(demotePlan)
                }
//                return newPlans
//            }
        }

        if (!plan.orderings.precedes(threat.second, threat.first.dependent)) {
//            print("Promote? ")
//            val choice = readLine()!!.startsWith("y")
//            if (choice) {
                val promotePlan = Plan(plan.steps, plan.links, plan.orderings.plus(threat.first.dependent, threat.second), "P $plan")
                if (threats.size > 1) {
                    newPlans.addAll(resolveThreatsAndConditions(promotePlan, threats.drop(1)))
                } else {
                    newPlans.add(promotePlan)
                }
//            }
        }

        return newPlans
    }

    fun openPreconditions(steps: List<Step>, links: Set<Link>): List<Pair<Step, Int>> =
            steps.map { step ->
                step.before.variables.mapIndexed { index, variable ->
                    if (step.before.domains[index] != variable.initializeDomain() &&
                            !links.any { it.variable == index && it.dependent == step }) {
                        step to index
                    } else {
                        null
                    }
                }.filterNotNull()
            }.flatten()

    fun fulfillmentCandidates(needStep: Step, varInd: Int, plan: Plan): List<Pair<Step, Boolean>> =
            plan.steps.filter { it != needStep && ((needStep.before.domains[varInd] as Domain<Any>)
            .supersetOf(it.after.domains[varInd] as Domain<Any>) ||
                    it.action.operations[varInd] is AddArbitrary) &&
            (it.action == INITIALIZE || it.action.operations[varInd] != null)
            && !plan.orderings.precedes(needStep, it) }.map { it to false } +

            actions.filter { it.operations[varInd] != null && canAchieve(it.operations[varInd] as Operation<Any>,
                    needStep.before.domains[varInd] as Domain<Any>) }.map { action ->
                val stepGoal = state(needStep.before.variables[varInd] to needStep.before.domains[varInd])
                val stepPre = action.unapply(stepGoal)
                Step(stepPre, action, action.apply(stepPre).intersect(stepGoal), nextId++)
            }.map { it to true }

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
                step.action.operations[link.variable] == null ||
                orderings.precedes(step, link.setter) || orderings.precedes(link.dependent, step))

    fun canPrune(plan: Plan, newStep: Step, needStep: Step): Boolean {
        plan.steps.filter { otherStep -> otherStep.action == newStep.action &&
                plan.links.filter { it.setter == otherStep }.all {
                    (it.dependent.before.domains[it.variable] as Domain<Any>)
                            .supersetOf(newStep.after.domains[it.variable] as Domain<Any>)
                } }.forEach { otherStep ->

            val frontier = mutableListOf(needStep)
            while (frontier.isNotEmpty()) {
                val current = frontier.removeAt(0)
                if (current.action == FINALIZE) {
                    return@forEach
                } else if (current != otherStep) {
                    frontier.addAll(plan.links.filter { it.setter == current }.map { it.dependent })
                }
            }

            return true
        }

        return false
    }
}