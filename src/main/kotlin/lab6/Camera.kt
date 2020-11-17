package lab6

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import lab5.Point
import kotlin.math.PI
import kotlin.math.tan

class Camera(var position: Point3D, var angleX: Double, var angleY: Double, val canvas: Canvas) {
    val mainGc = canvas.graphicsContext2D
    val height = canvas.height
    val width = canvas.width
    var projectionMode = Projection.PERSPECTIVE
    var zBufferMode = false
    var shaderMode = false
    var viewVector = DirectionVector(0.0, 0.0, 1.0)
    var cosX = Math.cos(angleX)
    var cosY = Math.cos(angleY)
    var sinX = Math.sin(angleX)
    var sinY = Math.sin(angleY)

    init {
        updateViewVector()
    }

    fun draw(model: Polyhedron) {
        val projectionMatrix = multiplyMatrices(
                viewPortMatrix(),
                multiplyMatrices(
                        when (projectionMode) {
                            Projection.PERSPECTIVE -> perspectiveProjectionMatrix()
                            Projection.ORTHOGRAPHIC -> orthographicProjectionMatrix()
                        },
                        viewMatrix()
                )
        )
        val clone = model.clone()
        transform(clone, projectionMatrix)
        var polygons = clone.faces(viewVector)

        if (zBufferMode) {
            zBuffer(canvas, mainGc, polygons)
        }
        else {
            for (polygon in polygons)
                for (edge in polygon.edges) {
                    val a = edge.point1
                    val b = edge.point2
                    mainGc.strokeLine(
                            a.x, a.y,
                            b.x, b.y
                    )
                }
        }

        polygons = clone.faces(DirectionVector(0.0, 0.0, -1.0))
        if (shaderMode) {
            shader(canvas, mainGc, polygons, clone, DirectionVector(0.0, 0.0, -1.0))
        }
        else {
            for (polygon in polygons)
                for (edge in polygon.edges) {
                    val a = edge.point1
                    val b = edge.point2
                    mainGc.strokeLine(
                            a.x, a.y,
                            b.x, b.y
                    )
                }
        }
    }

    fun changeAngleX(difAngleX: Double) {
        angleX += difAngleX
        cosX = Math.cos(angleX)
        sinX = Math.sin(angleX)
    }

    fun changeAngleY(difAngleY: Double) {
        angleY += difAngleY
        cosY = Math.cos(angleY)
        sinY = Math.sin(angleY)
    }

    fun changePosition(difX: Double, difY: Double) {
        position.x += difX
        position.y += difY
    }

    private fun updateViewVector() {
        val v1 = DirectionVector(cosX, 0.0, sinX)
        val v2 = DirectionVector(0.0, cosY, sinY)
        viewVector = v1 + v2
    }

    private fun perspectiveProjectionMatrix(): Matrix {
        return arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 0.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0, 0.0),
                doubleArrayOf(0.0, 0.0, -1.0 / 300.0, 1.0)
        )
    }

    private fun orthographicProjectionMatrix(): Matrix {
        return arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0, 0.0),
                doubleArrayOf(0.0, 1.0, 0.0, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0, 0.0),
                doubleArrayOf(0.0, 0.0, 0.0, 1.0)
        )
    }

    private fun viewPortMatrix(): Matrix {
        return arrayOf(
                doubleArrayOf(1.0,  0.0, 0.0, width/2),
                doubleArrayOf(0.0, -1.0, 0.0, height/2),
                doubleArrayOf(0.0,  0.0, 1.0, 0.0),
                doubleArrayOf(0.0,  0.0, 0.0, 1.0)
        )
    }

    private fun viewMatrix(): Matrix {
        val rotateY = rotationYMatrix(angleY - Math.PI / 2)
        val rotateX = rotationXMatrix(angleX - Math.PI / 2)
        val temp = multiplyMatrices(rotateY, rotateX)
        val temp2 = multiplyMatrices(
                translationMatrix(-position.x, -position.y, -position.z),
                temp
        )
        return temp2
//        return translationMatrix(-position.x, -position.y, -position.z)
    }
}