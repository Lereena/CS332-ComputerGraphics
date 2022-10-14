package ind2kostikova

import javafx.scene.paint.Color
import lab6.Point3D

enum class PolyhedronType { Hexahedron, Sphere }

//class Figure(val type: PolyhedronType, var size: Double, var specular: Double, var reflective: Double, var transparent: Double) {
//    var center = findCenter()
//
//    private fun findCenter(): Point3D {
//        var x = 0.0
//        var y = 0.0
//        var z = 0.0
//        for (face in faces) {
//            x += face.center.x
//            y += face.center.y
//            z += face.center.z
//        }
//        x /= faces.size
//        y /= faces.size
//        z /= faces.size
//
//        return Point3D(x, y, z)
//    }
//
//}

class Polyhedron(var faces: ArrayList<Face>) {
    var center = findCenter()
    var cubeSize = 0.0
    var isSphere = false
    var sphereRadius = 0.0
    var color = Color.WHITE
    var specular = 0.0
    var reflective = 0.0
    var transparent = 0.0

    init {
        if (faces.size != 0) {
            val newFaces = ArrayList<Face>()
            for (face in faces)
                newFaces.add(Face(face))
            faces = newFaces
        }
        center = findCenter()
    }

    fun hexahedron(cubeHalfSize: Double = 50.0) {
        val front = makeFace(
            Point3D(-cubeHalfSize, cubeHalfSize, cubeHalfSize),
            Point3D(cubeHalfSize, cubeHalfSize, cubeHalfSize),
            Point3D(cubeHalfSize, -cubeHalfSize, cubeHalfSize),
            Point3D(-cubeHalfSize, -cubeHalfSize, cubeHalfSize)
        )

        val back = makeFace(
            Point3D(-cubeHalfSize, cubeHalfSize, -cubeHalfSize),
            Point3D(-cubeHalfSize, -cubeHalfSize, -cubeHalfSize),
            Point3D(cubeHalfSize, -cubeHalfSize, -cubeHalfSize),
            Point3D(cubeHalfSize, cubeHalfSize, -cubeHalfSize)
        )

        val down = makeFace(front.points[2], back.points[2], back.points[1], front.points[3])
        val top = makeFace(back.points[0], back.points[3], front.points[1], front.points[0])
        val left = makeFace(back.points[0], front.points[0], front.points[3], back.points[1])
        val right = makeFace(back.points[3], back.points[2], front.points[2], front.points[1])

        faces = arrayListOf(front, back, down, top, left, right)
        cubeSize = 2 * cubeHalfSize
        center = findCenter()
    }

    fun sphere(center: Point3D, radius: Double) {
        isSphere = true
        sphereRadius = radius
        faces = arrayListOf(Face(listOf(Point3D(center))))
        this.center = center
    }

    fun findNormals() {
        for (face in faces)
            face.findNormal(center)
    }

    private fun makeFace(p1: Point3D, p2: Point3D, p3: Point3D, p4: Point3D): Face {
        return Face(listOf(Point3D(p1), Point3D(p2), Point3D(p3), Point3D(p4)))
    }

    private fun findCenter(): Point3D {
        var x = 0.0
        var y = 0.0
        var z = 0.0
        for (face in faces) {
            x += face.center.x
            y += face.center.y
            z += face.center.z
        }
        x /= faces.size
        y /= faces.size
        z /= faces.size

        return Point3D(x, y, z)
    }

    fun translate(x: Double, y: Double, z: Double) {
        for (face in faces)
            face.translate(x, y, z)
        center = findCenter()
    }
}
