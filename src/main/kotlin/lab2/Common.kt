package lab2

import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.stage.Stage

open class SceneWrapper(open val primaryStage: Stage, val title: String) {
    var scene = Scene(Group())

    fun switch() {
        primaryStage.title = title
        primaryStage.scene = scene
    }
}

class HSVComponents constructor(var h: Double, var s: Double, var v: Double)
class HSLComponents constructor(var h: Double, var s: Double, var l: Double)

fun drawColorGist(
    plotCtx: GraphicsContext, axesCtx: GraphicsContext,
    color: Color,
    values: Array<Int>,
    x0: Double, y0: Double,
    width: Double, height: Double
) {
    val max = values.max()!!

    plotCtx.stroke = color
    val kX = width / values.size
    val kY = height / max
    plotCtx.lineWidth = kX
    for (x in (values.indices)) {
        if (values[x] == 0)
            continue
        val xCord = x0 + x * kX
        plotCtx.moveTo(xCord, y0)
        plotCtx.lineTo(xCord, y0 - values[x] * kY)
    }
    plotCtx.stroke()

    axesCtx.stroke = Color.PURPLE
    axesCtx.lineWidth = 1.0
    axesCtx.moveTo(x0, y0)
    axesCtx.lineTo(x0, y0 - height)
    axesCtx.moveTo(x0, y0)
    axesCtx.lineTo(x0 + width, y0)
    axesCtx.stroke()
    axesCtx.strokeText(max.toString(), x0 - 20, y0 - height - 1);
    axesCtx.strokeText(values.size.toString(), x0 + width - 20, y0 + 10);
}