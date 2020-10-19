package lab6

import java.util.*

data class Point3D(var x: Double, var y: Double, var z: Double)

enum class Axis { X, Y, Z }

data class DirectionVector(val l: Double, val m: Double, val n: Double)

typealias Polyhedron = LinkedList<Polygon>

typealias Polygon = LinkedList<Line>

data class Line(val point1: Point3D, val point2: Point3D) {
    val directionVector = DirectionVector(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z)
}

fun multiplyMatrices(matrix1: Array<DoubleArray>, matrix2: Array<DoubleArray>): Array<DoubleArray> {
    val result = Array(matrix1.size) { DoubleArray(matrix2[0].size) { 0.0 } }
    for (i in matrix1.indices)
        for (j in matrix2[0].indices)
            for (k in matrix2.indices)
                result[i][j] += matrix1[i][k] * matrix2[k][j]

    return result
}
