package lab6

import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class Point3D(var x: Double, var y: Double, var z: Double)

enum class Axis { X, Y, Z }

data class DirectionVector(val l: Double, val m: Double, val n: Double)

class Polygon {
    val points = ArrayList<Point3D>()

    operator fun get(i: Int) = points[(i % points.size)]

    fun add(point: Point3D) = points.add(point)
}

class Polyhedron(filename: String) {
    var vertices = ArrayList<Point3D>()
    var polygons = ArrayList<Polygon>()

    init {
        File(filename).forEachLine {
            if (it.isNotEmpty()) {
                val sLine = it.split(' ')
                when (sLine[0]) {
                    "v" -> {
                        vertices.add(Point3D(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble()))
                    }
                    "f" -> {
                        val polygon = Polygon()
                        for (i in 1 until sLine.size) {
                            val number = sLine[i].substringBefore('/').toInt() - 1
                            polygon.add(vertices[number])
                        }
                        polygons.add(polygon)
                    }
                }
            }
        }
    }

    constructor(vertices: ArrayList<Point3D>, polygons: ArrayList<Polygon>) : this("") {
        this.vertices = vertices
        this.polygons = polygons
    }
}

typealias Matrix = Array<DoubleArray>

data class Line(val point1: Point3D, val point2: Point3D) {
    val directionVector = normalizeDirectionVector()

    private fun normalizeDirectionVector(): DirectionVector {
        val l = point2.x - point1.x
        val m = point2.y - point1.y
        val n = point2.z - point1.z
        val length = sqrt(l * l + m * m + n * n)
        return DirectionVector(l / length, m / length, n / length)
    }
}

fun multiplyMatrices(matrix1: Array<DoubleArray>, matrix2: Array<DoubleArray>): Array<DoubleArray> {
    val result = Array(matrix1.size) { DoubleArray(matrix2[0].size) { 0.0 } }
    for (i in matrix1.indices)
        for (j in matrix2[0].indices)
            for (k in matrix2.indices)
                result[i][j] += matrix1[i][k] * matrix2[k][j]

    return result
}