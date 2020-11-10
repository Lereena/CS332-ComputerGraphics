package lab6

import java.awt.Point
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.*

data class Point3D(var x: Double, var y: Double, var z: Double) {
    constructor(other: Point3D): this(other.x, other.y, other.z)

    override fun equals(other: Any?): Boolean {
        if (other is Point3D)
            return this.x == other.x &&
                this.y == other.y &&
                this.z == other.z
        return false
    }

    operator fun minus(other: Point3D): Point3D {
        return Point3D(x - other.x, y - other.y, z - other.z)
    }

    operator fun plus(other: Point3D): Point3D {
        return Point3D(x + other.x, y + other.y, z + other.z)
    }
}

enum class Axis { X, Y, Z }

data class DirectionVector(var l: Double, var m: Double, var n: Double) {
    constructor(p: Point3D): this(p.x, p.y, p.z)

    init {
        val len = Math.sqrt(l * l + m * m + n * n)
        l /= len
        m /= len
        n /= len
    }

    operator fun plus(other: DirectionVector): DirectionVector {
        return DirectionVector(l + other.l, m + other.m, n + other.n)
    }
}

class Polygon {
    var points = ArrayList<Point3D>()

    constructor() {}

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
    var normals = ArrayList<DirectionVector>()
    var centerPoint = Point3D(0.0, 0.0, 0.0)

    constructor(filename: String) {
        File(filename).forEachLine {
            if (it.isNotEmpty()) {
                val sLine = it.split(' ')
                when (sLine[0]) {
                    "v" -> {
                        vertices.add(Point3D(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble()))
                    }
                    "vn" -> {
                        normals.add(DirectionVector(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble()))
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

    constructor(
        vertices: ArrayList<Point3D>, polygons: ArrayList<Polygon>,
        centerPoint: Point3D = Point3D(0.0, 0.0, 0.0)
    ) {
        this.vertices = vertices
        this.polygons = polygons
        this.centerPoint = centerPoint
        for (polygon in polygons) {
            if (polygon.points.size < 3)
                continue
//                throw Exception("Меньше трёх точек в полигоне")
            normals.add(findNormal(polygon[0], polygon[1], polygon[2]))
        }
    }

    fun copy(): Polyhedron {
        val vertices = ArrayList<Point3D>()
        val polygons = ArrayList<Polygon>()
        val centerPoint = Point3D(this.centerPoint)
        for (polygon in this.polygons) {
            val tempPoints = ArrayList<Point3D>()
            for (point in polygon.points)
                tempPoints.add(Point3D(point))
            polygons.add(Polygon(tempPoints))
            vertices.addAll(tempPoints)
        }
        return Polyhedron(vertices, polygons, centerPoint)
    }

    fun faces(viewVector: DirectionVector): Array<Boolean> {
        val result = Array(polygons.size) { false }

        for (i in result.indices) {
            val polygon = polygons[i]
            if (polygon.points.size < 3) {
                result[i] = true
                continue
            }

//            if (normals.size == 0) {
                val normal = findNormal(polygon[0], polygon[1], polygon[2])
                result[i] = angleBetweenVectors(normal, viewVector) >= 0
//            } else
//                result[i] = angleBetweenVectors(normals[i], viewVector) >= PI / 2
        }

        return result
    }

    private fun angleBetweenVectors(v1: DirectionVector, v2: DirectionVector): Double {
        return ((v1.l * v2.l + v1.m * v2.m + v1.n * v2.n)
                / (sqrt(v1.l * v1.l + v1.m * v1.m + v1.n * v1.n)
                * sqrt(v2.l * v2.l + v2.m * v2.m + v2.n * v2.n)))
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

    override fun equals(other: Any?): Boolean {
        if (other !is Line)
            return false
        return point1.x == other.point1.x && point1.y == other.point1.y && point1.z == other.point1.z
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
