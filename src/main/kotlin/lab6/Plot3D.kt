package lab6

fun plot3D(x0: Double, y0: Double, x1: Double, y1: Double, step: Double, f: (Double, Double) -> Double): Polyhedron {
    val plot = Polyhedron(ArrayList(), ArrayList())
    val builtPoints = ArrayList<Point3D>()
    val pointNums = HashMap<Point3D, Int>()
    val relationships = HashMap<Int, ArrayList<Int>>()

    var i = 0
    var curEst = 0
    var x = x0
    while (x <= x1 - step) {
        var y = y0
        while (y <= y1 - step) {
            val currentPoints = arrayListOf(
                Point3D(x, y, f(x, y)),
                Point3D(x + step, y, f(x + step, y)),
                Point3D(x, y + step, f(x, y + step)),
                Point3D(x + step, y + step, f(x + step, y + step))
            )
            curEst = processPoints(currentPoints, plot, relationships, builtPoints, pointNums, curEst, i)
            i++
            y += step
        }
        x += step
    }
    if (x0 == x1) {
        var y = y0
        while (y <= y1 - step) {
            val currentPoints = ArrayList<Point3D>()
            currentPoints.add(Point3D(x0, y, f(x0, y)))
            currentPoints.add(Point3D(x0, y + step, f(x0, y + step)))
            curEst = processPoints(currentPoints, plot, relationships, builtPoints, pointNums, curEst, i)
            i++
            y += step
        }
    }
    if (y0 == y1) {
        x = x0
        while (x <= x1 - step) {
            val currentPoints = ArrayList<Point3D>()
            currentPoints.add(Point3D(x, y0, f(x, y0)))
            currentPoints.add(Point3D(x0 + step, y0, f(x0 + step, y0)))
            curEst = processPoints(currentPoints, plot, relationships, builtPoints, pointNums, curEst, i)
            i++
            x += step
        }
    }

    return plot
}

fun processPoints(
    currentPoints: ArrayList<Point3D>, plot: Polyhedron, relationships: HashMap<Int, ArrayList<Int>>,
    builtPoints: ArrayList<Point3D>, pointNums: HashMap<Point3D, Int>, curEst: Int, i: Int
): Int {
    val polygon = Polygon()
    var currentEst = curEst
    for (point in currentPoints) {
        if (!pointNums.containsKey(point)) {
            builtPoints.add(point)
            plot.vertices.add(point)
            pointNums[point] = currentEst
            currentEst++
        } else {
            polygon.add(builtPoints[pointNums[point]!!])
        }
    }
    if (polygon.points.size >= 3) plot.polygons.add(polygon)
    return currentEst
}
