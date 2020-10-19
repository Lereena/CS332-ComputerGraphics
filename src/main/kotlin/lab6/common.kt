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
