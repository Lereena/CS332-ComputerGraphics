package lab6

import java.security.InvalidParameterException
import kotlin.math.cos
import kotlin.math.sin

fun move(polyhedron: Polyhedron, dx: Double, dy: Double, dz: Double) {
    transform(polyhedron, translationMatrix(dx, dy, dz))
}

fun rotateAroundCenter(polyhedron: Polyhedron, axis: Axis, angle: Double) {
    val rotation = when (axis) {
        Axis.X -> rotationXMatrix(angle)
        Axis.Y -> rotationYMatrix(angle)
        Axis.Z -> rotationZMatrix(angle)
    }

    transform(polyhedron, rotation)
}

fun rotateAroundLine(polyhedron: Polyhedron, line: Line, angle: Double) {
    val dirVect = line.directionVector
    val point = line.point1

    val move = translationMatrix(-point.x, -point.y, -point.z)
    val rotate = generalRotationMatrix(angle, dirVect)
    val moveBack = translationMatrix(point.x, point.y, point.z)

    val transformationMatrix = multiplyMatrices(moveBack, multiplyMatrices(rotate, move))
    transform(polyhedron, transformationMatrix)
}

fun scale(polyhedron: Polyhedron, kX: Double, kY: Double, kZ: Double) {
    transform(polyhedron, scaleMatrix(kX, kY, kZ))
}

fun reflect(polyhedron: Polyhedron, axis1: Axis, axis2: Axis) {
    val transformationMatrix = identityMatrix
    when (axis1) {
        Axis.X -> when (axis2) {
            Axis.Y -> transformationMatrix[2][2] = -1.0
            Axis.Z -> transformationMatrix[1][1] = -1.0
            Axis.X -> throw InvalidParameterException()
        }
        Axis.Y -> when (axis2) {
            Axis.X -> transformationMatrix[2][2] = -1.0
            Axis.Z -> transformationMatrix[0][0] = -1.0
            Axis.Y -> throw InvalidParameterException()
        }
        Axis.Z -> when (axis2) {
            Axis.X -> transformationMatrix[1][1] = -1.0
            Axis.Y -> transformationMatrix[0][0] = -1.0
            Axis.Z -> throw InvalidParameterException()
        }
    }

    transform(polyhedron, transformationMatrix)
}

val identityMatrix = arrayOf(
    doubleArrayOf(1.0, 0.0, 0.0, 0.0),
    doubleArrayOf(0.0, 1.0, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 1.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0, 1.0),
)

fun translationMatrix(tX: Double, tY: Double, tZ: Double): Matrix {
    return arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0, tX),
        doubleArrayOf(0.0, 1.0, 0.0, tY),
        doubleArrayOf(0.0, 0.0, 1.0, tZ),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun scaleMatrix(mX: Double, mY: Double, mZ: Double): Matrix {
    return arrayOf(
        doubleArrayOf(mX, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, mY, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, mZ, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun generalRotationMatrix(angle: Double, vector: DirectionVector): Matrix {
    val angleCos = cos(angle)
    val angleSin = sin(angle)
    val l = vector.l
    val m = vector.m
    val n = vector.n
    return arrayOf(
        doubleArrayOf(
            l * l + angleCos * (1 - l * l),
            l * (1 - angleCos) * m + n * angleSin,
            l * (1 - angleCos) * n - m * angleSin,
            0.0
        ),
        doubleArrayOf(
            l * (1 - angleCos) * m - n * angleSin,
            m * m + angleCos * (1 - m * m),
            m * (1 - angleCos) * n + l * angleSin,
            0.0
        ),
        doubleArrayOf(
            l * (1 - angleCos) * n + m * angleSin,
            m * (1 - angleCos) * n - l * angleSin,
            n * n + angleCos * (1 - n * n),
            0.0
        ),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun rotationXMatrix(angle: Double): Matrix {
    return arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, cos(angle), -sin(angle), 0.0),
        doubleArrayOf(0.0, sin(angle), cos(angle), 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun rotationYMatrix(angle: Double): Matrix {
    return arrayOf(
        doubleArrayOf(cos(angle), 0.0, sin(angle), 0.0),
        doubleArrayOf(0.0, 1.0, 0.0, 0.0),
        doubleArrayOf(-sin(angle), 0.0, cos(angle), 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun rotationZMatrix(angle: Double): Matrix {
    return arrayOf(
        doubleArrayOf(cos(angle), -sin(angle), 0.0, 0.0),
        doubleArrayOf(sin(angle), cos(angle), 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
    )
}

fun transform(polyhedron: Polyhedron, matrix: Matrix) {
    for (i in polyhedron.vertices.indices) {
        val point = polyhedron.vertices[i]
        val transformed = multiplyMatrices(matrix, pointToMatrix(point))
        polyhedron.vertices[i].x = transformed[0][0]
        polyhedron.vertices[i].y = transformed[1][0]
        polyhedron.vertices[i].z = transformed[2][0]
    }
}

fun pointToMatrix(point: Point3D): Matrix {
    return arrayOf(
        doubleArrayOf(point.x),
        doubleArrayOf(point.y),
        doubleArrayOf(point.z),
        doubleArrayOf(1.0)
    )
}