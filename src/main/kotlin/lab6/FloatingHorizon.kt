package lab6

fun floatingPlot(x0: Double, y0: Double, x1: Double, y1: Double, step: Double, f: (Double, Double) -> Double): Polyhedron {
    val polygons = ArrayList<Polygon>()
    val allVertices = ArrayList<Vertex>()
    var x = x0
    var index = 0
    while (x <= x1 - step) {
        val vertices = ArrayList<Vertex>()
        var y = y0
        while (y <= y1 - step) {
            val point = Point3D(x, y, f(x, y))
            vertices.add(Vertex(point, index++))
            y += step
        }
        val newPolygon = Polygon(vertices)
        newPolygon.vertices.forEach { v -> v.addPolygon(newPolygon) }
        polygons.add(newPolygon)
        allVertices.addAll(vertices)
        x += step
    }

    return Polyhedron(allVertices, polygons)
}