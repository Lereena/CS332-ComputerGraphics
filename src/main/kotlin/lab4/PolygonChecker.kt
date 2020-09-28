package lab4

import lab3.getLine
import javafx.scene.canvas.GraphicsContext
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import lab2.SceneWrapper
import lab3.Point
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.sound.sampled.AudioSystem.getLine
import kotlin.math.abs

enum class Convexity {Convex, NonConvex, Undefined}
enum class Position { Left, Right, Belongs }

class PolygonChecker(val ctx: GraphicsContext) {

    fun checkIsIn(points: LinkedList<Point>, startPoint: Point) : Boolean {
        var x = startPoint.x
        var y = startPoint.y
        var cnt = 0;
        var cur = points.first
        for (next in points)
            if (next.y >= y && cur.y <= y || next.y <= y && cur.y >= y)
                if (x < next.x || x < cur.x)
                    cnt++
        if (cnt % 2 == 0)
            return false
        return true
    }

    fun checkPolygon(points: LinkedList<Point>, startPoint: Point) : Convexity {
        if (!checkIsIn(points, startPoint))
            return Convexity.Undefined
        //тут вместо else определяем выпуклый ли полигон
        else return Convexity.Convex
    }

    fun leftOrRight(points: LinkedList<Point>, startPoint: Point) : Position {
        //если yb·xa - xb·ya > 0 => b слева от Oa
        //если yb·xa - xb·ya < 0 => b справа от Oa
        val a = points.first
        val b = points.last
        if (b.y * a.x - b.x * a.y > 0)
            return Position.Left
        if (b.y * a.x - b.x * a.y < 0)
            return Position.Right
        return Position.Belongs
    }
}
