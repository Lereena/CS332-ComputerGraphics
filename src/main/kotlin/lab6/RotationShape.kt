package lab6

import kotlin.math.PI

fun rotationShape(points: ArrayList<Point3D>, iterations: Int, axis: Axis): Polyhedron {
    return Polyhedron("")
//    val polygonsCount = points.size - 1
//    val angle = 2 * PI / iterations
//    val vertices = ArrayList<Point3D>(points.size * iterations)
//    val polygons = ArrayList<Polygon>(polygonsCount * iterations)
//    val matrix = when (axis) {
//        Axis.X -> rotationXMatrix(angle)
//        Axis.Y -> rotationYMatrix(angle)
//        Axis.Z -> rotationZMatrix(angle)
//    }
//
//    vertices.addAll(points)
//    var prevPoints = points.toList()
//    for (i in (0 until iterations)) {
//        val newPoints = prevPoints.map { point -> multiplePointAndMatrix(point, matrix) }
//        for (i in (0 until prevPoints.size - 1)) {
//            val newPolygon = Polygon(arrayListOf(
//                    prevPoints[i], newPoints[i],
//                    newPoints[i + 1], prevPoints[i + 1],
//            ))
//            polygons.add(newPolygon)
//        }
//        vertices.addAll(newPoints)
//        prevPoints = newPoints
//    }
//    for (i in (0 until prevPoints.size - 1)) {
//        val newPolygon = Polygon(arrayListOf(
//                prevPoints[i], points[i],
//                points[i + 1], prevPoints[i + 1],
//        ))
//        polygons.add(newPolygon)
//    }
//
//    return Polyhedron(vertices, polygons)
}

//fun pointsToPolyhedron(points: ArrayList<Point3D>): Polyhedron {
//    if (points.size == 1) {
//        val polygon = Polygon(arrayListOf(points[0]))
//        return Polyhedron(arrayListOf(points[0]), arrayListOf(polygon))
//    }
//    val polygons = ArrayList<Polygon>(points.size - 1)
//    var prevPoint = points[0]
//    for (i in (1 until points.size)) {
//        val polygon = Polygon(arrayListOf(prevPoint, points[i]))
//        polygons.add(polygon)
//        prevPoint = points[i]
//    }
//    return Polyhedron(points, polygons)
//}