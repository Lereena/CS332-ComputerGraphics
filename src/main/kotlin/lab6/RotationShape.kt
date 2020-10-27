package lab6

import kotlin.math.PI

fun rotationShape(points: ArrayList<Point3D>, iterations: Int, axis: Axis): Polyhedron {
    val polygonsCount = points.size - 1
    val angle = 2 * PI / iterations
    val vertices = ArrayList<Point3D>(points.size * iterations)
    val polygons = ArrayList<Polygon>(polygonsCount * iterations)
    val matrix = when (axis) {
        Axis.X -> rotationXMatrix(angle)
        Axis.Y -> rotationYMatrix(angle)
        Axis.Z -> rotationZMatrix(angle)
    }

    var prevPoints = points.toList()
    for (i in (0 until iterations)) {
        val newPoints = prevPoints.map { point -> multiplePointAndMatrix(point, matrix) }
        for (i in (0 until prevPoints.size - 1)) {
            val newPolygon = Polygon(arrayListOf<Point3D>(
                    prevPoints[i], newPoints[i],
                    newPoints[i + 1], prevPoints[i + 1],
            ))
            polygons.add(newPolygon)
        }
        vertices.addAll(newPoints)
        prevPoints = newPoints
    }
    for (i in (0 until prevPoints.size - 1)) {
        val newPolygon = Polygon(arrayListOf<Point3D>(
                prevPoints[i], points[i],
                points[i + 1], prevPoints[i + 1],
        ))
        polygons.add(newPolygon)
    }

    return Polyhedron(vertices, polygons)
}