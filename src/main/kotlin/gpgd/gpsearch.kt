package gpgd

import java.util.*

/**
 * Created by ian on 3/25/17.
 */

fun optimize(dimensions : Int, data : Iterable<InputOutput>, iterationCallback : (Program) -> Unit) {
    val maxFunctionSize = 50
    val trainTest = splitTrainTest(dimensions, data, 0.2)
    val optimizer = NelderMeadOptimizer(dimensions)

    val queueForMutation : TreeSet<ValWithScore<Program>> = TreeSet()
    val allEvaluated : TreeSet<ValWithScore<Program>> = TreeSet()
    val evaluatedPrograms = HashSet<GPFunction>()
    val mutatedPrograms = HashSet<GPFunction>()

    val queueForEvaluation = TreeSet<ValWithScore<Program>>()

   /* val queueForEvaluation : TreeSet<Program> = TreeSet(Comparator<Program> { a, b ->
        if (a.gpFunction.size != b.gpFunction.size) {
            a.gpFunction.size.compareTo(b.gpFunction.size)
        } else {
            a.toString().compareTo(b.toString())
        }
    }) */
    queueForEvaluation += ValWithScore(Double.MAX_VALUE, Program(C(), listOf(0.1)))
    //queueForEvaluation += Program(P(0), listOf(0.1))
    //queueForEvaluation += Program((P(0) * C()) + C(), listOf(1.0, 0.1))
    //queueForEvaluation += Program(P(0) pow C(), listOf(-1.0))
    //queueForEvaluation += Program(C() pow P(0), listOf(1.1))
    outer@ while (true) {
        assert(queueForMutation.size == evaluatedPrograms.size)
        val toEvaluateWithScore = queueForEvaluation.pollFirst()
        if (toEvaluateWithScore == null) {
            val toMutate = queueForMutation.pollFirst()
            if (toMutate != null) {
                mutate(toMutate.v, allMutators(dimensions)).forEach {
                    val simplifiedMutation = it.simplifyConstants()
                    if (simplifiedMutation.gpFunction.size <= maxFunctionSize) {
                        queueForEvaluation +=ValWithScore(toMutate.score, simplifiedMutation)
                    }
                }
            } else {
                break@outer
            }

        } else {
            val toEvaluate = toEvaluateWithScore.v
            if (!mutatedPrograms.contains(toEvaluate.gpFunction)) {
                mutatedPrograms += toEvaluate.gpFunction
                fun funToOpt(constants : List<Double>) : Double {
                    val function : (List<Double>) -> Double = { parameters ->
                        assert(parameters.size == dimensions)
                        try {
                            toEvaluate.gpFunction.value(constants, parameters)
                        } catch (e : IndexOutOfBoundsException) {
                            throw RuntimeException("IndexOutOfBounds exception while evaluating ${toEvaluate.gpFunction} with constants: $constants, parameters: $parameters", e)
                        }
                    }
                    return calcLoss(function, trainTest.train)
                }
                try {
                    val optimized = optimizer.optimize(::funToOpt, toEvaluate.constants)
                    val testScore = calcLoss({ params -> toEvaluate.gpFunction.value(optimized.constants, params) }, trainTest.test) + optimized.score
                    val bestSoFar = allEvaluated.firstOrNull()
                    val valWithScore = ValWithScore(testScore, Program(toEvaluate.gpFunction, optimized.constants))
                    evaluatedPrograms += valWithScore.v.gpFunction
                    allEvaluated += valWithScore
                    queueForMutation += valWithScore
                    if (bestSoFar == null || testScore < bestSoFar.score) {
                        iterationCallback(valWithScore.v)
                        println("Evaluated: ${allEvaluated.size}\tEval Queue: ${queueForEvaluation.size}\tMutation Queue: ${queueForMutation.size}\tSize: ${valWithScore.v.gpFunction.size}\tNew best: $valWithScore")
                    }
                } catch (e : Exception) {
                   // println("${e.message} while optimizing ${next.gpFunction}, ignoring")
                }
            }
        }
    }
}

