package lab6

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import kotlin.math.ceil
import kotlin.math.floor

fun zBuffer(canvas: Canvas, gc: GraphicsContext, polygons: ArrayList<Polygon>) {
    val cWidth = canvas.width.toInt()
    val cHeight = canvas.height.toInt()
    val zBuff = Array(cWidth) {
        Array(cHeight) { Double.MAX_VALUE }
    }

    var minDepth = Double.MAX_VALUE
    for (polygon in polygons) {
        var leftBound = cWidth
        var rightBound = 0
        var upperBound = 0
        var lowerBound = cHeight

        for (point in polygon.vertices) {
            if (point.x < leftBound && point.x >= 0)
                leftBound = ceil(point.x).toInt()
            if (point.x > rightBound && point.x < cWidth)
                rightBound = floor(point.x).toInt()
            if (point.y < lowerBound && point.y >= 0)
                lowerBound = ceil(point.y).toInt()
            if (point.y > upperBound && point.y < cHeight)
                upperBound = floor(point.y).toInt()
        }

        val normal = findNormal(
            polygon.vertices[0].point,
            polygon.vertices[1].point,
            polygon.vertices[2].point
        )
        val A = normal.l
        val B = normal.m
        val C = normal.n
        // высчитываем свободный член в уравнении плоскости
        val F = -(polygon.vertices[0].x * A) - (polygon.vertices[0].y * B) - (polygon.vertices[0].z * C)

        for (x in (leftBound..rightBound)) {
            for (y in (lowerBound..upperBound)) {
                val point = Point3D(x.toDouble(), y.toDouble(), 0.0)

                if (checkIsInPolygon(point, polygon)) {
                    val depth = findDepth(x, y, A, B, C, F)
                    if (depth < zBuff[point.x.toInt()][point.y.toInt()]) {
                        zBuff[point.x.toInt()][point.y.toInt()] = depth
                        if (depth < minDepth)
                            minDepth = depth
                    }
                }
            }
        }
    }

    var maxDepth = java.lang.Double.MIN_VALUE
    for (x in zBuff.indices) {
        for (y in zBuff[x].indices) {
            if (zBuff[x][y] > maxDepth && zBuff[x][y] < java.lang.Double.MAX_VALUE)
                maxDepth = zBuff[x][y]
        }
    }

    val image = WritableImage(zBuff.size, zBuff[0].size)
    val writer = image.pixelWriter
    for (x in zBuff.indices) {
        for (y in zBuff[x].indices) {
            if (zBuff[x][y] < Double.MAX_VALUE) {
                val value = 1 - (zBuff[x][y] - minDepth) / (maxDepth - minDepth)
                writer.setColor(x, y, Color(value, value, value, 1.0))
            } else writer.setColor(x, y, Color(1.0, 1.0, 1.0, 1.0))
        }
    }
    gc.drawImage(image, 0.0, 0.0)
}

fun shader(
    canvas: Canvas, gc: GraphicsContext, polygons: ArrayList<Polygon>, model: Polyhedron,
    dv: DirectionVector, color: Color = Color.ORANGE, Li: Double = 1.0, kd: Double = 0.8
) {
    val cWidth = canvas.width.toInt()
    val cHeight = canvas.height.toInt()
    val zBuff = Array(cWidth) {
        Array(cHeight) { 2.0 }
    }
    val backgroundLightning = 0.2

    polygons.sortBy { polygon -> zOfPolygon(polygon) }

    for (polygon in polygons) {
        var leftBound = Point3D(canvas.width, 0.0, 0.0)
        var rightBound = Point3D(0.0, 0.0, 0.0)
        var upperBound = Point3D(0.0, 0.0, 0.0)
        var lowerBound = Point3D(0.0, canvas.height, 0.0)

        for (point in polygon.vertices) {
            if (point.x < leftBound.x && point.x >= 0)
                leftBound = point.point
            if (point.x > rightBound.x && point.x < cWidth)
                rightBound = point.point
            if (point.y < lowerBound.y && point.y >= 0)
                lowerBound = point.point
            if (point.y > upperBound.y && point.y < cHeight)
                upperBound = point.point
        }

        val leftBoundNormals = ArrayList<DirectionVector>()
        val rightBoundNormals = ArrayList<DirectionVector>()
        val upperBoundNormals = ArrayList<DirectionVector>()
        val lowerBoundNormals = ArrayList<DirectionVector>()
        for (p in model.polygons) {
            val normal = findNormal(p.vertices[0].point, p.vertices[1].point, p.vertices[2].point)
            for (point in p.vertices) {
                if (point.point == leftBound)
                    leftBoundNormals.add(normal)
                if (point.point == rightBound)
                    rightBoundNormals.add(normal)
                if (point.point == upperBound)
                    upperBoundNormals.add(normal)
                if (point.point == lowerBound)
                    lowerBoundNormals.add(normal)
            }
        }

        val leftLightning = getLightning(leftBoundNormals, dv, Li, kd, backgroundLightning)
        val rightLightning = getLightning(leftBoundNormals, dv, Li, kd, backgroundLightning)
        val upperLightning = getLightning(leftBoundNormals, dv, Li, kd, backgroundLightning)
        val lowerLightning = getLightning(leftBoundNormals, dv, Li, kd, backgroundLightning)

        val deltaHorizontalLightning = rightLightning - leftLightning
        val deltaWidth = rightBound.x - leftBound.x
        val deltaHorizontal = deltaHorizontalLightning / deltaWidth
        for (x in leftBound.x.toInt()..rightBound.x.toInt()) {
            val horizontalLightning = leftLightning +
                    deltaHorizontal * (x - leftBound.x)
            val deltaVerticalLightning = upperLightning - lowerLightning
            val deltaHeight = upperBound.y - lowerBound.y
            val deltaVertical = deltaVerticalLightning / deltaHeight
            for (y in (lowerBound.y.toInt()..upperBound.y.toInt())) {
                val verticalLightning = lowerLightning + deltaVertical * (y - lowerBound.y)
                val point = Point3D(x.toDouble(), y.toDouble(), 0.0)
                if (checkIsInPolygon(point, polygon)) {
                    var L0 = (horizontalLightning + verticalLightning) / 2
                    if (L0 > 1) L0 = 1.0
                    zBuff[point.x.toInt()][point.y.toInt()] = L0
                }
            }
        }
    }

    val image = WritableImage(zBuff.size, zBuff[0].size)
    val writer = image.pixelWriter
    for (x in zBuff.indices) {
        for (y in zBuff[x].indices) {
            if (zBuff[x][y] <= 1) {
                val value = zBuff[x][y]
                writer.setColor(x, y, Color(color.red * value, color.green * value, color.blue * value, 1.0))
            } else writer.setColor(x, y, Color(1.0, 1.0, 1.0, 1.0))
        }
    }
    gc.drawImage(image, 0.0, 0.0)
}

fun getLightning(
    normals: ArrayList<DirectionVector>, dv: DirectionVector, Li: Double,
    kd: Double, backgroundLightning: Double
): Double {
    var l = 0.0
    var m = 0.0
    var n = 0.0
    for (dv in normals) {
        l += dv.l
        m += dv.m
        n += dv.n
    }
    val size = normals.size.toDouble()
    val normal = DirectionVector(l / size, m / size, n / size)
    val cos = angleBetweenVectors(normal, dv)
    return if (cos > 0) {
        var L0 = Li * kd * cos + backgroundLightning
        if (L0 > 1) L0 = 1.0
        L0
    } else
        backgroundLightning
}

fun textureOverlay(canvas: Canvas, gc: GraphicsContext, polygons: ArrayList<Polygon>, texture: Image) {
    val cWidth = canvas.width.toInt()
    val cHeight = canvas.height.toInt()
    val zBuff = Array(cWidth) {
        Array(cHeight) { Pair(Double.MAX_VALUE, Color(0.0, 0.0, 0.0, 0.0)) }
    }

    for (polygon in polygons) {
        var leftBound = cWidth
        var rightBound = 0
        var upperBound = 0
        var lowerBound = cHeight

        for (point in polygon.vertices) {
            if (point.x < leftBound && point.x >= 0)
                leftBound = ceil(point.x).toInt()
            if (point.x > rightBound && point.x < cWidth)
                rightBound = floor(point.x).toInt()
            if (point.y < lowerBound && point.y >= 0)
                lowerBound = ceil(point.y).toInt()
            if (point.y > upperBound && point.y < cHeight)
                upperBound = floor(point.y).toInt()
        }

        val normal = findNormal(
            polygon.vertices[0].point,
            polygon.vertices[1].point,
            polygon.vertices[2].point
        )
        val A = normal.l
        val B = normal.m
        val C = normal.n
        // высчитываем свободный член в уравнении плоскости
        val F = -(polygon.vertices[0].x * A) - (polygon.vertices[0].y * B) - (polygon.vertices[0].z * C)

        for (x in (leftBound..rightBound)) {
            for (y in (lowerBound..upperBound)) {
                val point = Point3D(x.toDouble(), y.toDouble(), 0.0)

                if (checkIsInPolygon(point, polygon)) {
                    val depth = findDepth(x, y, A, B, C, F)
                    val buf = zBuff[point.x.toInt()][point.y.toInt()]
                    if (depth < buf.first) {
                        val uv = interpolateTextureCoordinate(point, polygon)
                        val color = coordinateToPixel(uv, texture)
                        zBuff[point.x.toInt()][point.y.toInt()] = Pair(depth, color)
                    }
                }
            }
        }
    }

    val image = WritableImage(zBuff.size, zBuff[0].size)
    val writer = image.pixelWriter
    for (x in zBuff.indices) {
        for (y in zBuff[x].indices) {
            if (zBuff[x][y].first < Double.MAX_VALUE) {
                writer.setColor(x, y, zBuff[x][y].second)
            } else writer.setColor(x, y, Color(1.0, 1.0, 1.0, 0.0))
        }
    }
    gc.drawImage(image, 0.0, 0.0)
}

fun interpolateTextureCoordinate(point: Point3D, polygon: Polygon): TextureCoordinate {
    val p1 = polygon.vertices[0].point
    val p2 = polygon.vertices[1].point
    val p3 = polygon.vertices.last().point

    val e1 = DirectionVector(p2 - p1)
    val e2 = DirectionVector(p3 - p1)

    val n = polygon.normal
    val m = DirectionVector(e2.l * p1.x, e2.m * p1.y, e2.n * p1.z)
    val l = DirectionVector(p1.x * e1.l, p1.y * e1.m, p1.z * e1.n)

    val matrix = arrayOf(
        doubleArrayOf(m.l, m.m, m.n),
        doubleArrayOf(l.l, l.m, l.n),
        doubleArrayOf(n.l, n.m, n.n)
    )

    val pointMatrix = arrayOf(
            doubleArrayOf(point.x),
            doubleArrayOf(point.y),
            doubleArrayOf(1.0)
    )

    val deltas = multiplyMatrices(matrix, pointMatrix)
    val u = deltas[0][0] / deltas[2][0]
    val v =  deltas[1][0] / deltas[2][0]

    val pX = p1.x + e1.m * u + e2.m * v
    val pY = p1.y + e1.n * u + e2.n * v

    return TextureCoordinate(pX, pY)
}

fun coordinateToPixel(coordinate: TextureCoordinate, texture: Image): Color {
    val pixelReader = texture.pixelReader
    val x = coordinate.u * texture.width
    val y = coordinate.v * texture.height

    return pixelReader.getColor(x.toInt(), y.toInt())
}
