package lab6

import kotlin.math.cos
import kotlin.math.sin

fun move(polyhedron: Polyhedron, dx: Double, dy: Double, dz: Double): Polyhedron {
    TODO("Not yet implemented")
}

fun rotateAroundCenter(polyhedron: Polyhedron, axis: Axis, angle: Double): Polyhedron {
    TODO("Not yet implemented")
}

fun rotateAroundLine(polyhedron: Polyhedron, line: Line, angle: Double): Polyhedron {
    TODO("Not yet implemented")
}

fun scale(polyhedron: Polyhedron, kX: Double, kY: Double, kZ: Double): Polyhedron {
    TODO("Not yet implemented")
}

fun reflect(polyhedron: Polyhedron, axis1: Axis, axis2: Axis): Polyhedron {
    TODO("Not yet implemented")
}

val identityMatrix = arrayOf(
    doubleArrayOf(1.0, 0.0, 0.0, 0.0),
    doubleArrayOf(0.0, 1.0, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 1.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0, 1.0),
)

fun translationMatrix(tX: Double, tY: Double, tZ: Double): Array<DoubleArray> {
    return arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0, tX),
        doubleArrayOf(0.0, 1.0, 0.0, tY),
        doubleArrayOf(0.0, 0.0, 1.0, tZ),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun scaleMatrix(mX: Double, mY: Double, mZ: Double): Array<DoubleArray> {
    return arrayOf(
        doubleArrayOf(mX, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, mY, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, mZ, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun rotationXMatrix(angle: Double): Array<DoubleArray> {
    return arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, cos(angle), -sin(angle), 0.0),
        doubleArrayOf(0.0, sin(angle), cos(angle), 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun rotationYMatrix(angle: Double): Array<DoubleArray> {
    return arrayOf(
        doubleArrayOf(cos(angle), 0.0, sin(angle), 0.0),
        doubleArrayOf(0.0, 1.0, 0.0, 0.0),
        doubleArrayOf(-sin(angle), 0.0, cos(angle), 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun rotationZMatrix(angle: Double): Array<DoubleArray> {
    return arrayOf(
        doubleArrayOf(cos(angle), -sin(angle), 0.0, 0.0),
        doubleArrayOf(sin(angle), cos(angle), 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

