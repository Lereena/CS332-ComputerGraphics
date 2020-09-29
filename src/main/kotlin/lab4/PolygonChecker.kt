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
        if (next.y >= y && cur.y <= y || next.y <= y && cur.y >= y) {
            val x_rib = (next.x * cur.y - cur.x * next.y
                    - (next.x - cur.x) * y) / (next.x - cur.x)
            if (x < x_rib)
                cnt++
        }
    if (cnt % 2 == 0)
        return false
    return true
}


fun checkEdgesIntersection(edges: Vector<Shape>): Vector<Point> {
    val result = Vector<Point>()
    for (main_line in edges) {
        for (other_line in edges) {
            //проверка пересекаются ли main_line и other_line,
            // если да — точку пересечения в result
        }
    }
    return result
            //throw NotImplementedError("NOT IMPLEMENTED")
}

fun polygonIsConvex(shape: Shape) : Convexity {
    var cur_line = shape.points.first
    var flag0 = false
    var flag1 = false
    for (next_line in shape.points) {
        if (flag0) {
            var cur_rib = shape.points.first
            for (next_rib in shape.points) {
                if (flag1) {
                    // Значения функции на точках ребер
                    val x0 = (next_line.x * cur_line.y - cur_line.x * next_line.y
                            - (cur_line.y - next_line.y) * cur_rib.x) / (next_line.x - cur_line.x)
                    val x1 = (next_line.x * cur_line.y - cur_line.x * next_line.y
                            - (cur_line.y - next_line.y) * next_rib.x) / (next_line.x - cur_line.x)
                    if (x0 > cur_rib.y && x1 < next_rib.y || x0 < cur_rib.y && x1 > next_rib.y)
                        return Convexity.NonConvex
                }
                flag1 = true
                cur_rib = next_rib
            }
        }
        flag0 = true
        cur_line = next_line
    }
    return Convexity.Convex
}

fun checkPolygon(shape: Shape, startPoint: Point) : Convexity {
    if (!checkIsIn(shape, startPoint))
        return Convexity.Undefined
    else return polygonIsConvex(shape)
}

class PolygonsCheckResult (val convex_count: Int, val nonconvex_count: Int) {}

fun checkPolygons(shapes: Vector<Shape>, startPoint: Point) : PolygonsCheckResult {
    var cnt_convex = 0
    var cnt_non_convex = 0
    for (s in shapes) {
        if (checkPolygon(s, startPoint) == Convexity.Convex)
            cnt_convex++
        if (checkPolygon(s, startPoint) == Convexity.NonConvex)
            cnt_non_convex++
    }
    return PolygonsCheckResult(cnt_convex, cnt_non_convex)
}

fun checkPointEdge(edge: Shape, startPoint: Point) : Position {
    //если yb·xa - xb·ya > 0 => b слева от Oa
    //если yb·xa - xb·ya < 0 => b справа от Oa
    var a = Point(edge.points.last.x - edge.points.first.x,
                  edge.points.last.y - edge.points.first.y)
    var b = Point(startPoint.x - edge.points.first.x,
                     startPoint.y - edge.points.first.y)
    if (b.y * a.x - b.x * a.y > 0)
        return Position.Left
    if (b.y * a.x - b.x * a.y < 0)
        return Position.Right
    return Position.Belongs
}
