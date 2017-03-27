package gpgd

import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 3/25/17.
 */

class FunctionsSpec : FreeSpec() {
    init {
        "Sin" {
            Sin(C()).value(listOf(0.3), emptyList()) shouldEqual (Math.sin(0.3) plusOrMinus 0.00001)
        }
        "Plus" {
            Plus(C(), C()).value(listOf(1.0, 2.0), emptyList()) shouldEqual (3.0 plusOrMinus 0.00001)
        }
        "Times" {
            Times(C(), C()).value(listOf(3.0, 2.0), emptyList()) shouldEqual (6.0 plusOrMinus 0.00001)
        }
        "Pow" {
            Pow(C(), C()).value(listOf(3.0, 2.0), emptyList()) shouldEqual (9.0 plusOrMinus 0.00001)
        }
        "Param" {
            P(0).value(emptyList(), listOf(2.0)) shouldBe 2.0
            P(1).value(emptyList(), listOf(1.0, 2.0)) shouldBe 2.0
        }
    }
}