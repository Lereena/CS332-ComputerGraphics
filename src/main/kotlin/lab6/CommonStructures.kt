package lab6

import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.*

data class Point3D(var x: Double, var y: Double, var z: Double) {
    constructor(other: Point3D) : this(other.x, other.y, other.z)

    override fun equals(other: Any?): Boolean {
        if (other is Point3D)
            return this.x == other.x &&
                    this.y == other.y &&
                    this.z == other.z
        return false
    }

    operator fun minus(other: Point3D): Point3D {
        return Point3D(x - other.x, y - other.y, z - other.z)
    }

    operator fun plus(other: Point3D): Point3D {
        return Point3D(x + other.x, y + other.y, z + other.z)
    }
}

data class Pixel(var x: Int, val y: Int) {
    constructor(point: Point3D): this(point.x.toInt(), point.y.toInt())
}

data class TextureCoordinate(var u: Double, var v: Double, var w: Double? = null)

enum class Axis { X, Y, Z }

data class DirectionVector(var l: Double, var m: Double, var n: Double) {
    constructor(p: Point3D) : this(p.x, p.y, p.z)

    init {
        val len = Math.sqrt(l * l + m * m + n * n)
        l /= len
        m /= len
        n /= len
    }

    operator fun plus(other: DirectionVector): DirectionVector {
        return DirectionVector(l + other.l, m + other.m, n + other.n)
    }
}

class Polygon(var vertices: ArrayList<Vertex>) {
    var textureCoordinates: ArrayList<TextureCoordinate?>
    var edges = ArrayList<Line>()
    var normal = findNormal(vertices[0].point, vertices[1].point, vertices[2].point)

    init {
        textureCoordinates = ArrayList()
        textureCoordinates.add(TextureCoordinate(0.0, 0.0))
        textureCoordinates.add(TextureCoordinate(1.0, 0.0))
        for (i in 2..vertices.size - 2) {
            textureCoordinates.add(TextureCoordinate(0.0, 0.0))
        }
        textureCoordinates.add(TextureCoordinate(0.0, 1.0))

        var prevVertex = vertices.last()
        for (vertex in vertices) {
            edges.add(Line(prevVertex.point, vertex.point))
            prevVertex = vertex
        }
    }

    constructor(vertices: ArrayList<Vertex>, textureCoordinates: ArrayList<TextureCoordinate?>): this(vertices) {
        this.textureCoordinates = textureCoordinates
    }

    val indices = vertices.indices

    operator fun get(i: Int) = vertices[(i % vertices.size)]
//    fun add(point: Point3D) = vertices.add(point)
//    fun add(vertex: Vertex, textureCoordinate: TextureCoordinate? = null) {
//    }//textureVertices.add(Pair(vertex, textureCoordinate))

    fun updateNormal() {
        normal = findNormal(vertices[0].point, vertices[1].point, vertices[2].point)
    }
}

class Vertex(val point: Point3D, val index: Int) {
    val polygons = ArrayList<Polygon>()
    var normal = DirectionVector(0.0, 0.0, 0.0)
//    var textureCoordinate = TextureCoordinate(0.0, 0.0, 0.0)

    fun addPolygon(polygon: Polygon) {
        polygons.add(polygon)
    }

    fun updateNormal() {
        normal = DirectionVector(0.0, 0.0, 0.0)
        for (polygon in polygons)
            normal += polygon.normal
    }

    fun clone(): Vertex {
        return Vertex(Point3D(point), index)
    }

    var x: Double
        get() = this.point.x
        set(value) { this.point.x = value }
    var y: Double
        get() = this.point.y
        set(value) { this.point.y = value }
    var z: Double
        get() = this.point.z
        set(value) { this.point.z = value }
}

class Polyhedron {
//    var vertices = ArrayList<Point3D>()
    var vertices = ArrayList<Vertex>()
    var polygons = ArrayList<Polygon>()

    var textureCoordinates = ArrayList<TextureCoordinate>()
    var vertexToTexture = HashMap<Point3D, TextureCoordinate>()
    var vertexToNormal = HashMap<Point3D, DirectionVector>()
    var normals = ArrayList<DirectionVector>()
    var centerPoint = Point3D(0.0, 0.0, 0.0)

    constructor(filename: String) {
        var vertexCount = 0
        var vtCount = 0
        var vnCount = 0
        File(filename).forEachLine {
            if (it.isNotEmpty()) {
                val sLine = it.split(' ')
                when (sLine[0]) {
                    "v" -> {
                        val point = Point3D(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble())
                        vertices.add(Vertex(point, vertexCount++))
//                        vertices.add(Point3D(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble()))
                    }
                    "vt" -> {
                        val u = sLine[1].toDouble()
                        val v = sLine[2].toDouble()
                        val w = if (sLine.size == 3) null else sLine[3].toDouble()
                        textureCoordinates.add(TextureCoordinate(u, v, w))
//                        vertices[vtCount].textureCoordinate = TextureCoordinate(u, v, w)
                        vtCount++
                    }
//                    "vn" -> {
////                        normals.add(DirectionVector(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble()))
//                        vertices[vnCount].normal = DirectionVector(sLine[1].toDouble(), sLine[2].toDouble(), sLine[3].toDouble())
//                        vnCount++
//                    }
                    "f" -> {
//                        for (i in 1 until sLine.size) {
//                            val split = sLine[i].split('/')
//                            val vertexNumber = split[0].toInt() - 1
//                            val vertex = vertices[vertexNumber]
//                            points.add(vertex)
//                            if (split.size > 1 && split[1] != "")
//                                vertexToTexture[vertex] = textureCoordinates[split[1].toInt() - 1]
//                            if (split.size == 3 && split[2] != "" && normals.size > 0)
//                                vertexToNormal[vertex] = normals[split[2].toInt() - 1]
//
//                        }
//
//                        polygons.add(Polygon(points))

                        val newVertices = ArrayList<Vertex>()
                        val newTextureCoordinates = ArrayList<TextureCoordinate?>()


                        for (i in 1 until sLine.size) {
                            val split = sLine[i].split('/')
                            val vertexNumber = split[0].toInt() - 1
                            val vertex = vertices[vertexNumber]
                            newVertices.add(vertex)
                            if (split.size > 1 && split[1] != "")
                                newTextureCoordinates.add(textureCoordinates[split[1].toInt() - 1])
//                            if (split.size == 3 && split[2] != "" && normals.size > 0)
//                                vertex.normal = normals[split[2].toInt() - 1]
                        }
                        val newPolygon = Polygon(newVertices, newTextureCoordinates)
                        for (vertex in newPolygon.vertices)
                            vertex.addPolygon(newPolygon)
                        polygons.add(newPolygon)
                    }
                }
            }
        }
    }

    constructor(
        vertices: ArrayList<Vertex>, polygons: ArrayList<Polygon>,
        centerPoint: Point3D = Point3D(0.0, 0.0, 0.0)
    ) {
        this.vertices = vertices
        this.polygons = polygons
        this.centerPoint = centerPoint
        for (polygon in polygons) {
            if (polygon.vertices.size < 3)
                continue
            normals.add(findNormal(polygon[0].point, polygon[1].point, polygon[2].point))
        }
    }

    fun clone(): Polyhedron {
        val newVertices = ArrayList<Vertex>()
        val newPolygons = ArrayList<Polygon>()
        val newCenterPoint = Point3D(this.centerPoint)
        for (vertex in vertices)
            newVertices.add(vertex.clone())

        for (polygon in this.polygons) {
            val tempPoints = ArrayList<Vertex>()
            for (vertex in polygon.vertices) {
                val newVertex = newVertices[vertex.index]
                tempPoints.add(newVertex)
            }
            val newPolygon = Polygon(tempPoints)
            for (vertex in newPolygon.vertices)
                vertex.addPolygon(newPolygon)
            newPolygons.add(newPolygon)
        }

        return Polyhedron(newVertices, newPolygons, newCenterPoint)
    }

    fun faces(viewVector: DirectionVector): ArrayList<Polygon> {
        val result = ArrayList<Polygon>()

        for (polygon in polygons) {
            if (polygon.vertices.size < 3) {
                result.add(polygon)
                continue
            }

            val normal = findNormal(polygon[0].point, polygon[1].point, polygon[2].point)
            if (angleBetweenVectors(normal, viewVector) >= 0)
                result.add(polygon)
        }

        return result
    }
}

typealias Matrix = Array<DoubleArray>

data class Line(val point1: Point3D, val point2: Point3D) {
    val directionVector = normalizeDirectionVector()

    private fun normalizeDirectionVector(): DirectionVector {
        val l = point2.x - point1.x
        val m = point2.y - point1.y
        val n = point2.z - point1.z
        val length = sqrt(l * l + m * m + n * n)
        return DirectionVector(l / length, m / length, n / length)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Line)
            return false
        return point1.x == other.point1.x && point1.y == other.point1.y && point1.z == other.point1.z
    }
}
