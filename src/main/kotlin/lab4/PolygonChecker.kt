package lab4

import lab3.Point
import java.util.*

enum class Convexity {Convex, NonConvex, Undefined}
enum class Position { Left, Right, Belongs }

fun checkIsIn(shape: Shape, startPoint: Point) : Boolean {
    var x = startPoint.x
    var y = startPoint.y
    var cnt = 0;
    var cur = shape.points.first
    for (next in shape.points)
        if (next.y >= y && cur.y <= y || next.y <= y && cur.y >= y)
            if (x < next.x || x < cur.x)
                cnt++
    if (cnt % 2 == 0)
        return false
    return true
}

fun checkEdgesIntersection(edges: Vector<Shape>): Vector<Point> {
    val result = Vector<Point>()
    // TODO: Найти пересечения между парами отрезков
    throw NotImplementedError("NOT IMPLEMENTED")
}

fun checkPolygon(shape: Shape, startPoint: Point) : Convexity {
    if (!checkIsIn(shape, startPoint))
        return Convexity.Undefined
    //тут вместо else определяем выпуклый ли полигон
    else return Convexity.Convex
}

class PolygonsCheckResult (val convex_count: Int, val nonconvex_count: Int) {}

fun checkPolygons(shapes: Vector<Shape>, startPoint: Point) : PolygonsCheckResult {
    /* TODO: Проверить, входит ли точка в какие-либо многоугольники.
        Вернуть количество выпуклых и невыпуклых многоугольников
        Думаю, что если фигура состоит из 1/2 точек, то можно игнорировать
    */
//    throw NotImplementedError("NOT IMPLEMENTED")

    return PolygonsCheckResult(0, 0)
}

fun checkPointEdge(edge: Shape, startPoint: Point) : Position {
    //если yb·xa - xb·ya > 0 => b слева от Oa
    //если yb·xa - xb·ya < 0 => b справа от Oa
    val a = edge.points.first
    val b = edge.points.last
    if (b.y * a.x - b.x * a.y > 0)
        return Position.Left
    if (b.y * a.x - b.x * a.y < 0)
        return Position.Right
    return Position.Belongs
}
