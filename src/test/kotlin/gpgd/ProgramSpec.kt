package gpgd

import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 3/26/17.
 */
class ProgramSpec : FreeSpec() {
    init {
        "when simplifying constants" - {
            val p = Program((C() + C()) + P(0) + Sine(C()), listOf(1.0, 2.0, 0.5))
            p.gpFunction.size shouldEqual 8
            "pre-simplified program should work as expected" {
                p.gpFunction.value(p.constants, listOf(0.1)) shouldBe ((1.0 + 2.0 + 0.1 + Math.sin(0.5)) plusOrMinus 0.00001)
            }
            "simplified program" - {
                val simplified = p.simplifyConstants()
                "should have the correct size" {
                    simplified.gpFunction.size shouldEqual 5
                }
                "should have the correct number of constants" {
                    simplified.constants.size shouldEqual 2
                }
                "should have same result" {
                    simplified.gpFunction.value(simplified.constants, listOf(0.1)) shouldBe ((1.0 + 2.0 + 0.1 + Math.sin(0.5)) plusOrMinus 0.00001)
                }
            }
        }
    }
}