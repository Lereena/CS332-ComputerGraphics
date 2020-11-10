package lab6

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import lab5.Point
import kotlin.math.PI
import kotlin.math.tan

class Camera(var position: Point3D, var viewVector: DirectionVector, val canvas: Canvas, val fovAngle: Double) {
    val mainGc = canvas.graphicsContext2D
    val height = canvas.height
    val width = canvas.width
    val aspectRatio = width / height
    val projectionMode = Projection.PERSPECTIVE
    var fovX = 1.0 / tan(fovAngle / 2)
    var fovY = 1.0 / tan(fovAngle / (2 * aspectRatio))
    var far = 5000.0
    var near = 1.0

    fun draw(model: Polyhedron) {
        val copy = model.copy()
//        var projectionMatrix = axonometricMatrix(145.0 * PI / 180, 45.0 * PI / 180)
//        if (projectionMode == Projection.PERSPECTIVE)
//            projectionMatrix = multiplyMatrices(projectionMatrix, perspectiveMatrix(position))
        val projectionMatrix = multiplyMatrices(perspectiveProjectionMatrix(), viewMatrix())
        transform(copy, projectionMatrix)
        val edges = removeNonFace(copy, viewVector)
                for (edge in edges) {
            val a = viewPortTransform(edge.point1)
            val b = viewPortTransform(edge.point2)
            mainGc.strokeLine(
                    a.x, a.y,
                    b.x, b.y
            )
        }
//        for (polygon in model.polygons) {
//            val projPoints = polygon.points.map { point ->
//                multiplePointAndMatrix(point, projectionMatrix)
//            }
//            var prevPoint = viewPortTransform(projPoints.last())
//            for (point in projPoints) {
//                val newPoint = viewPortTransform(point)
//                mainGc.moveTo(newPoint.x, newPoint.y)
//                mainGc.lineTo(prevPoint.x, prevPoint.y)
//                prevPoint = newPoint
//            }
//        }
//        mainGc.stroke()
    }

    private fun perspectiveProjectionMatrix(): Matrix {
        return arrayOf(
                doubleArrayOf(fovX, 0.0, 0.0, 0.0),
                doubleArrayOf(0.0, fovY, 0.0, 0.0),
                doubleArrayOf(0.0, 0.0, (far+near)/(far-near), -1.0),
                doubleArrayOf(0.0, 0.0, (2*near*far)/(near-far), 0.0)
        )
    }

    private fun viewPortTransform(point: Point3D): Point {
        return Point(
                (1.0 + point.x) * width / 2,
                (1.0 - point.y) * height / 2,
        )
    }

    private fun viewMatrix(): Matrix {
        return translationMatrix(position.x, position.y, position.z)
    }

//    private fun axonometricProjection(model: Polyhedron) {
//        for (polygon in model.polygons) {
//            val projPoints = polygon.points.map { point ->
//                multiplePointAndMatrix(point, axonometricMatrix(145.0 * PI / 180, 45.0 * PI / 180))
//            }
//            var prevPoint = projPoints.last()
//            for (point in projPoints) {
//                gc.moveTo(point.x + canvas.width / 2, -point.y + canvas.height / 2)
//                gc.lineTo(prevPoint.x + canvas.width / 2, -prevPoint.y + canvas.height / 2)
//                prevPoint = point
//            }
//        }
//        gc.stroke()
//    }
}