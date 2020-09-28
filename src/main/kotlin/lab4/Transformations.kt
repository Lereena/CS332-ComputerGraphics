package lab4

import lab3.Point
import java.util.*

fun move(polygon: LinkedList<Point>, dx: Int, dy: Int): LinkedList<Point> {
    val transformationMatrix = arrayOf(
        intArrayOf(1, 0, 0),
        intArrayOf(0, 1, 0),
        intArrayOf(-dx, -dy, 1),
    )
    val result = LinkedList<Point>()
    for (point in polygon) {
        val matrix = arrayOf(intArrayOf(point.x, point.y, 1))
        val newMatrix = matrixMultiplication(matrix, transformationMatrix)
        result.addLast(Point(newMatrix[0][0], newMatrix[0][1]))
    }

    return result
}

fun turnAroundPoint(polygon: LinkedList<Point>, point: Point): LinkedList<Point> {
    throw NotImplementedError()
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


fun matrixMultiplication(a: Array<IntArray>, b: Array<IntArray>): Array<IntArray> {
    val result = arrayOf(intArrayOf(0, 0, 0)) // хардкодед, сорян
    for (i in a.indices)
        for (j in b[0].indices)
            for (k in a[0].indices) {
                val res = a[i][k] * b[k][j]
                result[i][j] += a[i][k] * b[k][j]
            }
    return result
}