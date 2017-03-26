package gpgd

import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer

class NelderMeadOptimizer(dimensions : Int, val maxEval : Int = 1000) : Optimizer(dimensions) {
    fun optimize(function : (List<Double>) -> Double) = optimize(function, List<Double>(dimensions, {0.1}))

    override fun optimize(function : (List<Double>) -> Double, initialConstants: List<Double>) : Optimized {
        val nelderMead = NelderMeadSimplex(initialConstants.size)
        val opt = SimplexOptimizer(1e-6, 1e-10)
        nelderMead.build(initialConstants.toDoubleArray())
        val optimal = opt.optimize(InitialGuess(initialConstants.toDoubleArray()), GoalType.MINIMIZE, ObjectiveFunction({ da -> function(da.toList())}), MaxEval(maxEval), nelderMead)
        return Optimized(optimal.point.toList(), optimal.value)
    }

}