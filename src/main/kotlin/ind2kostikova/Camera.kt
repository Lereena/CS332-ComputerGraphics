package ind2kostikova

import lab6.Point3D

class Camera(point: Point3D, width: Int, height: Int) {
    val view = Edge(Point3D(point.x, point.y, point.z), Point3D(point.x, point.y, point.z + 1))
}
