package lab6

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import lab5.Point
import kotlin.math.PI
import kotlin.math.tan

enum class RasterModes { BY_EDGES, Z_BUFFER, SHADER, FLOAT_HOR }

class Camera(var position: Point3D, var angleX: Double, var angleY: Double, val canvas: Canvas) {
    val mainGc = canvas.graphicsContext2D
    val height = canvas.height
    val width = canvas.width
    var projectionMode = Projection.PERSPECTIVE
    var removeFaces = true
    var rasterMode = RasterModes.BY_EDGES
    var viewVector = DirectionVector(0.0, 0.0, 1.0)

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
        val clone = model.copy()
        transform(clone, projectionMatrix)
//        var polygons = clone.faces(viewVector)
        var polygons = clone.polygons

        when (rasterMode) {
            RasterModes.BY_EDGES -> drawByEdges(polygons)
            RasterModes.Z_BUFFER -> zBuffer(canvas, mainGc, polygons)
            RasterModes.SHADER -> shader(canvas, mainGc, polygons, DirectionVector(0.0, -1.0, 0.0))
            RasterModes.FLOAT_HOR -> drawFloatingHorizon(clone)
        }
    }

    private fun drawFloatingHorizon(plot: Polyhedron) {
        val minHorizon = Array<Int>(canvas.width.toInt()) { Int.MAX_VALUE }
        val maxHorizon = Array<Int>(canvas.width.toInt()) { Int.MIN_VALUE }

        val image = WritableImage(canvas.width.toInt(), canvas.height.toInt())
        val writer = image.pixelWriter

        val planes = if (true) plot.polygons else plot.polygons.reversed()
        for (plane in planes) {
            var prevPoint = plane.points.first()
            for (point in plane.points.drop(1)) {
                val dX = Math.abs(point.x.toInt() - prevPoint.x.toInt())
                val dY = Math.abs(point.y.toInt() - prevPoint.y.toInt())
                var error = 0
                val dError = dY + 1
                var y = prevPoint.y.toInt()
                val dirY = if (point.y > prevPoint.y) 1 else -1
                for (x in (prevPoint.x.toInt()..point.x.toInt())) {
                    if (x >= canvas.width || x < 0 ||
                            y >= canvas.height || y < 0)
                        continue
                    if (y > maxHorizon[x]) {
                        maxHorizon[x] = y
                        writer.setColor(x, y, Color.BLACK)
                    }
                    if (y < minHorizon[x]) {
                        maxHorizon[x] = y
                        writer.setColor(x, y, Color.BLACK)
                    }
                    error += dError
                    if (error >= dX + 1) {
                        y += dY
                        error -= dX + 1
                    }
                }
                prevPoint = point
            }
        }
        mainGc.drawImage(image, 0.0, 0.0)
    }

    private fun drawByEdges(polygons: ArrayList<Polygon>) {
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

    fun changeAngleX(difAngleX: Double) {
        angleX += difAngleX
    }

    fun changeAngleY(difAngleY: Double) {
        angleY += difAngleY
    }

    fun changePosition(difX: Double, difY: Double) {
        position.x += difX
        position.y += difY
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
        return multiplyMatrices(
                translationMatrix(-position.x, -position.y, -position.z),
                temp
        )
    }
}