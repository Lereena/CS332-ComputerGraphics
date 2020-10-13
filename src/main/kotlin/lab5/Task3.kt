package lab5

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
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

        var points = LinkedList<Point>()
        root.setOnMouseClicked {

            points.add(Point(it.x.toInt(), it.y.toInt()))
            //направляющие
            /*if (points.size % 2 == 0)
                drawLine(gc, Color.RED, points[points.size - 2], points[points.size - 1])*/

            //отрисовка первой дуги
            if (points.size == 4) {
                drawLineBezier(gc, points[0], points[1], points[2], points[3])
            }
            //при добавлении каждой следующей точки добавляются 2 дополнительные и по всему
            //списку points перерисовывается кривая с учетом новых точек
            if (points.size > 4) {
                //потом брать точки не посередине, а сливать со второй
                gc.clearRect(0.0, 0.0, 800.0, 800.0)
                gc.beginPath()

                //тут добавляем 2 дополнительные точки
                points.add(points.size - 2, middle(points[points.size - 3], points[points.size - 2]))
                points.add(points.size - 1, middle(points[points.size - 2], points[points.size - 1]))

                // для каждой четверки с шагом 3 рисуем кривую безье и ОНО НЕ РАБОТАЕТ
                // !!!
                var index = 0
                while (index <= points.size - 4) {
                    drawLineBezier(gc, points[index], points[index + 1], points[index + 2], points[index + 3])
                    index += 3
                }
            }
        }

        //drawLineBezier(gc, Point(50, 200), Point(100, 100),
          //Point(200, 100), Point(250, 200))
        //drawLineBezier(gc, Point(50, 300), Point(100, 200),
          //      Point(200, 200), Point(250, 300))

        //drawLine(gc, Color.BLACK, Point(100,100), Point(100, 100))
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
        endPoint = formula(p0, p1, p2, p3, t)
        gc.moveTo(startPoint.x.toDouble(), startPoint.y.toDouble())
        gc.lineTo(endPoint.x.toDouble(), endPoint.y.toDouble())
        startPoint = endPoint
    }
    gc.stroke()
}

fun formula(p0: Point, p1: Point, p2: Point, p3: Point, t: Double): Point {
    return Point((p0.x * (1 - t) * (1 - t) * (1 - t) +
            3 * p1.x * (1 - t) * (1 - t) * t +
            3 * p2.x * (1 - t) * t * t +
            p3.x * t * t * t).toInt(),
        (p0.y * (1 - t) * (1 - t) * (1 - t) +
                3 * p1.y * (1 - t) * (1 - t) * t +
                3 * p2.y * (1 - t) * t * t +
                p3.y * t * t * t).toInt())
}