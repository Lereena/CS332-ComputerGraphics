package lab6

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
    val cP = polyhedron.centerPoint
    val move = translationMatrix(-cP.x, -cP.y, -cP.z)
    val scale = scaleMatrix(kX, kY, kZ)
    val moveBack = translationMatrix(cP.x, cP.y, cP.z)

    val transformationMatrix = multiplyMatrices(moveBack, multiplyMatrices(scale, move))
    transform(polyhedron, transformationMatrix)
}

fun reflect(polyhedron: Polyhedron, axis: Axis) {
    val transformationMatrix = identityMatrix()
    when (axis) {
        Axis.Z -> transformationMatrix[2][2] = -1.0
        Axis.Y -> transformationMatrix[1][1] = -1.0
        Axis.X -> transformationMatrix[0][0] = -1.0
    }

    transform(polyhedron, transformationMatrix)
}

fun identityMatrix(): Matrix {
    return arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 1.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0)
    )
}

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

//fun axonometricMatrix(fi: Double, psi: Double): Matrix {
//    return arrayOf(
//        doubleArrayOf(cos(psi), 0.0, sin(psi), 0.0),
//        doubleArrayOf(sin(fi) * sin(psi), cos(fi), -sin(fi) * cos(psi), 0.0),
//        doubleArrayOf(0.0, 0.0, 0.0, 0.0),
//        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
//    )
//}

fun transform(polyhedron: Polyhedron, matrix: Matrix) {
    val newCenterPoint = multiplyMatrices(
        matrix,
        pointToMatrix(polyhedron.centerPoint)
    )
    polyhedron.centerPoint.x = newCenterPoint[0][0]
    polyhedron.centerPoint.y = newCenterPoint[1][0]
    polyhedron.centerPoint.z = newCenterPoint[2][0]
    for (i in polyhedron.vertices.indices) {
        val point = polyhedron.vertices[i]
        val transformed = multiplyMatrices(matrix, pointToMatrix(point))
        polyhedron.vertices[i].x = transformed[0][0] / transformed[3][0]
        polyhedron.vertices[i].y = transformed[1][0] / transformed[3][0]
        polyhedron.vertices[i].z = transformed[2][0] / transformed[3][0]
    }
}

fun multiplePointAndMatrix(point: Point3D, matrix: Matrix): Point3D {
    val transformed = multiplyMatrices(matrix, pointToMatrix(point))
    val k = transformed[3][0]
    return Point3D(transformed[0][0] / k, transformed[1][0] / k, transformed[2][0] / k)
}

fun pointToMatrix(point: Point3D): Matrix {
    return arrayOf(
        doubleArrayOf(point.x),
        doubleArrayOf(point.y),
        doubleArrayOf(point.z),
        doubleArrayOf(1.0)
    )
}

fun removeNonFace(polyhedron: Polyhedron, viewVector: DirectionVector): Array<Line> {
    val visiblePolygons = polyhedron.faces(viewVector)
    val visibleEdges = HashSet<Line>()

    for (i in polyhedron.polygons.indices)
        if (visiblePolygons[i]) {
            val polygon = polyhedron.polygons[i]
            for (j in polygon.points.indices)
                visibleEdges.add(Line(polygon[j], polygon[j + 1]))
        }

    return visibleEdges.toTypedArray()
}
