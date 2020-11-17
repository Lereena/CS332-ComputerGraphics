package lab6

fun floatingPlot(x0: Double, y0: Double, x1: Double, y1: Double, step: Double, f: (Double, Double) -> Double): Polyhedron {
    val polygons = ArrayList<Polygon>()
    val allPoints = ArrayList<Point3D>()
    var x = x0
    while (x <= x1 - step) {
        val points = ArrayList<Point3D>()
        var y = y0
        while (y <= y1 - step) {
            points.add(Point3D(x, y, f(x, y)))
            y += step
        }
        polygons.add(Polygon(points))
        allPoints.addAll(points)
        x += step
    }

    return Polyhedron(allPoints, polygons)
}