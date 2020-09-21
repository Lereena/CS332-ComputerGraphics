package lab3

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import java.util.*
import kotlin.math.abs

class Task3(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 3") {
    init {
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        scene = Scene(root)

        var startPoint: Point?
        var endPoint: Point? = null

        root.setOnMouseClicked {
            startPoint = endPoint
            endPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
            if (startPoint != null && endPoint != null)
                drawLine(gc, Color.BLACK, startPoint!!, endPoint!!)
        }
    }

    private fun drawLine(gc: GraphicsContext, color: Color, startPoint: Point, endPoint: Point) {
        val line = getLine(startPoint, endPoint)
        val pixelWriter = gc.pixelWriter
        for (point in line)
            pixelWriter.setColor(point.x, point.y, color)
    }

    private fun getLine(startPoint: Point, endPoint: Point): LinkedList<Point> {
        val result = LinkedList<Point>()
        result.add(startPoint)

        val a = endPoint.y - startPoint.y
        val b = startPoint.x - endPoint.x
        val sign = if (abs(a) > abs(b)) 1 else -1
        val signA = if (a > 0) 1 else -1
        val signB = if (b > 0) 1 else -1

        var f = 0
        var x = startPoint.x
        var y = startPoint.y

        if (sign == -1)
            do {
                f += a * signA
                if (f > 0) {
                    f -= b * signB
                    y += signA
                }
                x -= signB
                result.add(Point(x, y))
            } while (x != endPoint.x || y != endPoint.y)
        else
            do {
                f += b * signB
                if (f > 0) {
                    f -= a * signA
                    x -= signB
                }
                y += signA
                result.add(Point(x, y))
            } while (x != endPoint.x || y != endPoint.y)

        return result
    }

}