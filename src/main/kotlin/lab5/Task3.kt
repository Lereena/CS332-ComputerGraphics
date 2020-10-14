package lab5

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import lab3.Point
import lab3.drawLine
import java.util.*

class Task3(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 3") {
    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        scene = Scene(root)
        root.children.add(canvas)

        val closeBut = ToggleButton("Close spline")
        val deleteBut = ToggleButton("Delete point")
        val moveBut = ToggleButton("Move point")
        val clearBut = ToggleButton("Clear")
        val pointNum = TextField("1")
        root.children.addAll(deleteBut, moveBut, closeBut, clearBut, pointNum)
        primaryStage.show()

        var points = LinkedList<Point>()


        closeBut.setOnMouseClicked {
            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            gc.beginPath()

            points.add(points.size - 1, middle(points[points.size - 2], points[points.size - 1]))
            points.add(1, middle(points[0], points[1]))

            var index = 1
            drawLine(gc, Color.RED, points[index - 1], points[index + 1])
            while (index <= points.size - 4) {
                drawLineBezier(gc, points[index], points[index + 1], points[index + 2], points[index + 3])
                drawLine(gc, Color.RED, points[index + 2], points[index + 4])
                index += 3
            }
            drawLineBezier(gc, points[points.size - 2], points[points.size - 1],
                    points[0], points[1])
            //drawLine(gc, Color.RED, points[index + 2], points[index + 3])
            closeBut.isSelected = false
        }

        clearBut.setOnMouseClicked {
            points.clear()
            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            gc.beginPath()
            clearBut.isSelected = false
        }

        deleteBut.setOnMouseClicked {
            var index = pointNum.text.toInt()
            if (index < points.size) {
                if (index == 1) {
                    points.removeAt(1)
                    points.removeAt(0)
                    points.removeAt(1)
                    redraw(canvas, gc, points)
                } else if (((points.size - 4) / 3) + 2 == index) {
                    points.removeAt(points.size - 1)
                    points.removeAt(points.size - 1)
                    points.removeAt(points.size - 2)
                    redraw(canvas, gc, points)
                } else {
                    points.removeAt(3 * (index - 1) - 1)
                    points.removeAt(3 * (index - 1) - 1)
                    points.removeAt(3 * (index - 1) - 1)
                    redraw(canvas, gc, points)
                }
            }
            deleteBut.isSelected = false
        }

        root.setOnMouseClicked {

            points.add(Point(it.x.toInt(), it.y.toInt()))
            if (points.size % 2 == 0)
                drawLine(gc, Color.RED, points[points.size - 2], points[points.size - 1])

            //отрисовка первой дуги
            if (points.size == 4) {
                drawLineBezier(gc, points[0], points[1], points[2], points[3])
            }
            //при добавлении каждой следующей точки добавляются 2 дополнительные и по всему
            //списку points перерисовывается кривая с учетом новых точек
            if (points.size > 4) {
                //потом брать точки не посередине, а сливать со второй

                //тут добавляем 2 дополнительные точки
                points.add(points.size - 2, middle(points[points.size - 3], points[points.size - 2]))
                points.add(points.size - 1, middle(points[points.size - 2], points[points.size - 1]))
                //points.add(points.size - 2, Point(points[points.size - 3].x, points[points.size - 2].y))
                //points.add(points.size - 1, Point(points[points.size - 1].x, points[points.size - 1].y))

                redraw(canvas, gc, points)
            }
        }
    }
}

fun middle(p1: Point, p2: Point) : Point {
    var x = (p1.x + p2.x) / 2
    var y = (p1.y + p2.y) / 2
    return Point(x, y)
}

fun drawLineBezier(gc: GraphicsContext, p0: Point, p1: Point, p2: Point, p3: Point) {
    var startPoint = p0
    var endPoint = p3
    var t = 0.00
    while (t <= 1) {
        t += 0.005
        endPoint = tempPoint(p0, p1, p2, p3, t)
        gc.moveTo(startPoint.x.toDouble(), startPoint.y.toDouble())
        gc.lineTo(endPoint.x.toDouble(), endPoint.y.toDouble())
        startPoint = endPoint
    }
    gc.stroke()
}

fun tempPoint(p0: Point, p1: Point, p2: Point, p3: Point, t: Double): Point {
    return Point((p0.x * (1 - t) * (1 - t) * (1 - t) +
            3 * p1.x * (1 - t) * (1 - t) * t +
            3 * p2.x * (1 - t) * t * t +
            p3.x * t * t * t).toInt(),
        (p0.y * (1 - t) * (1 - t) * (1 - t) +
                3 * p1.y * (1 - t) * (1 - t) * t +
                3 * p2.y * (1 - t) * t * t +
                p3.y * t * t * t).toInt())
}

fun redraw(canvas: Canvas, gc: GraphicsContext, points: LinkedList<Point>) {
    gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
    gc.beginPath()
    // для каждой четверки с шагом 3 рисуем кривую безье
    var index = 0
    //drawLine(gc, Color.RED, points[index], points[index + 1])
    while (index <= points.size - 4) {
        drawLineBezier(gc, points[index], points[index + 1],
                points[index + 2], points[index + 3])
        drawLine(gc, Color.RED, points[index], points[index + 1])
        //drawLine(gc, Color.RED, points[index + 1], points[index + 2])
        drawLine(gc, Color.RED, points[index + 2], points[index + 3])
        index += 3
    }
}