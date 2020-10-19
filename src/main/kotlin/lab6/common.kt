package lab6

import java.security.InvalidParameterException

data class Point3D(var x: Double, var y: Double, var z: Double)

enum class Axis { X, Y, Z }

data class DirectionVector(val l: Double, val m: Double, val n: Double)

data class Line(var point: Point3D, var vector: DirectionVector) {

    constructor(point1: Point3D, point2: Point3D) {
        if (point1 == point1)
            throw InvalidParameterException("Прямая не может быть задана совпадающими точками")
        point = point1
        vector = DirectionVector(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z)
    }
}
