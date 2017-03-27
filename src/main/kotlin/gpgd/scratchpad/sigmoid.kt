package gpgd.scratchpad

import gpgd.data.spiralData
import gpgd.optimize

/**
 * Created by ian on 3/25/17.
 */

fun main(args: Array<String>) {
    val data = spiralData()
    for (d in data) {
        //println("${d.input[0]}\t${d.input[1]}")
    }
    optimize(2, data, {})
}
