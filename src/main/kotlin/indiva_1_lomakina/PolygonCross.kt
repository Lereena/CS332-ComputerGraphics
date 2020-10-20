package indiva_1_lomakina

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ToggleButton
import javafx.scene.layout.FlowPane
import javafx.stage.Stage
import lab4.lineFunction
import java.util.*

class Point (val x: Int, val y: Int, val cross: Boolean) {
    override fun toString(): String {
        return "($x, $y)"
    }
}

class PolygonCross : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Polygon Cross"
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        primaryStage.scene = Scene(root)

        val polygon1But = ToggleButton("First Polygon")
        val polygon2But = ToggleButton("Second Polygon")
        val clearBut = ToggleButton("Clear")
        val crossBut = ToggleButton("Cross")
        root.children.addAll(polygon1But, polygon2But, clearBut, crossBut)

        primaryStage.show()

        var p1 = LinkedList<Point>()
        var p2 = LinkedList<Point>()


        clearBut.setOnMouseClicked {
            p1.clear()
            p2.clear()
            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            gc.beginPath()
            clearBut.isSelected = false
        }

        crossBut.setOnMouseClicked {
            crossPolygons(canvas, gc, p1, p2)/*
            var res = addCrossPoints(gc, p1, p2)
            p1 = res.first
            p2 = res.second*/

            //redraw(canvas, gc, p1, p2)

            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            gc.beginPath()/*
            if (p1.size != 0) {
                var cur = p1.last
                for (index in 0 until p1.size) {
                    gc.moveTo(cur.x.toDouble(), cur.y.toDouble())
                    //gc.stroke = Color.LIGHTBLUE
                    gc.lineTo(p1[index].x.toDouble(), p1[index].y.toDouble())
                    var d = 5.0
                    if (p1[index].cross)
                        d = 10.0
                    gc.strokeOval(
                            p1[index].x - 2.5,
                            p1[index].y - 2.5, d, d);
                    cur = p1[index]
                    gc.stroke()
                }
            }*/
            if (p2.size != 0) {
                var cur = p2.last
                for (index in 0 until p2.size) {
                    gc.moveTo(cur.x.toDouble(), cur.y.toDouble())
                    //gc.stroke = Color.LIGHTBLUE
                    gc.lineTo(p2[index].x.toDouble(), p2[index].y.toDouble())
                    var d = 5.0
                    if (p1[index].cross)
                        d = 10.0
                    gc.strokeOval(
                            p2[index].x - 2.5,
                            p2[index].y - 2.5, d, d);
                    cur = p2[index]
                    gc.stroke()
                }
            }

            crossBut.isSelected = false
        }

        root.setOnMouseClicked {
            if (polygon1But.isSelected) {
                p1.add(Point(it.x.toInt(), it.y.toInt(), false))
                redraw(canvas, gc, p1, p2)
            }
            if (polygon2But.isSelected) {
                p2.add(Point(it.x.toInt(), it.y.toInt(), false))
                redraw(canvas, gc, p1, p2)
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(PolygonCross::class.java, *args)
        }
    }
}


fun crossPolygons(canvas: Canvas, gc: GraphicsContext, _p1: LinkedList<Point>, _p2: LinkedList<Point>) {
    val addCrosses = addCrossPoints(gc, _p1, _p2)
    var p1 = addCrosses.first
    var p2 = addCrosses.second

    //поиск самой левой точки
    var temp1 = findLeftPoint(p1)
    val temp2 = findLeftPoint(p2)
    var polNum = true //если true - идем по первому полигону, иначе - по второму
    var curPoint = temp1.first
    var index = temp1.second
    if (temp1.first.x > temp2.first.x) {
        polNum = false
        curPoint = temp2.first
        index = temp2.second
    }

    while (!curPoint.cross) {
        if (polNum) {
            if (index < p1.size) {
                curPoint = p1[index]
                index++
            }
            else index -= p1.size
        }
        else {
            if (index < p2.size) {
                curPoint = p2[index]
                index++
            }
            else index -= p2.size
        }
    }

    //тут будут храниться точки результата
    var result = LinkedList<Point>()
    result.add(curPoint)

    var iPoint = curPoint
    if (polNum) {
        if (index < p1.size) {
            iPoint = p1[index]
            index++
        }
        else iPoint = p1[0]
    }
    else {
        if (index < p2.size) {
            iPoint = p2[index]
            index++
        }
        else iPoint = p2[0]
    }

/*
    gc.strokeOval(
            iPoint.x - 2.5,
            iPoint.y - 2.5, 10.0, 10.0)
    gc.stroke()*/
    while (iPoint.x != curPoint.x && iPoint.y != curPoint.y) {
        result.add(iPoint)
        if (iPoint.cross) {
            polNum = !polNum
        }
        if (polNum) {
            for (i in 0 until p1.size)
                if (p1[i].x == iPoint.x && p1[i].y == iPoint.y)
                    index = i + 1
            if (index < p1.size)
                iPoint = p1[index]
            else iPoint = p1[0]
        }
        else {
            for (i in 0 until p2.size)
                if (p2[i].x == iPoint.x && p2[i].y == iPoint.y)
                    index = i + 1
            if (index < p2.size)
                iPoint = p2[index]
            else iPoint = p2[0]
        }
    }

    gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
    gc.beginPath()
    if (result.size != 0) {
        var cur = result.last
        for (index in 0 until result.size) {
            gc.moveTo(cur.x.toDouble(), cur.y.toDouble())
            //gc.stroke = Color.LIGHTBLUE
            gc.lineTo(result[index].x.toDouble(), result[index].y.toDouble())
            var d = 5.0
            if (result[index].cross)
                d = 10.0
            gc.strokeOval(
                    result[index].x - 2.5,
                    result[index].y - 2.5, d, d);
            cur = result[index]
            gc.stroke()
        }
    }
}

fun addCrossPoints(gc: GraphicsContext, p1: LinkedList<Point>, p2: LinkedList<Point>) :
                                    Pair<LinkedList<Point>, LinkedList<Point>> {
    var res1 = p1
    var res2 = p2

    var start1 = res1.last
    var i1 = 0
    while (i1 < res1.size) {
        val finish1 = res1[i1]
        val line1 = edgeToFunction(start1, finish1)
        var start2 = res2.last
        var i2 = 0
        while (i2 < res2.size){
            val finish2 = res2[i2]
            val line2 = edgeToFunction(start2, finish2)
            val crossPoint = getLinesIntersection(line1, line2)
/*
            gc.strokeOval(
                    crossPoint.x - 2.5,
                    crossPoint.y - 2.5, 10.0, 10.0);*/

            //проверяем, что точка лежит на ребре
            if ((crossPoint.x <= start1.x && (crossPoint.x >= finish1.x)) ||
                    (crossPoint.x >= start1.x && crossPoint.x <= finish1.x))
                if ((crossPoint.x <= start2.x && (crossPoint.x >= finish2.x)) ||
                        (crossPoint.x >= start2.x && crossPoint.x <= finish2.x))
                    if ((crossPoint.y <= start1.y && (crossPoint.y >= finish1.y)) ||
                            (crossPoint.y >= start1.y && crossPoint.y <= finish1.y))
                        if ((crossPoint.y <= start2.y && (crossPoint.y >= finish2.y)) ||
                                (crossPoint.y >= start2.y && crossPoint.y <= finish2.y)) {
                            //добавляем в полигоны
                            res1.add(i1, crossPoint)//Point((start1.x + finish1.x) / 2, (start1.y + finish1.y) / 2))
                            res2.add(i2, crossPoint)
                            i1++

                            start2 = crossPoint
                            i2++
                            //crossPointAdded = true
                        }
                        else {
                            start2 = finish2
                            i2++
                        }
        }
        start1 = finish1
        i1++
    }

    return Pair(res1, res2)
}


fun redraw(canvas: Canvas, gc: GraphicsContext, p1: LinkedList<Point>, p2: LinkedList<Point>) {
    gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
    gc.beginPath()
    if (p1.size != 0) {
        var cur = p1.last
        for (index in 0 until p1.size) {
            gc.moveTo(cur.x.toDouble(), cur.y.toDouble())
            //gc.stroke = Color.LIGHTBLUE
            gc.lineTo(p1[index].x.toDouble(), p1[index].y.toDouble())
            var d = 5.0
            if (p1[index].cross)
                d = 10.0
            gc.strokeOval(
                    p1[index].x - 2.5,
                    p1[index].y - 2.5, d, d);
            cur = p1[index]
        }
    }
    if (p2.size != 0) {
        var cur = p2.last
        for (index in 0 until p2.size) {
            gc.moveTo(cur.x.toDouble(), cur.y.toDouble())
            //gc.stroke = Color.LIGHTGREEN
            gc.lineTo(p2[index].x.toDouble(), p2[index].y.toDouble())
            var d = 5.0
            if (p1[index].cross)
                d = 10.0
            gc.strokeOval(
                    p2[index].x - 2.5,
                    p2[index].y - 2.5, d, d)
            cur = p2[index]
        }
    }
    gc.stroke()
}

class lineFunction(val k: Double, val b: Double) {
    fun eval(x: Double): Double {
        return k * x + b
    }
}

fun edgeToFunction(p1: Point, p2: Point): lineFunction {
    val k = (p2.y - p1.y).toDouble() / (p2.x - p1.x)
    val b = p1.y.toDouble() - p1.x * k
    return lineFunction(k, b)
}

fun getLinesIntersection(line1: lineFunction, line2: lineFunction): Point {
    val x = (line2.b - line1.b) / (line1.k - line2.k)
    return Point(x.toInt(), line1.eval(x).toInt(), true)
}

fun findLeftPoint(p: LinkedList<Point>) : Pair<Point, Int> {
    var res = p.first
    var index = 0
    for (i in 1 until p.size)
        if (p[i].x < res.x) {
            res = p[i]
            index = i
        }
    return Pair(res, index)
}