package lab4

import lab3.Point
import java.security.InvalidParameterException
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun move(polygon: LinkedList<Point>, dx: Int, dy: Int): LinkedList<Point> {
    val transformationMatrix = arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0),
        doubleArrayOf(0.0, 1.0, 0.0),
        doubleArrayOf(-dx.toDouble(), -dy.toDouble(), 1.0)
    )

    return makeTransformation(transformationMatrix, polygon)
}

fun turnAroundPoint(polygon: LinkedList<Point>, point: Point, angle: Double): LinkedList<Point> {
    val angleCos = cos(angle)
    val angleSin = sin(angle)
    val x = point.x
    val y = point.y
    val transformationMatrix = arrayOf(
        doubleArrayOf(angleCos, angleSin, 0.0),
        doubleArrayOf(-angleSin, angleCos, 0.0),
        doubleArrayOf(-x * angleCos + y * angleSin + x,
                        -x * angleSin - y * angleCos + y,
                        1.0)
    )

    return makeTransformation(transformationMatrix, polygon)
}

fun turnAroundCenter(polygon: LinkedList<Point>, angle: Double): LinkedList<Point> {
    return turnAroundPoint(polygon, findCenter(polygon), angle)
}

fun scaleAroundPoint(polygon: LinkedList<Point>, point: Point, kX: Double, kY: Double): LinkedList<Point> {
    val transformationMatrix = arrayOf(
        doubleArrayOf(kX, 0.0, 0.0),
        doubleArrayOf(0.0, kY, 0.0),
        doubleArrayOf((1 - kX) * point.x, (1 - kY) * point.y, 1.0)
    )

    return makeTransformation(transformationMatrix, polygon)
}

fun scaleAroundCenter(polygon: LinkedList<Point>, kX: Double, kY: Double): LinkedList<Point> {
    return scaleAroundPoint(polygon, findCenter(polygon), kX, kY)
}

fun findCenter(polygon: LinkedList<Point>): Point {
    val n = polygon.size
    if (n == 2)
        return findEdgeCenter(polygon)
    var x = 0
    var y = 0
    var area = 0

    for (i in 0..(n - 2)) {
        val current = polygon[i]
        val next = polygon[i + 1]
        val mult =  (current.x * next.y - next.x * current.y)
        x += (current.x + next.x) * mult
        y += (current.y + next.y) * mult
        area += mult
    }
    val first = polygon[0]
    val last = polygon[n - 1]
    val mult = (last.x * first.y - first.x * last.y)
    x += (last.x + first.x) * mult
    y += (last.y + first.y) * mult
    area += mult
    area /= 2
    x /= 6 * area
    y /= 6 * area

    return Point(x, y)
}

fun findEdgeCenter(edge: LinkedList<Point>): Point {
    if (edge.size != 2)
        throw InvalidParameterException("Передано не ребро")
    val a = edge.first
    val b = edge.last
    return Point((a.x + b.x) / 2, (a.y + b.y) / 2)
}

fun makeTransformation(transformationMatrix: Array<DoubleArray>, polygon: LinkedList<Point>): LinkedList<Point> {
    val result = LinkedList<Point>()
    for (point in polygon) {
        val matrix = arrayOf(doubleArrayOf(point.x.toDouble(), point.y.toDouble(), 1.0))
        val newMatrix = matrixMultiplication(matrix, transformationMatrix)
        result.addLast(Point(newMatrix[0][0], newMatrix[0][1]))
    }

    return result
}

fun matrixMultiplication(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<IntArray> {
    val result = arrayOf(doubleArrayOf(0.0, 0.0, 0.0)) // хардкодед, сорян
    for (i in a.indices)
        for (j in b[0].indices)
            for (k in a[0].indices) {
                result[i][j] += a[i][k] * b[k][j]
            }
    val intResult = arrayOf(intArrayOf(0, 0, 0))
    for (i in result[0].indices)
        intResult[0][i] = result[0][i].roundToInt()
    return intResult
}