package lab6

import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.*

data class Point3D(var x: Double, var y: Double, var z: Double)

enum class Axis { X, Y, Z }

data class DirectionVector(val l: Double, val m: Double, val n: Double)

class Polygon {
    var points = ArrayList<Point3D>()

    constructor(){}

    constructor(points: ArrayList<Point3D>) {
        this.points = points
    }

    val indices = points.indices
    operator fun get(i: Int) = points[(i % points.size)]
    fun add(point: Point3D) = points.add(point)
}

class Polyhedron {
    var vertices = ArrayList<Point3D>()
    var polygons = ArrayList<Polygon>()
    var centerPoint = Point3D(0.0, 0.0, 0.0)

    constructor(filename: String) {
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

    constructor(vertices: ArrayList<Point3D>, polygons: ArrayList<Polygon>,
                centerPoint: Point3D = Point3D(0.0, 0.0, 0.0)) {
        this.vertices = vertices
        this.polygons = polygons
        this.centerPoint = centerPoint
    }
}

fun getLinePolyhedron(line: Line): Polyhedron {
    val vertices = arrayListOf(line.point1, line.point2)
    val polygon = Polygon()
    polygon.add(line.point1)
    polygon.add(line.point2)
    val polygons = arrayListOf(polygon)
    return Polyhedron(vertices, polygons)
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

fun multiplyMatrices(left: Matrix, right: Matrix): Matrix {
    val result = Array(left.size) { DoubleArray(right[0].size) { 0.0 } }
    for (i in left.indices) // Rows
        for (j in right[0].indices) // Columns
            for (k in right.indices)
                result[i][j] += left[i][k] * right[k][j]

    return result
}
