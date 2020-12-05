package ind2kostikova

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import lab6.Point3D
import java.lang.Math.pow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class RayTracer(val width: Int, val height: Int) {
    val polyhedrons = ArrayList<Polyhedron>()
    val camera = Camera(Point3D(0.0, 3.0, -15.0), width, height)

    private val lights = listOf(
        Light(LightType.Ambient, 0.2, Point3D(0.0, 0.0, 0.0)),
        Light(LightType.Point, 0.2, Point3D(0.0, 9.0, 0.0)),
        Light(LightType.Point, 0.6, Point3D(-9.0, 9.0, -9.0))
    )

    private val viewPortW = 1
    private val viewPortH = 1
    private val projectionPlaneD = 1.0
    private val eps = 1E-3
    private val recurseDepth = 5
    private val backgroundColor = Color.BLACK

    fun showScene(gc: GraphicsContext) {
        genScene()
        val image = WritableImage(width, height)
        val pixelWriter = image.pixelWriter
        for (y in (-height / 2)..(height / 2))
            for (x in (-width / 2)..(width / 2)) {
                val D = сanvasToViewport(x, y, width.toDouble(), height.toDouble())
                val color = traceRay(camera.view.point1, D, 1.0, Double.MAX_VALUE, 1.0)
                val imgX = x + width / 2
                val imgY = height / 2 - y - 1
                if (imgX < 0 || imgX >= width || imgY < 0 || imgY >= height)
                    continue
                pixelWriter.setColor(imgX, imgY, color)
            }
        gc.drawImage(image, 0.0, 0.0)
        println("рендер окончен")
    }

    private fun increase(k: Double, c: Color): Color {
        val a = c.opacity
        val r = min(1.0, max(0.0, c.red * k + 0.05))
        val g = min(1.0, max(0.0, c.green * k + 0.05))
        val b = min(1.0, max(0.0, c.blue * k + 0.05))
        return Color(r, g, b, a)
    }

    private fun increase(k: Point3D, c: Color): Color {
        val a = c.opacity
        val r = min(1.0, max(0.0, (c.red * k.x + 0.05)))
        val g = min(1.0, max(0.0, (c.green * k.x + 0.05)))
        val b = min(1.0, max(0.0, (c.blue * k.x + 0.05)))
        return Color(r, g, b, a)
    }

    private fun sum(c1: Color, c2: Color): Color {
        val a = c1.opacity
        val r = max(0.0, min(1.0, c1.red + c2.red))
        val g = max(0.0, min(1.0, c1.green + c2.green))
        val b = max(0.0, min(1.0, c1.blue + c2.blue))
        return Color(r, g, b, a)
    }

    private fun dot(v1: Point3D, v2: Point3D): Double {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
    }

    private fun length(vec: Point3D): Double {
        return sqrt(dot(vec, vec))
    }

    private fun mul(k: Double, vec: Point3D): Point3D {
        return Point3D(k * vec.x, k * vec.y, k * vec.z)
    }

    private fun sum(vec1: Point3D, vec2: Point3D): Point3D {
        return Point3D(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z)
    }

    private fun sub(vec1: Point3D, vec2: Point3D): Point3D {
        return Point3D(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z)
    }

    private fun reflectRay(R: Point3D, N: Point3D): Point3D {
        return sub(mul(2 * dot(R, N), N), R)
    }

    private fun addSphere(
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

    private fun addCube(
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

    private fun addWall(
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

    private fun genScene() {
        addSphere(Point3D(-3.0, 2.0, 1.0), 1.0, Color.RED, 500.0)
        addSphere(Point3D(0.0, 3.0, -3.0), 1.0, Color.WHITE, 500.0, 1.0)
        addSphere(Point3D(3.0, 0.0, -4.0), 0.5, Color.WHITE, 500.0, 0.0, 1.0)

        addCube(0.75, Point3D(3.0, 1.0, 1.0), Color.BLUE, 500.0)
        addCube(0.75, Point3D(-1.0, 0.0, -3.0), Color.WHITE, 500.0, 1.0)
        addCube(0.5, Point3D(-3.0, 0.0, -5.0), Color.WHITE, 500.0, 0.0, 1.0)
        addCube(0.5, Point3D(-1.0, 0.0, -7.0), Color.GREEN, 500.0)

        var points = listOf(
            Point3D(-10.0, -1.0, -10.0),
            Point3D(-10.0, 10.0, -10.0),
            Point3D(10.0, 10.0, -10.0),
            Point3D(10.0, -1.0, -10.0)
        )
        addWall(points, arrayListOf(0.0, 0.0, -1.0), Color.DEEPPINK)
        points = listOf(
            Point3D(-10.0, -1.0, 10.0),
            Point3D(-10.0, 10.0, 10.0),
            Point3D(10.0, 10.0, 10.0),
            Point3D(10.0, -1.0, 10.0)
        )
        addWall(points, arrayListOf(0.0, 0.0, 1.0), Color.CHOCOLATE)
        points = listOf(
            Point3D(-10.0, -1.0, -10.0),
            Point3D(-10.0, -1.0, 10.0),
            Point3D(10.0, -1.0, 10.0),
            Point3D(10.0, -1.0, -10.0)
        )
        addWall(points, arrayListOf(0.0, -1.0, 0.0), Color.YELLOW)
        points = listOf(
            Point3D(-10.0, 10.0, -10.0),
            Point3D(-10.0, 10.0, 10.0),
            Point3D(10.0, 10.0, 10.0),
            Point3D(10.0, 10.0, -10.0)
        )
        addWall(points, arrayListOf(0.0, 1.0, 0.0), Color.GREEN)
        points = listOf(
            Point3D(-10.0, -1.0, -10.0),
            Point3D(-10.0, 10.0, -10.0),
            Point3D(-10.0, 10.0, 10.0),
            Point3D(-10.0, -1.0, 10.0)
        )
        addWall(points, arrayListOf(-1.0, 0.0, 0.0), Color.RED)
        points = listOf(
            Point3D(10.0, -1.0, -10.0),
            Point3D(10.0, 10.0, -10.0),
            Point3D(10.0, 10.0, 10.0),
            Point3D(10.0, -1.0, 10.0)
        )
        addWall(points, arrayListOf(1.0, 0.0, 0.0), Color.BLUE)
    }

    private fun сanvasToViewport(x: Int, y: Int, width: Double, height: Double): Point3D {
        val X = x * viewPortW / width
        val Y = y * viewPortH / height
        return Point3D(X, Y, projectionPlaneD)
    }

    private fun сlosestIntersection(
        camera: Point3D,
        D: Point3D,
        tMin: Double,
        tMax: Double
    ): Triple<Polyhedron, Double, Point3D> {
        var closestPolyhedron = Polyhedron(ArrayList())
        var closestT = Double.MAX_VALUE
        var norm = Point3D(0.0, 0.0, 0.0)

        for (polyhedron in polyhedrons) {
            if (polyhedron.isSphere) {
                val t = intersectRaySphere(camera, D, polyhedron)
                if (t.x < closestT && tMin < t.x && t.x < tMax) {
                    closestT = t.x
                    closestPolyhedron = polyhedron
                }
                if (t.y < closestT && tMin < t.y && t.y < tMax) {
                    closestT = t.y
                    closestPolyhedron = polyhedron
                }
            } else {
                val (t, normRes) = intersectRay(camera, D, polyhedron)
                if (t < closestT && tMin < t && t < tMax) {
                    closestT = t
                    closestPolyhedron = polyhedron
                    norm = normRes
                }
            }
        }

        if (closestPolyhedron.isSphere) {
            val point = sum(camera, mul(closestT, D))
            norm = sub(point, closestPolyhedron.center)
        }

        return Triple(closestPolyhedron, closestT, norm)
    }

    private fun traceRay(camera: Point3D, D: Point3D, tMin: Double, tMax: Double, depth: Double, step: Int = 0): Color {
        var (closest, closestT, normal) = сlosestIntersection(camera, D, tMin, tMax)
        if (closest.faces.size == 0)
            return backgroundColor

        normal = mul(1.0 / length(normal), normal)
        val point = sum(camera, mul(closestT, D))
        val lightK = computeLighting(point, normal, mul(-1.0, D), closest.specular)
        val localColor = increase(lightK, closest.color)
        if (step > recurseDepth || depth <= eps)
            return localColor

        val reflectedRay = reflectRay(mul(-1.0, D), normal)
        val reflectionColor = traceRay(point, reflectedRay, eps, Double.MAX_VALUE, depth * closest.reflective, step + 1)
        val reflected = sum(increase(1 - closest.reflective, localColor), increase(closest.reflective, reflectionColor))
        if (closest.transparent <= 0)
            return increase(depth, reflected)

        val refracted = refract(D, normal, 1.5)
        val trColor = traceRay(point, refracted, eps, Double.MAX_VALUE, depth * closest.transparent, step + 1)
        val transparent = sum(increase(1 - closest.transparent, reflected), increase(closest.transparent, trColor))
        return increase(depth, transparent)
    }

    fun clip(value: Double, min: Double, max: Double): Double {
        return if (value < min) min else if (value > max) max else value
    }

    private fun refract(I: Point3D, N: Point3D, ior: Double): Point3D {
        val res = Point3D(0.0, 0.0, 0.0)
        var cosi = clip(dot(I, N), -1.0, 1.0)
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
        val k = 1.0 - eta * eta * (1.0 - cosi * cosi)
        return if (k < 0) res
        else sum(mul(eta, I), mul(((eta * cosi - sqrt(k))), n))
    }

    private fun computeLighting(point: Point3D, normal: Point3D, view: Point3D, specular: Double): Point3D {
        val intensity = Point3D(0.0, 0.0, 0.0)
        val lengthN = length(normal)
        val lengthV = length(view)
        var tMax: Double
        for (light in lights) {
            if (light.type == LightType.Ambient) {
                intensity.x += light.rIntensity
                intensity.y += light.gIntensity
                intensity.z += light.bIntensity
            } else {
                var vectorLight: Point3D
                if (light.type == LightType.Point) {
                    vectorLight = sub(light.position, point)
                    tMax = 1.0
                } else {
                    vectorLight = light.position
                    tMax = Double.MAX_VALUE
                }
                val (blocker, _, _) = сlosestIntersection(point, vectorLight, eps, tMax)
                val tr = 1.0
                if (blocker.faces.size == 0)
                    continue
                val nDotL = dot(normal, vectorLight)
                if (nDotL > 0) {
                    intensity.x += tr * light.rIntensity * nDotL / (lengthN * length(vectorLight))
                    intensity.y += tr * light.gIntensity * nDotL / (lengthN * length(vectorLight))
                    intensity.z += tr * light.bIntensity * nDotL / (lengthN * length(vectorLight))
                }
                if (specular > 0) {
                    val vecR = reflectRay(vectorLight, normal)
                    val rDotV = dot(vecR, view)
                    if (rDotV > 0) {
                        intensity.x += tr * light.rIntensity * pow(rDotV / (length(vecR) * lengthV), specular)
                        intensity.y += tr * light.gIntensity * pow(rDotV / (length(vecR) * lengthV), specular)
                        intensity.z += tr * light.bIntensity * pow(rDotV / (length(vecR) * lengthV), specular)
                    }
                }
            }
        }
        return intensity
    }

    private fun intersectRay(camera: Point3D, D: Point3D, polyhedron: Polyhedron): Pair<Double, Point3D> {
        var res = Double.MAX_VALUE
        var norm = Point3D(0.0, 0.0, 0.0)
        for (i in 0 until polyhedron.faces.size) {
            val n = polyhedron.faces[i].normal
            val normal = Point3D(n[0], n[1], n[2])
            mul(1.0 / length(normal), normal)
            val dN = dot(D, normal)
            if (dN < eps)
                continue
            val d = dot(sub(polyhedron.faces[i].center, camera), normal) / dN
            if (d < 0)
                continue
            val point = sum(camera, mul(d, D))
            if (res > d && polyhedron.faces[i].inside(point)) {
                res = d
                norm = mul(-1.0, normal)
            }
        }

        return Pair(res, norm)
    }

    private fun intersectRaySphere(camera: Point3D, D: Point3D, sphere: Polyhedron): Point2D {
        val radius = sphere.sphereRadius
        val OC = sub(camera, sphere.center)
        val k1 = dot(D, D)
        val k2 = 2 * dot(OC, D)
        val k3 = dot(OC, OC) - radius * radius
        val discriminant = k2 * k2 - 4 * k1 * k3
        if (discriminant < 0)
            return Point2D(Double.MAX_VALUE, Double.MAX_VALUE)
        val t1 = (-k2 + sqrt(discriminant)) / (2 * k1)
        val t2 = (-k2 - sqrt(discriminant)) / (2 * k1)
        return Point2D(t1, t2)
    }
}