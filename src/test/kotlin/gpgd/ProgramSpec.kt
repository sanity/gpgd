package gpgd

import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 3/26/17.
 */
class ProgramSpec : FreeSpec() {
    init {
        "when simplifying constants" - {
            val p = Program((C() + C()) + P(0) + Sin(C()), listOf(1.0, 2.0, 0.5))
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
            "symmetric functions should swap if right is constant" {
                val p = Program(P(0) + C(), listOf(1.0))
                val simplified = p.simplifyConstants()
                ((simplified.gpFunction as BiFunction).left is C) shouldBe true
                ((simplified.gpFunction as BiFunction).right is P) shouldBe true
            }
            "simplifications for specific constants" - {
                "anything multiplied by zero is zero" {
                    val p = Program(C() * C(), listOf(0.0, 0.7))
                    val simplified = p.simplifyConstants()
                    (simplified.gpFunction is C) shouldBe true
                    simplified.constants.size shouldBe 1
                    simplified.constants[0] shouldBe (0.0 plusOrMinus 0.00001)
                }
                "anything multiplied by 1 is itself" {
                    val p = Program(C() * C(), listOf(1.0, 0.7))
                    val simplified = p.simplifyConstants()
                    (simplified.gpFunction is C) shouldBe true
                    simplified.constants.size shouldBe 1
                    simplified.constants[0] shouldBe (0.7 plusOrMinus 0.00001)
                }
                "one to the power of anything is one" {
                    val p = Program(C() pow C(), listOf(1.0, 0.7))
                    val simplified = p.simplifyConstants()
                    (simplified.gpFunction is C) shouldBe true
                    simplified.constants.size shouldBe 1
                    simplified.constants[0] shouldBe (1.0 plusOrMinus 0.00001)
                }
            }
        }
    }
}