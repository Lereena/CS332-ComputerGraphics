package ind2kostikova

import javafx.scene.paint.Color
import lab6.Point3D
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class RayTracer(val width: Int, val height: Int) {
    val polyhedrons = ArrayList<Polyhedron>()
    val lights = listOf(
        Light(LightType.Ambient, 0.2, Point3D(0.0, 0.0, 0.0)),
        Light(LightType.Point, 0.2, Point3D(0.0, 9.0, 0.0)),
        Light(LightType.Point, 0.6, Point3D(-9.0, 9.0, -9.0))
    )
    val camera = Camera(
        Point3D(0.0, 3.0, -15.0), width, height
    )

    val viewPortW = 1
    val viewPortH = 1
    val projectionPlaneD = 1
    val inf = 1000000
    val eps = 1E-3f
    val recurseDepth = 5
    val backgroundColor = Color.BLACK

    private fun Increase(k: Double, c: Color): Color {
        val a = c.opacity
        val r = min(255.0, max(0.0, (c.red * k + 0.5)))
        val g = min(255.0, max(0.0, (c.green * k + 0.5)))
        val b = min(255.0, max(0.0, (c.blue * k + 0.5)))
        return Color(r, g, b, a)
    }

    private fun Increase(k: Point3D, c: Color): Color {
        val a = c.opacity
        val r = min(255.0, max(0.0, (c.red * k.x + 0.5)))
        val g = min(255.0, max(0.0, (c.green * k.y + 0.5)))
        val b = min(255.0, max(0.0, (c.blue * k.z + 0.5)))
        return Color(r, g, b, a)
    }

    private fun Sum(c1: Color, c2: Color): Color? {
        val a = c1.opacity
        val r = max(0.0, min(255.0, c1.red + c2.red))
        val g = max(0.0, min(255.0, c1.green + c2.green))
        val b = max(0.0, min(255.0, c1.blue + c2.blue))
        return Color(r, g, b, a)
    }

    private fun Dot(v1: Point3D, v2: Point3D): Double {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
    }

    private fun Length(vec: Point3D): Double {
        return sqrt(Dot(vec, vec))
    }

    private fun Mul(k: Double, vec: Point3D): Point3D {
        return Point3D(k * vec.x, k * vec.y, k * vec.z)
    }

    private fun Sum(vec1: Point3D, vec2: Point3D): Point3D {
        return Point3D(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z)
    }

    private fun Sub(vec1: Point3D, vec2: Point3D): Point3D {
        return Point3D(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z)
    }

    private fun ReflectRay(R: Point3D, N: Point3D): Point3D {
        return Sub(Mul(2 * Dot(R, N), N), R)
    }

    private fun AddSphere(
        point: Point3D,
        radius: Double,
        color: Color,
        specular: Double = 0.0,
        reflective: Double = 0.0,
        transparent: Double = 0.0
    ) {
        val sphere = Polyhedron()
        sphere.sphere(point, radius)
        sphere.color = color
        sphere.specular = specular
        sphere.reflective = reflective
        sphere.transparent = transparent
        polyhedrons.add(sphere)
    }

    private fun AddCube(
        cube_half_size: Double,
        point: Point3D,
        color: Color,
        specular: Double = 0.0,
        reflective: Double = 0.0,
        transparent: Double = 0.0
    ) {
        val cube = Polyhedron()
        cube.hexahedron(cube_half_size)
        cube.translate(point.x, point.y, point.z)
        cube.color = color
        cube.specular = specular
        cube.reflective = reflective
        cube.transparent = transparent
        cube.findNormals()
        polyhedrons.add(cube)
    }

    private fun AddWall(
        points: List<Point3D>,
        normal: ArrayList<Double>,
        color: Color,
        specular: Double = 0.0,
        reflective: Double = 0.0
    ) {
        val f = Face(points)
        val wall = Polyhedron(arrayListOf(f))
        wall.faces[0].normal = normal
        wall.color = color
        wall.specular = specular
        wall.reflective = reflective
        polyhedrons.add(wall)
    }

    private fun GenScene() {
        AddSphere(Point3D(-3.0, 2.0, 1.0), 1.0, Color.RED, 500.0)
        AddSphere(Point3D(0.0, 3.0, -3.0), 1.0, Color.WHITE, 500.0, 1.0)
        AddSphere(Point3D(3.0, 0.0, -4.0), 0.5, Color.WHITE, 500.0, 0.0, 1.0)

        AddCube(0.75, Point3D(3.0, 1.0, 1.0), Color.BLUE, 500.0)
        AddCube(0.75, Point3D(-1.0, 0.0, -3.0), Color.WHITE, 500.0, 1.0)
        AddCube(0.5, Point3D(-3.0, 0.0, -5.0), Color.WHITE, 500.0, 0.0, 1.0)
        AddCube(0.5, Point3D(-1.0, 0.0, -7.0), Color.GREEN, 500.0)

        var points = listOf(
            Point3D(
                -10.0,
                -1.0,
                -10.0
            ), Point3D(-10.0, 10.0, -10.0), Point3D(10.0, 10.0, -10.0), Point3D(10.0, -1.0, -10.0)
        )
        AddWall(points, arrayListOf(0.0, 0.0, -1.0), Color.DEEPPINK)
        points = listOf(
            Point3D(
                -10.0,
                -1.0,
                10.0
            ), Point3D(-10.0, 1.0, 10.0), Point3D(10.0, 10.0, 10.0), Point3D(10.0, -1.0, 10.0)
        )
        AddWall(points, arrayListOf(0.0, 0.0, 1.0), Color.CHOCOLATE)
        points = listOf(
            Point3D(
                -1.0,
                -1.0,
                -1.0
            ), Point3D(-10.0, -1.0, 1.0), Point3D(10.0, -1.0, 10.0), Point3D(10.0, -1.0, -10.0)
        )
        AddWall(points, arrayListOf(0.0, -1.0, 0.0), Color.YELLOW)
        points = listOf(
            Point3D(
                -10.0,
                10.0,
                -10.0
            ), Point3D(-10.0, 10.0, 10.0), Point3D(10.0, 10.0, 10.0), Point3D(10.0, 10.0, -10.0)
        )
        AddWall(points, arrayListOf(0.0, 1.0, 0.0), Color.GREEN)
        points = listOf(
            Point3D(
                -10.0,
                -1.0,
                -10.0
            ), Point3D(-10.0, 10.0, -10.0), Point3D(-10.0, 10.0, 10.0), Point3D(-10.0, -1.0, 10.0)
        )
        AddWall(points, arrayListOf(-1.0, 0.0, 0.0), Color.RED)
        points = listOf(
            Point3D(
                10.0,
                -1.0,
                -10.0
            ), Point3D(10.0, 10.0, -10.0), Point3D(10.0, 10.0, 10.0), Point3D(10.0, -1.0, 10.0)
        )
        AddWall(points, arrayListOf(1.0, 0.0, 0.0), Color.BLUE)
    }
}