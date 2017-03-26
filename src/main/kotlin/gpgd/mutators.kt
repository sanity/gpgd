package gpgd

/**
 * Created by ian on 3/25/17.
 */

fun mutate(program: Program, mutators: List<Mutator>): List<Program> {
    val mutations = ArrayList<Program>()
    val origConstants = program.constants
    val origFunction = program.gpFunction
    for (mutator in mutators) {
        val mutated = mutator.mutate(origConstants, origFunction)
        if (mutated != null) {
            mutations.add(Program(mutated.mFun, mutated.newConstants))
            try {
                mutated.mFun.value(mutated.newConstants, DoubleArray(100, { 0.1 }).asList())
            } catch (e: Exception) {
                throw RuntimeException("Error evaluating ${mutated.mFun} with constants: ${mutated.newConstants} after mutation ${mutator} mutated $origFunction/${origConstants}", e)
            }
        }
    }

    if (origFunction is UniFunction) {
        mutations += mutate(Program(origFunction.subFun, origConstants), mutators).map { program -> Program(origFunction.replaceSubFun(program.gpFunction), program.constants) }
    } else if (origFunction is BiFunction) {
        mutations += mutate(Program(origFunction.left, origFunction.leftConstants(origConstants)), mutators).map {
            mutation ->
            Program(origFunction.replaceLeftSubFun(mutation.gpFunction), mutation.constants + origFunction.rightConstants(origConstants))
        }
        mutations += mutate(Program(origFunction.right, origFunction.rightConstants(origConstants)), mutators).map {
            mutation ->
            Program(origFunction.replaceRightSubFun(mutation.gpFunction), origFunction.leftConstants(origConstants) + mutation.constants)
        }
    }

    return mutations
}

fun allMutators(parameters: Int): List<Mutator> {
    val l = ArrayList<Mutator>()
    l += NegateMutator()
    l += CollapseUniFunMutator()
    l += CollapseLeftBiFunMutator()
    l += CollapsRightBiFunMutator()
    l += SwapBiFunMutator()
    l += AddZeroMutator()
    l += PowOfOneMutator()
    l += MultByOneMutator()
    l += DoubleThenHalveMutator()
    l += SquareThenRootMutator()
    l += PowWithDupeMutator()
    for (paramNo in 0..(parameters - 1)) {
        val p = P(paramNo)
        l += AddTermPlusMutator(p, emptyList())
        l += AddTermMultMutator(p, emptyList())
        l += AddTermPowMutator(p, emptyList())
        l += AddTermPowRMutator(p, emptyList())
    }

    l += AddTermPlusMutator(C(), listOf(1.0))
    l += AddTermPlusMutator(C(), listOf(-1.0))
    l += AddTermPowMutator(C(), listOf(-1.0))
    l += AddTermPowMutator(C(), listOf(0.5))
    l += AddTermPowRMutator(C(), listOf(0.5))
    l += AddTermMultMutator(C(), listOf(-1.0))
    l += AddTermMultMutator(C(), listOf(0.5))

/*
    l += UniFunctionInsertingMutator("Sine/AddTermMultByZeroMutator", ::Sine, ::AddTermPlusMutator)
    l += UniFunctionInsertingMutator("Sine/AddTermMultByOneMutator", ::Sine, ::AddTermMultMutator)
    l += UniFunctionInsertingMutator("Sine/AddTermPowOfOneMutator", ::Sine, ::AddTermPowMutator)
    l += UniFunctionInsertingMutator("Sine/AddTermPowOfOneMutator", ::Sine, ::AddTermPowRMutator)
*/
    return l
}

interface Mutator {
    fun mutate(const: List<Double>, f: GPFunction): MutatedFun?
}

data class MutatedFun(val mFun: GPFunction, val newConstants: List<Double>)

class AddZeroMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f + C(), const + 0.0)
    }
}

class CollapseUniFunMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        if (f is UniFunction) {
            return MutatedFun(f.subFun, const)
        } else {
            return null
        }
    }

}

class SwapBiFunMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        if (f is BiFunction) {
            return MutatedFun(f.replaceLeftSubFun(f.right).replaceRightSubFun(f.left), f.rightConstants(const) + f.leftConstants(const))
        } else {
            return null
        }
    }

}

class CollapseLeftBiFunMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        if (f is BiFunction) {
            return MutatedFun(f.left, f.leftConstants(const))
        } else {
            return null
        }
    }
}

class CollapsRightBiFunMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        if (f is BiFunction) {
            return MutatedFun(f.right, f.rightConstants(const))
        } else {
            return null
        }
    }
}

class UniFunctionInsertingMutator(val name: String, val uniFunctionFactory: (GPFunction) -> GPFunction, val termAdder: (GPFunction, List<Double>) -> Mutator) : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        val termToAdd = uniFunctionFactory(f)
        val mutatedFunction = termAdder(termToAdd, DoubleArray(termToAdd.numConstants, { 0.1 }).asList()).mutate(const, f)
        return mutatedFunction
    }

    override fun toString(): String {
        return "UniFunctionInsertingMutator($name)"
    }
}

class AddTermPlusMutator(val term : GPFunction, val termConstants: List<Double>) : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        return MutatedFun(f + term, const + termConstants)
    }
}

class AddTermMultMutator(val term : GPFunction, val termConstants: List<Double>) : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        return MutatedFun(f * term, const + termConstants)
    }
}

class AddTermPowMutator(val term : GPFunction, val termConstants: List<Double>) : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        return MutatedFun(f pow term, const + termConstants)
    }
}

class AddTermPowRMutator(val term : GPFunction, val termConstants: List<Double>) : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun? {
        return MutatedFun(term pow f, termConstants + const)
    }
}

class AddTermMultByZeroMutator(val term: GPFunction, val termConstants: List<Double>) : Mutator {
    init {
        assert(term.numConstants == termConstants.size)
    }

    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f + (C() * term), const + 0.0 + termConstants)
    }
}

class PowOfOneMutator() : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f pow C(), const + 1.0)
    }

}

class NegateMutator() : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(C() * f, listOf(-1.0) + const)
    }

}

class MultByOneMutator() : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f * C(), const + 1.0)
    }
}

class AddTermMultByOneMutator(val term: GPFunction, val termConstants: List<Double>) : Mutator {
    init {
        assert(term.numConstants == termConstants.size)
    }

    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f * (C() + (C() * term)), const + 1.0 + 0.0 + termConstants)
    }
}

class AddTermPowOfOneMutator(val term: GPFunction, val termConstants: List<Double>) : Mutator {
    init {
        assert(term.numConstants == termConstants.size)
    }

    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f pow (C() + (C() * term)), const + 1.0 + 0.0 + termConstants)
    }
}

class DoubleThenHalveMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun((f + f) * C(), const + const + 0.5)
    }
}

class SquareThenRootMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun((f * f) pow C(), const + const + 0.5)
    }

}

class PowWithDupeMutator : Mutator {
    override fun mutate(const: List<Double>, f: GPFunction): MutatedFun {
        return MutatedFun(f + (C() * (C() pow f)), const + arrayOf(0.0, 1.0) + const)
    }
}