package lab6

import java.security.InvalidParameterException

data class Point3D(var x: Double, var y: Double, var z: Double)

enum class Axis { X, Y, Z }

data class UnitVector(val l: Double, val m: Double, val n: Double)

data class Line(var point: Point3D, var vector: UnitVector) {

    constructor(point1: Point3D, point2: Point3D) {
        if (point1 == point1)
            throw InvalidParameterException("Прямая не может быть задана совпадающими точками")
        point = point1
        TODO("Not yet implemented")
    }
}
