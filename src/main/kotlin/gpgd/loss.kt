package gpgd

import org.apache.commons.math3.stat.descriptive.SummaryStatistics

/**
 * Created by ian on 3/25/17.
 */

fun calcLoss(f : (List<Double>) -> Double, data : Iterable<InputOutput>) : Double {
    var sumSquaredError = 0.0
    var count = 0
    for (io in data) {
        count++
        val squared = (f(io.input) - io.output).squared()
        sumSquaredError += squared
    }
    assert (count > 10, {"less than three InputOutputs in data, not enough to calculate a good loss"})
    return Math.sqrt(sumSquaredError / count)
}

data class InputOutput(val input : List<Double>, val output : Double)

fun splitTrainTest(numVariables : Int, data : Iterable<InputOutput>, testProp : Double = 0.2) : TrainTest {
    val counters = Array(numVariables, {SummaryStatistics()})
    for ((input) in data) {
        for ((ix, v) in input.withIndex()) {
            counters[ix].addValue(v)
        }
    }
    val scored = ArrayList<ValWithScore<InputOutput>>()
    for (io in data) {
         val errorStat = SummaryStatistics()
         io.input.withIndex().forEach {(ix, d) ->
            val counter = counters[ix]
            val error = ((d - counter.mean) / counter.standardDeviation).squared()
            errorStat.addValue(error)
        }
        scored += ValWithScore(Math.sqrt(errorStat.mean), io)
    }
    scored.sort()
    val threshold = Math.round(scored.size * (1.0-testProp)).toInt()
    val trainingData = ArrayList<InputOutput>()
    (0 .. (threshold-1)).mapTo(trainingData) { scored[it].v }
    val testingData = ArrayList<InputOutput>()
    (threshold .. (scored.size-1)).mapTo(testingData) { scored[it].v }
    return TrainTest(trainingData, testingData)
}

data class TrainTest(val train : Iterable<InputOutput>, val test : Iterable<InputOutput>)

data class ValWithScore<V : Any>(val score : Double, val v : V) : Comparable<ValWithScore<V>> {
    init {
        if (score.isInfinite()) throw RuntimeException("Score is infinite")
    }
    override fun compareTo(other: ValWithScore<V>): Int {
        if (score != other.score) {
            return score.compareTo(other.score)
        } else {
            return v.hashCode().compareTo(other.v.hashCode())
        }
    }
}