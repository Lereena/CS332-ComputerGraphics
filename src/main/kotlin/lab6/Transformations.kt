package lab6

import java.util.*
import kotlin.math.cos
import kotlin.math.sin

fun move(polyhedron: LinkedList<Point3D>, dx: Int, dy: Int, dz: Int): LinkedList<Point3D> {
    TODO("Not yet implemented")
}

fun rotateAroundCenter(polyhedron: LinkedList<Point3D>, axis: Axis, angle: Double): LinkedList<Point3D> {
    TODO("Not yet implemented")
}

fun rotateAroundLine(polyhedron: LinkedList<Point3D>, line: Line, angle: Double): LinkedList<Point3D> {
    TODO("Not yet implemented")
}

fun scale(polyhedron: LinkedList<Point3D>, kX: Double, kY: Double, kZ: Double): LinkedList<Point3D> {
    TODO("Not yet implemented")
}

fun reflect(polyhedron: LinkedList<Point3D>, axis1: Axis, axis2: Axis): LinkedList<Point3D> {
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

fun scaleMatrix(mX: Double, mY: Double, mZ: Double): Array<Array<Double>> {
    return arrayOf(
        arrayOf(mX, 0.0, 0.0, 0.0),
        arrayOf(0.0, mY, 0.0, 0.0),
        arrayOf(0.0, 0.0, mZ, 0.0),
        arrayOf(0.0, 0.0, 0.0, 1.0),
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

