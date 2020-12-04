package ind2kostikova

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import lab6.Point3D
import java.lang.Math.pow
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
    val projectionPlaneD = 1.0
    val inf = 1000000
    val eps = 1E-3
    val recurseDepth = 5
    val backgroundColor = Color.BLACK

    fun ShowScene(gc: GraphicsContext) {
        val image = WritableImage(width, height)
        val pixelWriter = image.pixelWriter
        for (y in -height / 2..height / 2)
            for (x in -width / 2..width / 2) {
                val D = CanvasToViewport(x, y, width.toDouble(), height.toDouble())
                val color = TraceRay(camera.view.point1, D, 1.0, Double.MAX_VALUE, 1.0, 0)
                val bmpx = x + width / 2
                val bmpy = height / 2 - y - 1
                if (bmpx < 0 || bmpx >= width || bmpy < 0 || bmpy >= height)
                    continue
                pixelWriter.setColor(bmpx, bmpy, color)
            }
        gc.drawImage(image, 0.0, 0.0)
    }


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

    private fun Sum(c1: Color, c2: Color): Color {
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
        val sphere = Polyhedron(ArrayList())
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
        val cube = Polyhedron(ArrayList())
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
            Point3D(-10.0, -1.0, -10.0),
            Point3D(-10.0, 10.0, -10.0),
            Point3D(10.0, 10.0, -10.0),
            Point3D(10.0, -1.0, -10.0)
        )
        AddWall(points, arrayListOf(0.0, 0.0, -1.0), Color.DEEPPINK)
        points = listOf(
            Point3D(-10.0, -1.0, 10.0),
            Point3D(-10.0, 1.0, 10.0),
            Point3D(10.0, 10.0, 10.0),
            Point3D(10.0, -1.0, 10.0)
        )
        AddWall(points, arrayListOf(0.0, 0.0, 1.0), Color.CHOCOLATE)
        points = listOf(
            Point3D(-1.0, -1.0, -1.0),
            Point3D(-10.0, -1.0, 1.0),
            Point3D(10.0, -1.0, 10.0),
            Point3D(10.0, -1.0, -10.0)
        )
        AddWall(points, arrayListOf(0.0, -1.0, 0.0), Color.YELLOW)
        points = listOf(
            Point3D(-10.0, 10.0, -10.0),
            Point3D(-10.0, 10.0, 10.0),
            Point3D(10.0, 10.0, 10.0),
            Point3D(10.0, 10.0, -10.0)
        )
        AddWall(points, arrayListOf(0.0, 1.0, 0.0), Color.GREEN)
        points = listOf(
            Point3D(-10.0, -1.0, -10.0),
            Point3D(-10.0, 10.0, -10.0),
            Point3D(-10.0, 10.0, 10.0),
            Point3D(-10.0, -1.0, 10.0)
        )
        AddWall(points, arrayListOf(-1.0, 0.0, 0.0), Color.RED)
        points = listOf(
            Point3D(10.0, -1.0, -10.0),
            Point3D(10.0, 10.0, -10.0),
            Point3D(10.0, 10.0, 10.0),
            Point3D(10.0, -1.0, 10.0)
        )
        AddWall(points, arrayListOf(1.0, 0.0, 0.0), Color.BLUE)
    }

    private fun CanvasToViewport(x: Int, y: Int, width: Double, height: Double): Point3D {
        val X = x * viewPortW / width
        val Y = y * viewPortH / height
        return Point3D(X, Y, projectionPlaneD)
    }

    private fun ClosestIntersection(
        camera: Point3D,
        D: Point3D,
        t_min: Double,
        t_max: Double
    ): Triple<Polyhedron, Double, Point3D> {
        var closestT = Double.MAX_VALUE
        var closest = Polyhedron(ArrayList())
        var norm = Point3D(0.0, 0.0, 0.0)

        for (polyhedron in polyhedrons) {
            if (polyhedron.isSphere) {
                val t = IntersectRaySphere(camera, D, polyhedron)
                if (t.x < closestT && t_min < t.x && t.x < t_max) {
                    closestT = t.x
                    closest = polyhedron
                }
                if (t.y < closestT && t_min < t.y && t.y < t_max) {
                    closestT = t.y
                    closest = polyhedron
                }
            } else {
                val (t, norm_res) = IntersectRay(camera, D, polyhedron)
                if (t < closestT && t_min < t && t < t_max) {
                    closestT = t
                    closest = polyhedron
                    norm = norm_res
                }
            }
        }

        if (closest.isSphere) {
            val point = Sum(camera, Mul(closestT, D))
            norm = Sub(point, closest.center)
        }

        return Triple(closest, closestT, norm)
    }

    private fun TraceRay(camera: Point3D, D: Point3D, t_min: Double, t_max: Double, depth: Double, step: Int): Color {
        var (closest, closestT, normal) = ClosestIntersection(camera, D, t_min, t_max)
        if (closest.faces.size == 0)
            return backgroundColor

        normal = Mul(1.0 / Length(normal), normal)
        val point = Sum(camera, Mul(closestT, D))
        val lightK = ComputeLighting(point, normal, Mul(-1.0, D), closest.specular)
        val local = Increase(lightK, closest.color)
        if (step > recurseDepth || depth <= eps)
            return local

        val r = ReflectRay(Mul(-1.0, D), normal)
        val reflectionColor = TraceRay(point, r, eps, Double.MAX_VALUE, depth * closest.reflective, step + 1)
        val reflected = Sum(Increase(1 - closest.reflective, local), Increase(closest.reflective, reflectionColor))
        if (closest.transparent <= 0)
            return Increase(depth, reflected)

        val refracted = Refract(D, normal, 1.5)
        val trColor = TraceRay(point, refracted, eps, Double.MAX_VALUE, depth * closest.transparent, step + 1)
        val transparent = Sum(Increase(1 - closest.transparent, reflected), Increase(closest.transparent, trColor))
        return Increase(depth, transparent)
    }

    fun Clip(value: Double, min: Double, max: Double): Double {
        return if (value < min) min else if (value > max) max else value
    }

    private fun Refract(I: Point3D, N: Point3D, ior: Double): Point3D {
        val res = Point3D(0.0, 0.0, 0.0)
        var cosi = Clip(Dot(I, N), -1.0, 1.0)
        var etai = 1.0
        var etat = ior
        var n = Point3D(N.x, N.y, N.z)
        if (cosi < 0)
            cosi = -cosi
        else {
            etai = ior
            etat = 1.0
            n = Point3D(-N.x, -N.y, -N.z)
        }
        val eta = etai / etat
        val k = 1 - eta * eta * (1 - cosi * cosi)
        return if (k < 0) res
        else Sum(Mul(eta, I), Mul(((eta * cosi - sqrt(k))), n))
    }

    private fun ComputeLighting(point: Point3D, normal: Point3D, view: Point3D, specular: Double): Point3D {
        var intensity = Point3D(0.0, 0.0, 0.0)
        val lengthN = Length(normal)
        val lengthV = Length(view)
        var tMax = 0.0
        for (light in lights) {
            if (light.type == LightType.Ambient) {
                intensity.x += light.rIntensity
                intensity.y += light.gIntensity
                intensity.z += light.bIntensity
            } else {
                var vectorLight: Point3D
                if (light.type == LightType.Point) {
                    vectorLight = Sub(light.position, point)
                    tMax = 1.0
                } else {
                    vectorLight = light.position
                    tMax = Double.MAX_VALUE
                }
                val (blocker, _, _) = ClosestIntersection(point, vectorLight, eps, tMax)
                val tr = 1
                if (blocker.faces.size == 0)
                    continue
                val nDotL = Dot(normal, vectorLight)
                if (nDotL > 0) {
                    intensity.x += tr * light.rIntensity * nDotL / (lengthN * Length(vectorLight))
                    intensity.y += tr * light.gIntensity * nDotL / (lengthN * Length(vectorLight))
                    intensity.z += tr * light.bIntensity * nDotL / (lengthN * Length(vectorLight))
                }
                if (specular > 0) {
                    val vecR = ReflectRay(vectorLight, normal)
                    val rDotV = Dot(vecR, view)
                    if (rDotV > 0) {
                        intensity.x += tr * light.rIntensity * pow(rDotV / (Length(vecR) * lengthV), specular)
                        intensity.y += tr * light.gIntensity * pow(rDotV / (Length(vecR) * lengthV), specular)
                        intensity.z += tr * light.bIntensity * pow(rDotV / (Length(vecR) * lengthV), specular)
                    }
                }
            }
        }
        return intensity
    }

    private fun IntersectRay(camera: Point3D, D: Point3D, polyhedron: Polyhedron): Pair<Double, Point3D> {
        var res = Double.MAX_VALUE
        var norm = Point3D(0.0, 0.0, 0.0)
        for (i in 0 until polyhedron.faces.size) {
            val n = polyhedron.faces[i].normal
            val normal = Point3D(n[0], n[1], n[2])
            Mul(1f / Length(normal), normal)
            val d_n = Dot(D, normal)
            if (d_n < eps)
                continue
            val d = Dot(Sub(polyhedron.faces[i].center, camera), normal) / d_n
            if (d < 0)
                continue
            val point = Sum(camera, Mul(d, D))
            if (res > d && polyhedron.faces[i].inside(point)) {
                res = d
                norm = Mul(-1.0, normal)
            }
        }

        return Pair(res, norm)
    }

    private fun IntersectRaySphere(camera: Point3D, D: Point3D, sphere: Polyhedron): Point2D {
        val r = sphere.sphereRadius
        val OC = Sub(camera, sphere.center)
        val k1 = Dot(D, D)
        val k2 = 2 * Dot(OC, D)
        val k3 = (Dot(OC, OC) - r * r)
        val discriminant = k2 * k2 - 4 * k1 * k3
        if (discriminant < 0)
            return Point2D(Double.MAX_VALUE, Double.MAX_VALUE)
        val t1 = (-k2 + sqrt(discriminant)) / (2 * k1)
        val t2 = (-k2 - sqrt(discriminant)) / (2 * k1)
        return Point2D(t1, t2)
    }
}