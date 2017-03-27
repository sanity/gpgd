package gpgd

data class Program(val gpFunction: GPFunction, val constants : List<Double>) {
    fun simplifyConstants(tolerance : Double = 0.000001) : Program {
        return when {
            gpFunction is UniFunction -> {
                val simplifiedSubProgram = Program(gpFunction.subFun, constants).simplifyConstants()
                if (simplifiedSubProgram.gpFunction is C) {
                    Program(C(), listOf(gpFunction.f(simplifiedSubProgram.constants[0])))
                } else {
                    Program(gpFunction.replaceSubFun(simplifiedSubProgram.gpFunction), simplifiedSubProgram.constants)
                }
            }
            gpFunction is BiFunction -> {
                val simplifedLeft = Program(gpFunction.left, gpFunction.leftConstants(constants)).simplifyConstants()
                val simplifiedRight = Program(gpFunction.right, gpFunction.rightConstants(constants)).simplifyConstants()
                if (simplifedLeft.gpFunction is C && simplifiedRight.gpFunction is C) {
                    Program(C(), listOf(gpFunction.f(gpFunction.leftConstants(constants)[0], gpFunction.rightConstants(constants)[0])))
                } else if ((gpFunction is Times) && ((gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 0.0)) || (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 0.0)))) {
                    Program(C(), listOf(0.0))
                } else if ((gpFunction is Times) && (gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 1.0))) {
                    Program(gpFunction.right, gpFunction.rightConstants(constants))
                } else if ((gpFunction is Times) && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 1.0))) {
                    Program(gpFunction.left, gpFunction.leftConstants(constants))
                } else if ((gpFunction is Plus) && (gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(gpFunction.right, gpFunction.rightConstants(constants))
                } else if ((gpFunction is Plus) && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(gpFunction.left, gpFunction.leftConstants(constants))
                } else if ((gpFunction is Plus) && (gpFunction.numConstants == 0 && gpFunction.left == gpFunction.right)) {
                    Program(gpFunction.left * C(), gpFunction.leftConstants(constants) + 2.0)
                } else if ((gpFunction is Pow) && (gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(C(), listOf(0.0))
                } else if ((gpFunction is Pow) && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(C(), listOf(1.0))
                } else if ((gpFunction is Pow) && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 1.0))) {
                    Program(gpFunction.left, gpFunction.leftConstants(constants))
                } else if ((gpFunction is Pow) && (gpFunction.left is Pow)) {
                    Program(Pow(gpFunction.left.left, gpFunction.left.right * gpFunction.right), constants)
                } else {
                    Program(gpFunction.replaceLeftSubFun(simplifedLeft.gpFunction).replaceRightSubFun(simplifiedRight.gpFunction), simplifedLeft.constants + simplifiedRight.constants)
                }
            }
            else -> this
        }
    }

    private fun Double.withinToleranceOf(tolerance : Double, v : Double) : Boolean {
        return Math.abs(this - v) < tolerance
    }

    override fun toString(): String {
        return gpFunction.toString(constants, listOf("x", "y", "z", "i", "j", "k", "l", "m"))
    }
}