package gpgd

import java.util.*

/**
 * Created by ian on 3/25/17.
 */

fun optimize(dimensions : Int, data : Iterable<InputOutput>) {
    val trainTest = splitTrainTest(dimensions, data, 0.2)
    val optimizer = NelderMeadOptimizer(dimensions)

    val queueForMutation : TreeSet<ValWithScore<Program>> = TreeSet()
    val evaluatedPrograms = HashSet<GPFunction>()
    val mutatedPrograms = HashSet<GPFunction>()

    val queueForEvaluation : TreeSet<Program> = TreeSet(Comparator<Program> { a, b ->
        if (a.gpFunction.size != b.gpFunction.size) {
            a.gpFunction.size.compareTo(b.gpFunction.size)
        } else {
            a.toString().compareTo(b.toString())
        }
    })
    queueForEvaluation += Program(C(), listOf(0.1))
    //queueForEvaluation += Program(P(0), listOf(0.1))
    //queueForEvaluation += Program((P(0) * C()) + C(), listOf(1.0, 0.1))
    //queueForEvaluation += Program(P(0) pow C(), listOf(-1.0))
    //queueForEvaluation += Program(C() pow P(0), listOf(1.1))
    outer@ while (true) {
        assert(queueForMutation.size == evaluatedPrograms.size)
        val next = queueForEvaluation.pollFirst()
        if (next == null) {
            val toMutate = queueForMutation.pollFirst()
            if (toMutate != null) {
                mutate(toMutate.v, allMutators(dimensions)).forEach {
                    queueForEvaluation += it.simplifyConstants()
                }
            } else {
                break@outer
            }

        } else if (!mutatedPrograms.contains(next.gpFunction)) {
            mutatedPrograms += next.gpFunction
            fun funToOpt(constants : List<Double>) : Double {
                val function : (List<Double>) -> Double = { parameters ->
                    assert(parameters.size == dimensions)
                    try {
                        next.gpFunction.value(constants, parameters)
                    } catch (e : IndexOutOfBoundsException) {
                        throw RuntimeException("IndexOutOfBounds exception while evaluating ${next.gpFunction} with constants: $constants, parameters: $parameters", e)
                    }
                }
                return calcLoss(function, trainTest.train)
            }
            try {
                val optimized = optimizer.optimize(::funToOpt, next.constants)
                val testScore = calcLoss({ params -> next.gpFunction.value(optimized.constants, params) }, trainTest.test)
                val bestSoFar = queueForMutation.firstOrNull()
                val valWithScore = ValWithScore(testScore, Program(next.gpFunction, optimized.constants))
                queueForMutation += valWithScore
                if (bestSoFar == null || testScore < bestSoFar.score) {
                    println("Eval Queue: ${queueForEvaluation.size}\tMutation Queue: ${queueForMutation.size}\tBest: $valWithScore")
                }
            } catch (e : Exception) {
               // println("${e.message} while optimizing ${next.gpFunction}, ignoring")
            }
        }
    }
}

