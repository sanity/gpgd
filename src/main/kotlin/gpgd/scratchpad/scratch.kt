package gpgd.scratchpad

import gpgd.InputOutput
import gpgd.optimize

/**
 * Created by ian on 3/25/17.
 */

fun main(args: Array<String>) {
    val e = Math.E

    val data = ArrayList<InputOutput>()
    for (x in -40 .. 40) {
        val xv = x.toDouble() / 10.0
        data += InputOutput(listOf(xv), 1.0 / (1.0 + Math.pow(e, -xv)))
    }

    optimize(1, data)
}


