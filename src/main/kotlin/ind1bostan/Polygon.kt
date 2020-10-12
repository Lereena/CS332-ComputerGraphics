package ind1bostan

import lab4.Convexity

class DoublePoint (var x: Double, var y: Double)

class Vertex (var point: DoublePoint,
              var next: Vertex? = null,
              var prev: Vertex? = null) {
    var x = point.x
    var y = point.y
}

class Polygon() {
    public var root: Vertex? = null
    private var count = 0

    fun addVertex(point: DoublePoint) {
        count++
        if (root == null) {
            root = Vertex(point)
            root!!.next = root
            root!!.prev = root
            return
        }
        val newNode = Vertex(point, root, root!!.prev)
        if (root!!.next == root)
            root!!.next = newNode
        root!!.prev = newNode
    }

    fun first(): Vertex {
        return root!!
    }

    fun last(): Vertex {
        return root!!.prev!!
    }

    fun split (a: Vertex, b: Vertex): Polygon {
        val aT = Vertex(a.point, a.next, a.prev)
        val bT = Vertex(b.point, b.next, b.prev)
        aT.next = bT
        bT.prev = aT

        a.prev = b
        b.next = a
        root = a
        this.resize()

        val newPoly = Polygon()
        newPoly.root = aT
        newPoly.resize()

        return newPoly
    }

    fun remove(vertex: Vertex) {
        vertex.prev!!.next = vertex.next
        vertex.next!!.prev = vertex.prev
        if (vertex == root)
            root = vertex.next
    }

    fun count(): Int {
        return count
    }

    fun resize() {
        if (root == null) {
            count = 0
            return
        }
        count = 0
        var node = root
        do {
            count++
            node = node!!.next
        } while (node != root)
    }

    fun clear() {
        root = null
        count = 0
    }
}

fun polygonOf(vararg points: DoublePoint): Polygon {
    val result = Polygon()
    for (point in points)
        result.addVertex(point)
    return result
}