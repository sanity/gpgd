package gpgd

import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 3/25/17.
 */

class MutateSpec : FreeSpec() {
    private val tolerance = 0.000001

    init {
        "PowOfOneMutator applied to a single Constant" - {
            val mutators = listOf(PowOfOneMutator())
            val constant = C()
            val mutations = mutate(Program(constant, listOf(2.0)), mutators)
            "should return one mutation" {
                mutations.size shouldBe 1
            }
            val mutation = mutations[0]
            "which should produce a 'pow' function at the top level" {
                (mutation.gpFunction is Pow) shouldBe true
            }
            "we should now have two constants, the original plus a new one as the power" {
                mutation.constants shouldBe listOf(2.0, 1.0)
            }
        }

        "A single PowOfOneMutator applied to a function 'C+P(0)'" - {
            val mutators = listOf(PowOfOneMutator())
            val origFunction = C() + P(0)
            "function should work as expected prior to mutation" {
                origFunction.value(listOf(2.0), listOf(1.0)) shouldEqual 3.0
            }
            val mutations = mutate(Program(origFunction, listOf(2.0)), mutators)
            "should produce 3 mutations" {
                mutations.size shouldEqual 3
            }
            "with the appropriate gpFunctions" {
                (mutations.map { it.gpFunction }) should containInAnyOrder<GPFunction>(
                        ((C() + P(0)) pow C()),
                        (C() pow C()) + P(0),
                        (C() + (P(0) pow C()))
                )
            }
            "mutations should not change a calculated value" {
                for (mutation in mutations) {
                    mutation.gpFunction.value(mutation.constants, listOf(1.0)) shouldEqual 3.0
                }
            }
        }
        "mutators should be function preserving" - {
            "applied to a function P(0)" - {
                val origFunction = P(0)
                "function should work as expected" {
                    origFunction.value(emptyList(), listOf(2.0)) shouldEqual (2.0 plusOrMinus tolerance)
                }
                "for mutators" - {
                    for (mutator in allMutators(1)) {
                        "${mutator::class.simpleName}" {
                            val mutation = mutator.mutate(emptyList(), origFunction)
                            mutation.mFun.value(mutation.newConstants, listOf(2.0)) shouldBe (2.0 plusOrMinus tolerance)
                        }
                    }
                }
            }
        }
    }
}