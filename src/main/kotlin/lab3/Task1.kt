package lab3

import com.sun.glass.ui.Application
import javafx.application.Application.launch
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ToggleButton
import javafx.scene.layout.FlowPane
import javafx.stage.Stage
import lab2.SceneWrapper
import java.awt.Color
import java.util.*
import kotlin.math.abs

var coloredPoints: Array<Array<Int>> = Array(800, { Array(600, {0}) })

class Task1(override val primaryStage: Stage): SceneWrapper(primaryStage, "Task 1") {
    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        scene = Scene(root)
        root.children.add(canvas)

        var startPoint: Point?
        var endPoint: Point? = null


        val Button = ToggleButton("Начать заливку")
        root.children.add(Button)

        root.setOnMouseClicked {
            startPoint = endPoint
            endPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
            if (startPoint != null && endPoint != null)
                    drawLine(gc, javafx.scene.paint.Color.BLACK, startPoint!!, endPoint!!)
        }

        Button.setOnAction {
            root.setOnMouseClicked {
                val start = Point(it.sceneX.toInt(), it.sceneY.toInt())
                //drawLine(gc, javafx.scene.paint.Color.RED, Point(0, 0), Point(50, 100))
                zalivka(gc, start)
                //for (y in (0 until 600))
                    //for (x in (0 until 800))
                        //if (coloredPoints[x][y] == 1)
                            //drawLine(gc, javafx.scene.paint.Color.GREEN, Point(x,y), Point(x+1, y+1))
                //drawLine(gc, javafx.scene.paint.Color.RED, Point(55, 105), Point(100, 150))
            }
        }
    }

    private fun zalivka(gc: GraphicsContext, start: Point) {
        drawLine(gc, javafx.scene.paint.Color.RED, Point(55, 105), Point(100, 150))
        var x_l = start.x;
        var y_l = start.y;
        var x_r = start.x;
        var y_r = start.y;
        if (coloredPoints[start.x][start.y] == 0) {
            while (coloredPoints[x_l][y_l] != 1 && x_l > 0) {
                if (y_l + 1 < 600 && coloredPoints[x_l][y_l + 1] != 1)
                    zalivka(gc, Point(x_l, y_l + 1))
                //if (y_l - 1 > 0 && coloredPoints[x_l][y_l - 1] != 1)
                  //  zalivka(gc, Point(x_l, y_l - 1))
                x_l -= 1;
            }

            while (coloredPoints[x_r][y_r] != 1 && x_r < 800) {
                if (y_l + 1 < 600 && coloredPoints[x_l][y_l + 1] != 1)
                    zalivka(gc, Point(x_r, y_l + 1))
                //if (y_l - 1 > 0 && coloredPoints[x_l][y_l - 1] != 1)
                  //  zalivka(gc, Point(x_r, y_l - 1))
                x_r += 1;
            }
            while (x_l < x_r) {
                if (y_l + 1 < 600 && coloredPoints[x_l][y_l + 1] != 1)
                    zalivka(gc, Point(x_l, y_l + 1))
                if (y_l - 1 > 0 && coloredPoints[x_l][y_l - 1] != 1)
                    zalivka(gc, Point(x_l, y_l - 1))
                x_l += 1
            }
            drawLine(gc, javafx.scene.paint.Color.RED, Point(x_l, y_l), Point(x_r, y_r))
        }

        //drawLine(gc, javafx.scene.paint.Color.RED, startPoint!!, endPoint!!)
    }

    private fun drawLine(gc: GraphicsContext, color: javafx.scene.paint.Color, start: Point, end: Point) {
        val line = getLine(javafx.scene.paint.Color.BLACK, start, end)
        val pixelWriter = gc.pixelWriter
        for (point in line) {
            pixelWriter.setColor(point.x, point.y, color)
            coloredPoints[point.x][point.y] = 1
        }
    }

    private fun getLine(color: javafx.scene.paint.Color, start: Point, end: Point): LinkedList<Point> {
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
}