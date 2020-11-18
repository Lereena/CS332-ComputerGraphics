package lab6

import kotlin.math.sqrt

fun angleBetweenVectors(v1: DirectionVector, v2: DirectionVector): Double {
    return ((v1.l * v2.l + v1.m * v2.m + v1.n * v2.n)
            / (sqrt(v1.l * v1.l + v1.m * v1.m + v1.n * v1.n)
            * sqrt(v2.l * v2.l + v2.m * v2.m + v2.n * v2.n)))
}

fun checkIsInPolygon(point: Point3D, polygon: Polygon): Boolean {
    val result = classifyPoint(point, polygon.edges.first())
    for (i in (1 until polygon.edges.size)) {
        if (result != classifyPoint(point, polygon.edges[i]))
            return false
    }
    return true
}

fun zOfPolygon(polygon: Polygon): Double {
    var zSum = 0.0
    var zCnt = 0.0
    for (point in polygon.vertices) {
        zSum += point.z
        zCnt += 1.0
    }
    return zSum / zCnt
}

fun multiplyMatrices(left: Matrix, right: Matrix): Matrix {
    val result = Array(left.size) { DoubleArray(right[0].size) { 0.0 } }
    for (i in left.indices) // Rows
        for (j in right[0].indices) // Columns
            for (k in right.indices)
                result[i][j] += left[i][k] * right[k][j]

    return result
}
