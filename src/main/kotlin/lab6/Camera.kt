package lab6

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import lab3.getLine
import lab5.Point
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.PI
import kotlin.math.abs
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

        val flag = plot.polygons.first().points.first().z >
                plot.polygons.last().points.first().z
        val planes = if (flag) plot.polygons else plot.polygons.reversed()
        for (plane in planes) {
            var prevPoint = plane.points.first()
            for (point in plane.points.drop(1)) {
                val rasterPoints = getLine(prevPoint, point)
                for (pixel in rasterPoints) {
                    if (pixel.x >= canvas.width || pixel.x < 0 ||
                            pixel.y >= canvas.height || pixel.y < 0)
                        continue
                    if (pixel.y >= maxHorizon[pixel.x]) {
                        maxHorizon[pixel.x] = pixel.y
                        writer.setColor(pixel.x, pixel.y, Color.BLACK)
                    }
                    if (pixel.y <= minHorizon[pixel.x]) {
                        minHorizon[pixel.x] = pixel.y
                        writer.setColor(pixel.x, pixel.y, Color.BLACK)
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

fun getLine(start: Point3D, end: Point3D): LinkedList<Pixel> {
    val result = LinkedList<Pixel>()
    val pStart = Pixel(start)
    val pEnd = Pixel(end)
    result.add(pStart)

    val a = pEnd.y - pStart.y
    val b = pStart.x - pEnd.x
    if (a == 0 && b == 0)
        return result
    val sign = if (abs(a) > abs(b)) 1 else -1
    val signA = if (a > 0) 1 else -1
    val signB = if (b > 0) 1 else -1

    var f = 0
    var x = pStart.x
    var y = pStart.y

    if (sign == -1)
        do {
            f += a * signA
            if (f > 0) {
                f -= b * signB
                y += signA
            }
            x -= signB
            result.add(Pixel(x, y))
        } while (x != pEnd.x || y != pEnd.y)
    else
        do {
            f += b * signB
            if (f > 0) {
                f -= a * signA
                x -= signB
            }
            y += signA
            result.add(Pixel(x, y))
        } while (x != pEnd.x || y != pEnd.y)

    return result
}