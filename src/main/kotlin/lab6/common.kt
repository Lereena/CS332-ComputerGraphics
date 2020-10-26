package lab6

import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class Point3D(var x: Double, var y: Double, var z: Double)

enum class Axis { X, Y, Z }

data class DirectionVector(val l: Double, val m: Double, val n: Double)

class Polygon {
    val points = ArrayList<Point3D>()

    operator fun get(i: Int) = points[(i % points.size)]

    fun add(point: Point3D) = points.add(point)
}

class Polyhedron {
    var vertices = ArrayList<Point3D>()
    var polygons = ArrayList<Polygon>()
    var edges = ArrayList<Line>()

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

    constructor(vertices: ArrayList<Point3D>, polygons: ArrayList<Polygon>) {
        this.vertices = vertices
        this.polygons = polygons
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

fun multiplyMatrices(matrix1: Array<DoubleArray>, matrix2: Array<DoubleArray>): Array<DoubleArray> {
    val result = Array(matrix1.size) { DoubleArray(matrix2[0].size) { 0.0 } }
    for (i in matrix1.indices)
        for (j in matrix2[0].indices)
            for (k in matrix2.indices)
                result[i][j] += matrix1[i][k] * matrix2[k][j]

    return result
}

val functions = arrayListOf<(Double, Double) -> Double>(
    { x, y -> sin(x + y) },
    { x, y -> x.pow(2) + y.pow(2) },
    { x, y -> sin(x + y) / (x + y)}
)

fun plot3D(x0: Double, y0: Double, x1: Double, y1: Double, step: Double, f: (Double, Double) -> Double): Polyhedron {
    val plot = Polyhedron(ArrayList(), ArrayList())
    val builtPoints = ArrayList<Point3D>()
    val pointNums = HashMap<Point3D, Int>()
    val relationships = HashMap<Int, ArrayList<Int>>()

    var i = 0
    var curEst = 0
    var x = x0
    while (x <= x1 - step) {
        var y = y0
        while (y <= y1 - step) {
            val currentPoints = arrayListOf(
                Point3D(x, y, f(x, y)),
                Point3D(x + step, y, f(x + step, y)),
                Point3D(x, y + step, f(x, y + step)),
                Point3D(x + step, y + step, f(x + step, y + step))
            )
            curEst = processPoints(currentPoints, plot, relationships, builtPoints, pointNums, curEst, i)
            i++
            y += step
        }
        x += step
    }
    if (x0 == x1) {
        var y = y0
        while (y <= y1 - step) {
            val currentPoints = ArrayList<Point3D>()
            currentPoints.add(Point3D(x0, y, f(x0, y)))
            currentPoints.add(Point3D(x0, y + step, f(x0, y + step)))
            curEst = processPoints(currentPoints, plot, relationships, builtPoints, pointNums, curEst, i)
            i++
            y += step
        }
    }
    if (y0 == y1) {
        x = x0
        while (x <= x1 - step) {
            val currentPoints = ArrayList<Point3D>()
            currentPoints.add(Point3D(x, y0, f(x, y0)))
            currentPoints.add(Point3D(x0 + step, y0, f(x0 + step, y0)))
            curEst = processPoints(currentPoints, plot, relationships, builtPoints, pointNums, curEst, i)
            i++
            x += step
        }
    }

    return plot
}

fun processPoints(
    currentPoints: ArrayList<Point3D>, plot: Polyhedron, relationships: HashMap<Int, ArrayList<Int>>,
    builtPoints: ArrayList<Point3D>, pointNums: HashMap<Point3D, Int>, curEst: Int, i: Int
): Int {
    relationships[i] = ArrayList()
    val polygon = Polygon()
    var currentEst = curEst
    for (point in currentPoints) {
        if (!pointNums.containsKey(point)) {
            builtPoints.add(point)
            plot.vertices.add(point)
            pointNums[point] = currentEst
            currentEst++
        } else {
            polygon.add(builtPoints[pointNums[point]!!])
        }
        relationships[i]!!.add(pointNums[point]!!)
    }
    plot.polygons.add(polygon)
    return currentEst
}
