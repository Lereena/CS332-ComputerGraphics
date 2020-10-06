package ind1kostikova

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ToggleButton
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab3.Point
import java.util.*

class ConvexHull : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Convex Hull (Andrew)"
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val width = 800.0
        val height = 600.0
        val canvas = Canvas(width, height)
        val dotsGC = canvas.graphicsContext2D
        val hullGC = canvas.graphicsContext2D

        dotsGC.stroke = Color.BLACK
        hullGC.stroke = Color.BLUEVIOLET

        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        val pointsButton = ToggleButton("Ставить точки")
        val hullButton = ToggleButton("Получить выпуклую оболочку")
        root.children.addAll(pointsButton, hullButton)
        primaryStage.show()

        val canvasPoints = LinkedList<Point>()

        pointsButton.setOnMouseClicked {
            if (hullButton.isSelected) {
                dotsGC.clearRect(0.0, 0.0, canvas.width, canvas.height)
                canvasPoints.clear()
            }
            hullButton.isSelected = false
        }

        hullButton.setOnMouseClicked {
            pointsButton.isSelected = false
            drawHull(hullGC, getHull(canvasPoints))
        }

        root.setOnMouseClicked {
            if (pointsButton.isSelected) {
                val point = Point(it.sceneX.toInt(), it.sceneY.toInt())
                dotsGC.strokeOval(point.x - 1.0, point.y - 1.0, 2.0, 2.0)
                canvasPoints.add(point)
            }
        }
    }

    fun drawHull(gc: GraphicsContext, hull: List<Point>) {
        for (i in 0 until hull.size - 1) {
            val currentPoint = hull[i]
            val nextPoint = hull[i + 1]
            gc.moveTo(currentPoint.x.toDouble(), currentPoint.y.toDouble())
            gc.lineTo(nextPoint.x.toDouble(), nextPoint.y.toDouble())
        }
        val firstPoint = hull[0]
        val lastPoint = hull[hull.size - 1]
        gc.moveTo(firstPoint.x.toDouble(), firstPoint.y.toDouble())
        gc.lineTo(lastPoint.x.toDouble(), lastPoint.y.toDouble())
        gc.stroke()
    }

    fun getHull(points: LinkedList<Point>): List<Point> {
        if (points.size <= 3)
            return points

        points.sortWith(compareBy({ it.x }, { it.y }))
        val first = points.first
        val last = points.last
        val up = LinkedList<Point>(); up.push(first)
        val down = LinkedList<Point>(); down.push(first)

        for (i in 1 until points.size) {
            val point = points[i]
            val isLast = i == points.size - 1
            if (isLast || clockwise(first, point, last))
                putInPart(up, point) { a, b, c -> clockwise(a, b, c) }
            if (isLast || counterClockwise(first, point, last))
                putInPart(down, point) { a, b, c -> counterClockwise(a, b, c) }
        }

        return concat(up, down)
    }

    private fun concat(up: LinkedList<Point>, down: LinkedList<Point>): LinkedList<Point> {
        for (i in (down.size - 2) downTo 1)
            up.addLast(down[i])

        return up
    }

    private fun putInPart(part: LinkedList<Point>, point: Point, direction: (Point, Point, Point) -> Boolean) {
        while (part.size >= 2 && !direction(part[part.size - 2], part[part.size - 1], point))
            part.removeLast()

        part.addLast(point)
    }

    private fun orientedArea(p1: Point, p2: Point, p3: Point): Int {
        return p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)
    }

    private fun clockwise(p1: Point, p2: Point, p3: Point): Boolean {
        return orientedArea(p1, p2, p3) < 0
    }

    private fun counterClockwise(p1: Point, p2: Point, p3: Point): Boolean {
        return orientedArea(p1, p2, p3) > 0
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ConvexHull::class.java, *args)
        }
    }
}
