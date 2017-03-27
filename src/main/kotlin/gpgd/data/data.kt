package gpgd.data

import gpgd.InputOutput

/**
 * Created by ian on 3/26/17.
 */

fun spiralData() : List<InputOutput>{
    val data = ArrayList<InputOutput>()
    genSpiral(400, 0.0, 1.0, data) // Positive
    genSpiral(400, Math.PI, -1.0, data) // Negative
    return data
}


fun sigmoidData(): ArrayList<InputOutput> {
    val e = Math.E
    val data = ArrayList<InputOutput>()
    for (x in -40..40) {
        val xv = x.toDouble() / 10.0
        data += InputOutput(listOf(xv), 1.0 / (1.0 + Math.pow(e, -xv)))
    }
    return data
}


private fun genSpiral(samples : Int, deltaT: Double, label: Double, addTo : ArrayList<InputOutput>) {
    for (i in 0 .. (samples / 2)) {
        val r = i.toDouble() / samples * 5.0;
        val t = 2.0* 1.75 * i / samples * 2 * Math.PI + deltaT;
        val x = r * Math.sin(t)
        val y = r * Math.cos(t)
        addTo += InputOutput(listOf(x, y), label);
    }
}