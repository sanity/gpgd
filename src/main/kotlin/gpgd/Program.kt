package gpgd

data class Program(val gpFunction: GPFunction, val constants : List<Double>) {
    fun simplifyConstants(tolerance : Double = 0.01) : Program {
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
                } else if ((gpFunction.name == "*") && ((gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 0.0)) || (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 0.0)))) {
                    Program(C(), listOf(0.0))
                } else if ((gpFunction.name == "*") && (gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 1.0))) {
                    Program(gpFunction.right, gpFunction.rightConstants(constants))
                } else if ((gpFunction.name == "*") && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 1.0))) {
                    Program(gpFunction.left, gpFunction.leftConstants(constants))
                } else if ((gpFunction.name == "+") && (gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(gpFunction.right, gpFunction.rightConstants(constants))
                } else if ((gpFunction.name == "+") && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(gpFunction.left, gpFunction.leftConstants(constants))
                } else if ((gpFunction.name == "^") && (gpFunction.left is C && constants[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(C(), listOf(0.0))
                } else if ((gpFunction.name == "^") && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 0.0))) {
                    Program(C(), listOf(1.0))
                } else if ((gpFunction.name == "^") && (gpFunction.right is C && gpFunction.rightConstants(constants)[0].withinToleranceOf(tolerance, 1.0))) {
                    Program(gpFunction.left, gpFunction.leftConstants(constants))
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
        return gpFunction.toString(constants, listOf("x", "z", "i", "j", "k", "l", "m"))
    }
}