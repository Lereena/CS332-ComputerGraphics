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

class ARGBComponents constructor(val a: Int, val r: Int, val g: Int, val b: Int)

class HSVComponents constructor(h: Int, s: Int, v: Int)

fun evalARGBComponents(argb: Int) : ARGBComponents {
    var _argb = argb
    val b = argb and 255
    _argb = _argb shr 8
    val g = argb and 255
    _argb = _argb shr 8
    val r = argb and 255
    _argb = _argb shr 8
    val a = argb
    return ARGBComponents(a, r, g, b)
}

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
}