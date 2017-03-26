package gpgd

/**
 * Created by ian on 3/25/17.
 */
abstract class Optimizer(val dimensions : Int) {
    abstract fun optimize(function: (List<Double>) -> Double, initialConstants: List<Double>): Optimized

    data class Optimized(val constants : List<Double>, val score : Double)
}