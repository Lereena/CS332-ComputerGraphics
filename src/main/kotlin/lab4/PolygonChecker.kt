package lab4

import lab3.Point
import java.util.*
import kotlin.math.min

enum class Convexity {Convex, NonConvex, Undefined}
enum class Position { Left, Right, Belongs }

class lineFunction(val k: Double, val b: Double) {
    fun eval(x: Double): Double {
        return k * x + b
    }
}

fun edgeToFunction(edge: Shape): lineFunction {
    val p1 = edge.points.first
    val p2 = edge.points.last

    val k = (p2.y - p1.y).toDouble() / (p2.x - p1.x)
    val b = p1.y.toDouble() - p1.x * k
    return lineFunction(k, b)
}

fun checkIsIn(shape: Shape, startPoint: Point) : Boolean {
    var x = startPoint.x
    var y = startPoint.y
    var cnt = 0.0
    var prevPoint = shape.points.last
    for (point in shape.points) {
        val edge = getEdge(prevPoint, point)
        val temp = checkPointEdge(edge, startPoint)

        if (point.y <= prevPoint.y && temp == Position.Right) {
            if (y > prevPoint.y && y < point.y ||
                    y < prevPoint.y && y > point.y)
                cnt += 1.0
            else if (y == prevPoint.y || y == point.y)
                cnt += 0.5
        }
        else if (point.y >= prevPoint.y && temp == Position.Left) {
            if (y > prevPoint.y && y < point.y ||
                    y < prevPoint.y && y > point.y)
                cnt += 1.0
            else if (y == prevPoint.y || y == point.y)
                cnt += 0.5
        }
        prevPoint = point
    }
    return (cnt.toInt() % 2) == 1
}

fun getLinesIntersection(line1: lineFunction, line2: lineFunction): Point {
    val x = (line2.b - line1.b) / (line1.k - line2.k)
    return Point(x.toInt(), line1.eval(x).toInt())
}


fun checkEdgesIntersection(edges: Vector<Shape>): Vector<Point> {
    val result = Vector<Point>()

    val funcs = Vector<lineFunction>()
    for (edge in edges)
        funcs.add(edgeToFunction(edge))

    for (i in (0 until edges.count() - 1)) {
        val func1 = funcs[i]
        for (j in (0 until edges.count())) {
            val func2 = funcs[j]

            result.add(getLinesIntersection(func1, func2))
        }
    }
    return result
}

fun getPolygonConvexivity(shape: Shape, anchorPoint: Point) : Convexity {
    if (!checkIsIn(shape, anchorPoint))
        return Convexity.Undefined

    var prevPoint = shape.points.last
    for (point in shape.points) {
        val temp = checkPointEdgeByCoords(
                anchorPoint.x, anchorPoint.y,
                prevPoint.x, prevPoint.y,
                point.x, point.y
        )
        if (temp == Position.Left)
            return Convexity.NonConvex
        prevPoint = point
    }
    return Convexity.Convex
}

class PolygonsCheckResult (val convex_count: Int, val nonconvex_count: Int) {}

fun checkPolygons(shapes: Vector<Shape>, startPoint: Point) : PolygonsCheckResult {
    var cnt_convex = 0
    var cnt_non_convex = 0
    for (shape in shapes) {
        when (getPolygonConvexivity(shape, startPoint)) {
            Convexity.Convex ->    { cnt_convex++ }
            Convexity.NonConvex -> { cnt_non_convex++ }
            else -> {}
        }
    }
    return PolygonsCheckResult(cnt_convex, cnt_non_convex)
}

fun checkPointEdgeByCoords(px: Int, py:Int, x1: Int, y1: Int, x2: Int, y2: Int): Position {
    var a = Point(x2 - x1, y2 - y1)
    var b = Point(px - x1, py - y1)

    if (b.y * a.x - b.x * a.y < 0)
        return Position.Left
    if (b.y * a.x - b.x * a.y > 0)
        return Position.Right
    return Position.Belongs
}

fun checkPointEdge(edge: Shape, startPoint: Point) : Position {
    //если yb·xa - xb·ya > 0 => b слева от Oa
    //если yb·xa - xb·ya < 0 => b справа от Oa
    val p1 = edge.points.first
    val p2 = edge.points.last
    return checkPointEdgeByCoords(
            startPoint.x, startPoint.y,
            p1.x, p1.y, p2.x, p2.y
    )
}
