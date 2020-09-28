package lab4

import lab3.Point
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
    val transformationMatrix = arrayOf(
        doubleArrayOf(cos(angle), sin(angle), 0.0),
        doubleArrayOf(-sin(angle), cos(angle), 0.0),
        doubleArrayOf(0.0, 0.0, 1.0)
    )

    return makeTransformation(transformationMatrix, polygon)
}

fun turnAroundCenter(polygon: LinkedList<Point>): LinkedList<Point> {
    throw NotImplementedError()
}

fun scaleAroundPoint(polygon: LinkedList<Point>, point: Point): LinkedList<Point> {
    throw NotImplementedError()
}

fun turnEdge(edge: LinkedList<Point>): LinkedList<Point> {
    throw NotImplementedError()
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