package gpgd.scratchpad.spiral

import gpgd.data.spiralData
import javafx.application.Application
import javafx.scene.chart.NumberAxis
import tornadofx.App
import tornadofx.View
import tornadofx.scatterchart


/**
 * Created by ian on 3/26/17.
 */

fun main(args: Array<String>) {
    Application.launch(SpiralApp::class.java, *args)
}

class SpiralApp : App(SpiralView::class) {

}

class SpiralView  : View() {
   override val root = scatterchart("Spiral", NumberAxis(), NumberAxis()) {
       val spiralData = spiralData()
   }
}
