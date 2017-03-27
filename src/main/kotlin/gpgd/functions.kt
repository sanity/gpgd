package gpgd

/**
 * Created by ian on 3/25/17.
 */

abstract class GPFunction(val numConstants : Int) {
    abstract fun value(constants : List<Double>, parameters : List<Double>) : Double
    abstract fun toString(constants : List<Double>, parameters : List<String>) : String
    abstract val size : Int

    override fun equals(other: Any?): Boolean = this.toString() == other.toString()
    override fun hashCode(): Int {
        return this.toString().hashCode()
    }
}

open class UniFunction(val name : String, val f : (Double) -> Double, val subFun : GPFunction) : GPFunction(subFun.numConstants) {
    override val size: Int get() = 1 + subFun.size

    override fun toString(constants: List<Double>, parameters: List<String>) : String {
        return "$name(${subFun.toString(constants, parameters)})"
    }


    override fun value(constants: List<Double>, parameters: List<Double>): Double {
        assert(constants.size == numConstants)
        return f(subFun.value(constants, parameters))
    }

    fun replaceSubFun(replacementFun : GPFunction) : UniFunction {
        return UniFunction(name, f, replacementFun)
    }

    override fun toString(): String {
        return "$name($subFun)"
    }
}

class Sin(a : GPFunction) : UniFunction("sin", {Math.sin(it)}, a)

class Log(a : GPFunction) : UniFunction("log", {Math.log(it)}, a)


open class BiFunction(val name : String, val f : (Double, Double) -> Double, origLeft: GPFunction, origRight: GPFunction, val swapIfRightIsConstant : Boolean = false) : GPFunction(origLeft.numConstants + origRight.numConstants) {
    val left : GPFunction
    val right : GPFunction

    init {
        if (swapIfRightIsConstant && origRight is C && origLeft !is C) {
            left = origRight
            right = origLeft
        } else {
            left = origLeft
            right = origRight
        }
    }

    override val size: Int
        get() = 1 + left.size + right.size

    override fun toString(constants: List<Double>, parameters: List<String>): String {
        return "(${left.toString(leftConstants(constants), parameters)} $name ${right.toString(rightConstants(constants), parameters)})"
    }

    override fun value(constants: List<Double>, parameters: List<Double>): Double {
        assert(constants.size == numConstants, {"was passed constants $constants, but was expecting $numConstants constants"})
        return f(left.value(leftConstants(constants), parameters), right.value(rightConstants(constants), parameters))
    }
    fun leftConstants(constants: List<Double>) = constants.subList(0, left.numConstants)

    fun rightConstants(constants: List<Double>) = constants.subList(left.numConstants, constants.size)

    fun replaceLeftSubFun(replacementFun : GPFunction) : BiFunction {
        return BiFunction(name, f, replacementFun, right)
    }

    fun replaceRightSubFun(replacementFun : GPFunction) : BiFunction {
        return BiFunction(name, f, left, replacementFun)
    }

    override fun toString(): String = "($left $name $right)"
}

infix operator fun GPFunction.plus(b : GPFunction) = Plus(this, b)
class Plus(a : GPFunction, b: GPFunction) : BiFunction("+", { a, b -> a+b}, a, b, true)

infix operator fun GPFunction.times(b : GPFunction) = Times(this, b)
class Times(a : GPFunction, b: GPFunction) : BiFunction("*", { a, b -> a * b}, a, b, true)

infix fun GPFunction.pow(b : GPFunction) = Pow(this, b)
class Pow(a : GPFunction, b: GPFunction) : BiFunction("^", {a, b -> Math.pow(a, b)}, a, b)

class C : GPFunction(1) {
    override val size = 1

    override fun toString(constants: List<Double>, parameters: List<String>): String {
        assert(constants.size == 1)
        return doubleShortener.format(constants[0])
    }

    override fun value(constants: List<Double>, parameters: List<Double>): Double {
        assert(constants.size == numConstants)
        return constants[0]
    }

    override fun toString(): String = "C"
}

class P(val pNum : Int) : GPFunction(0) {
    override val size = 1

    override fun toString(constants: List<Double>, parameters: List<String>): String {
        return parameters[pNum]
    }

    override fun value(constants: List<Double>, parameters: List<Double>): Double {
        return parameters[pNum]
    }

    override fun toString(): String = "P($pNum)"
}