package lab3

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ToggleButton
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import java.util.*
import kotlin.math.abs

class Point(val x: Int, val y: Int, val color: Color = Color.BLACK) {
    override fun toString(): String {
        return "($x, $y)"
    }
}

class Task3(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 3") {
    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        scene = Scene(root)
        root.children.add(canvas)

        var startPoint: Point?
        var endPoint: Point? = null

        val wuButton = ToggleButton("Сглаживание")
        root.children.add(wuButton)

        root.setOnMouseClicked {
            startPoint = endPoint
            endPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
            if (startPoint != null && endPoint != null)
                if (wuButton.isSelected)
                    drawWuLine(gc, Color.BLACK, startPoint!!, endPoint!!)
                else
                    drawLine(gc, Color.BLACK, startPoint!!, endPoint!!)
        }
    }
}

fun drawLine(gc: GraphicsContext, color: Color, start: Point, end: Point) {
    val line = getLine(Color.BLACK, start, end)
    val pixelWriter = gc.pixelWriter
    for (point in line)
        pixelWriter.setColor(point.x, point.y, color)
}

private fun getLine(color: Color, start: Point, end: Point): LinkedList<Point> {
    val result = LinkedList<Point>()
    result.add(start)

    val a = end.y - start.y
    val b = start.x - end.x
    val sign = if (abs(a) > abs(b)) 1 else -1
    val signA = if (a > 0) 1 else -1
    val signB = if (b > 0) 1 else -1

    var f = 0
    var x = start.x
    var y = start.y

    if (sign == -1)
        do {
            f += a * signA
            if (f > 0) {
                f -= b * signB
                y += signA
            }
            x -= signB
            result.add(Point(x, y, color))
        } while (x != end.x || y != end.y)
    else
        do {
            f += b * signB
            if (f > 0) {
                f -= a * signA
                x -= signB
            }
            y += signA
            result.add(Point(x, y, color))
        } while (x != end.x || y != end.y)

    return result
}

fun drawWuLine(gc: GraphicsContext, color: Color, start: Point, end: Point) {
    val line = getWuLine(color, start, end)
    val pixelWriter = gc.pixelWriter
    for (point in line)
        pixelWriter.setColor(point.x, point.y, point.color)
}

private fun getWuLine(color: Color, start: Point, end: Point): LinkedList<Point> {
    val result = LinkedList<Point>()
    val dx = abs(start.x - end.x)
    val dy = abs(start.y - end.y)
    if (dx == 0 || dy == 0)
        return getLine(color, start, end)

    var x0 = start.x
    var x1 = end.x
    var y0 = start.y
    var y1 = end.y

    if (dy < dx) {
        if (x1 < x0) {
            x1 += x0; x0 = x1 - x0; x1 -= x0
            y1 += y0; y0 = y1 - y0; y1 -= y0
        }
        val grad = if (y1 > y0) 1.0 * dy / dx else - 1.0 * dy / dx
        var intery = y0 + grad
        result.add(Point(x0, y0, Color(color.red, color.green, color.blue, 1.0)))

        for (x in (x0 + 1) until x1) {
            result.add(
                Point(
                    x, intery.toInt(),
                    Color(
                        color.red, color.blue, color.green,
                        (255 - (intery - intery.toInt()) * 255) / 255
                    )
                ))
            result.add(
                Point(
                    x, intery.toInt() + 1,
                    Color(
                        color.red, color.blue, color.green,
                        (intery - intery.toInt())
                    )))
            intery += grad
        }
        result.add(Point(x1, y1, Color(color.red, color.green, color.blue, 1.0)))
    }
    else {
        if (y1 < y0) {
            x1 += x0; x0 = x1 - x0; x1 -= x0
            y1 += y0; y0 = y1 - y0; y1 -= y0
        }
        val gradient = if (x1 > x0) 1.0 * dx / dy else - 1.0 * dx / dy
        var interx = x0 + gradient
        result.add(Point(x0, y0, Color(color.red, color.green, color.blue, 1.0)))

        for (y in (y0 + 1) until y1) {
            result.add(
                Point(
                    interx.toInt(), y,
                    Color(
                        color.red, color.blue, color.green,
                        (255 - (interx - interx.toInt()) * 255) / 255
                    )
                ))
            result.add(
                Point(
                    interx.toInt() + 1, y,
                    Color(
                        color.red, color.blue, color.green,
                        (interx - interx.toInt())
                    )))
            interx += gradient
        }
        result.add(Point(x1, y1, Color(color.red, color.green, color.blue, 1.0)))
    }

    return result
}