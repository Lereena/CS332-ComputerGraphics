package ind2kostikova

import lab6.Point3D
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Point2D(var x: Double, var y: Double)

class Face(var points: List<Point3D>) {
    var center = findCenter()
    var normal = ArrayList<Double>()
    var xconst = points.all { point -> point.x == points[0].x }
    var yconst = points.all { point -> point.y == points[0].y }
    var zconst = points.all { point -> point.z == points[0].z }

    constructor(face: Face): this(face.points) {
        points = face.points.asSequence().map { pt -> Point3D(pt.x, pt.y, pt.z) }.toList()
        center = Point3D(face.center)
        if (face.normal.size > 0)
            face.normal.asSequence().map { element -> normal.add(element) }
        xconst = face.xconst
        yconst = face.yconst
        zconst = face.zconst
    }

    private fun pointBelongs(e1: Point2D, e2: Point2D, pt: Point2D): Boolean {
        val a = e1.y - e2.y
        val b = e2.x - e1.x
        val c = e1.x * e2.y - e2.x * e1.y
        if (abs(a * pt.x + b * pt.y + c) > eps)
            return false

        return LEq(min(e1.x, e2.x), pt.x)
                && LEq(pt.x, max(e1.x, e2.x))
                && LEq(min(e1.y, e2.y), pt.y)
                && LEq(pt.y, max(e1.y, e2.y))
    }

    private fun isCrossed(first1: Point2D, first2: Point2D, second1: Point2D, second2: Point2D): Boolean {
        val a1 = first1.y - first2.y
        val b1 = first2.x - first1.x
        val c1 = first1.x * first2.y - first2.x * first1.y
        val a2 = second1.y - second2.y
        val b2 = second2.x - second1.x
        val c2 = second1.x * second2.y - second2.x * second1.y
        val zn = a1 * b2 - a2 * b1
        if (abs(zn) < eps)
            return false

        var x = -1.0 * (c1 * b2 - c2 * b1) / zn
        var y = -1.0 * (a1 * c2 - a2 * c1) / zn
        if (Eq(x, 0.0))
            x = 0.0
        if (Eq(y, 0.0))
            y = 0.0
        val toFirst = LEq(min(first1.x, first2.x), x)
                && LEq(x, max(first1.x, first2.x))
                && LEq(min(first1.y, first2.y), y)
                && LEq(y, max(first1.y, first2.y))
        val toSecond = LEq(min(second1.x, second2.x), x)
                && LEq(x, max(second1.x, second2.x))
                && LEq(min(second1.y, second2.y), y)
                && LEq(y, max(second1.y, second2.y))
        return toFirst && toSecond
    }

    fun inside(p: Point3D): Boolean {
        var count = 0
        if (zconst) {
            val pt = Point2D(p.x, p.y)
            val ray = Point2D(100000.0, pt.y)
            for (i in 1..points.size) {
                val tmp1 = Point2D(points[i - 1].x, points[i - 1].y)
                val tmp2 = Point2D(points[i % points.size].x, points[i % points.size].y)
                if (pointBelongs(tmp1, tmp2, pt))
                    return true
                if (Eq(tmp1.y, tmp2.y))
                    continue
                if (Eq(pt.y, min(tmp1.y, tmp2.y)))
                    continue
                if (Eq(pt.y, max(tmp1.y, tmp2.y)) && Less(pt.x, min(tmp1.x, tmp2.x)))
                    count++
                else if (isCrossed(tmp1, tmp2, pt, ray))
                    count++
            }
            return count % 2 != 0
        } else if (yconst) {
            val pt = Point2D(p.x, p.z)
            val ray = Point2D(100000.0, pt.y)
            for (i in 1..points.size) {
                val tmp1 = Point2D(points[i - 1].x, points[i - 1].z)
                val tmp2 = Point2D(points[i % points.size].x, points[i % points.size].z)
                if (pointBelongs(tmp1, tmp2, pt))
                    return true
                if (Eq(tmp1.y, tmp2.y))
                    continue
                if (Eq(pt.y, min(tmp1.y, tmp2.y)))
                    continue
                if (Eq(pt.y, max(tmp1.y, tmp2.y)) && Less(pt.x, min(tmp1.x, tmp2.x)))
                    count++
                else if (isCrossed(tmp1, tmp2, pt, ray))
                    count++
            }
            return count % 2 != 0
        } else if (xconst) {
            val pt = Point2D(p.y, p.z)
            val ray = Point2D(100000.0, pt.y)
            for (i in 1..points.size) {
                val tmp1 = Point2D(points[i - 1].y, points[i - 1].z)
                val tmp2 = Point2D(points[i % points.size].y, points[i % points.size].z)
                if (pointBelongs(tmp1, tmp2, pt))
                    return true
                if (Eq(tmp1.y, tmp2.y))
                    continue
                if (Eq(pt.y, min(tmp1.y, tmp2.y)))
                    continue
                if (Eq(pt.y, max(tmp1.y, tmp2.y)) && Less(pt.x, min(tmp1.x, tmp2.x)))
                    count++
                else if (isCrossed(tmp1, tmp2, pt, ray))
                    count++
            }
            return count % 2 != 0
        }
        return false
    }

    fun findNormal(polyhedronCenter: Point3D) {
        val Q = points[1]
        val R = points[2]
        val S = points[0]
        val QR = listOf(R.x - Q.x, R.y - Q.y, R.z - Q.z)
        val QS = listOf(S.x - Q.x, S.y - Q.y, S.z - Q.z)
        val result = arrayListOf(
            QR[1] * QS[2] - QR[2] * QS[1],
            -(QR[0] * QS[2] - QR[2] * QS[0]),
            QR[0] * QS[1] - QR[1] * QS[0]
        )

        val CQ = listOf(Q.x - polyhedronCenter.x, Q.y - polyhedronCenter.y, Q.z - polyhedronCenter.z)
        if (mulMatrix(result, 1, 3, CQ, 3, 1)[0] > eps) {
            result[0] = result[0] * -1
            result[1] = result[1] * -1
            result[2] = result[2] * -1
        }

        normal = result
    }

    fun translate(x: Double, y: Double, z: Double) {
        for (point in points) {
            val t = listOf(
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                x, y, z, 1.0
            )
            val xyz = listOf(point.x, point.y, point.z, 1.0)
            val c = mulMatrix(xyz, 1, 4, t, 4, 4)
            point.x = c[0]
            point.y = c[1]
            point.z = c[2]
        }

        center = findCenter()
    }

    private fun mulMatrix(matr1: List<Double>, m1: Int, n1: Int, matr2: List<Double>, m2: Int, n2: Int): List<Double> {
        if (n1 != m2)
            return ArrayList()

        val c = ArrayList<Double>()
        for (i in 0 until m1 * n2)
            c.add(0.0)

        for (i in 0 until m1)
            for (j in 0 until n2)
                for (r in 0 until n1)
                    c[i * m1 + j] += matr1.get(i * m1 + r) * matr2[r * n2 + j]

        return c
    }

    private fun findCenter(): Point3D {
        var x = 0.0
        var y = 0.0
        var z = 0.0
        for (point in points) {
            x += point.x
            y += point.y
            z += point.z
        }
        x /= points.size
        y /= points.size
        z /= points.size

        return Point3D(x, y, z)
    }
}

val eps = 1e-6f

fun Eq(d1: Double, d2: Double): Boolean {
    return abs(d1 - d2) < eps
}

fun Less(d1: Double, d2: Double): Boolean {
    return d1 < d2 && abs(d1 - d2) >= eps
}

fun LEq(b1: Double, b2: Double): Boolean {
    return Less(b1, b2) || Eq(b1, b2)
}
