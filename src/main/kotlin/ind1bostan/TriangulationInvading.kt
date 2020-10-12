package ind1bostan

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ToggleButton
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab4.Position


class TriangulationInvading : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Triangulation via point invasion"
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val width = 800.0
        val height = 600.0
        val canvas = Canvas(width, height)
        val mainCtx = canvas.graphicsContext2D
        mainCtx.stroke = Color.BLACK

        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        val drawButton = ToggleButton("Рисовать многоугольник")
        root.children.addAll(drawButton)
        primaryStage.show()

        val polygon = Polygon()

        drawButton.setOnMouseClicked {
            if (drawButton.isSelected) {
                mainCtx.clearRect(0.0, 0.0, canvas.width, canvas.height)
                polygon.clear()
                mainCtx.beginPath()
            }
            else {
                mainCtx.clearRect(0.0, 0.0, canvas.width, canvas.height)
                mainCtx.beginPath()
                val triangles = triangulatePolygon(polygon)
                for (triangle in triangles) {
                    drawPolygon(triangle, mainCtx)
                }
            }
        }

        canvas.setOnMouseClicked {
            if (drawButton.isSelected) {
                val newPoint = DoublePoint(it.sceneX, it.sceneY)
                if (polygon.count() != 0) {
                    val lastPoint = polygon.last()
                    mainCtx.strokeLine(lastPoint.x, lastPoint.y, newPoint.x, newPoint.y)
                }
                mainCtx.strokeOval(it.sceneX - 1.0, it.sceneY - 1.0, 2.0, 2.0)
                polygon.addVertex(newPoint)
            }
        }
    }

    // Get List of triangles
    private fun triangulatePolygon(polygon: Polygon): List<Polygon> {
        if (polygon.count() <= 3)
            return listOf(polygon)

        var curVertex = polygon.root!!
        var prevVertex = curVertex.prev!!
        var nextVertex = curVertex.next!!

        while (true) {
            if (isVertexConvex(curVertex)) {
                val triangle = listOf(prevVertex.point, curVertex.point, nextVertex.point)
                val invPoint = getInvadingPoint(triangle, polygon)
                if (invPoint == null) {
                    polygon.remove(curVertex) // Check if this deletes
                    return listOf(polygonOf(triangle[0], triangle[1], triangle[2])) +
                            triangulatePolygon(polygon)
                } else {
                    val newPoly = polygon.split(curVertex, invPoint)
                    return triangulatePolygon(polygon) + triangulatePolygon(newPoly)
                }
            }
            prevVertex = curVertex
            curVertex = nextVertex
            nextVertex = nextVertex.next!!
        }
    }

    private fun isVertexConvex(vertex: Vertex): Boolean {
        val b = vertex.prev!!
        val a = b.prev!!

        return classifyPoint(vertex.point, a.point, b.point) == Position.Right
    }

    fun classifyPoint(p: DoublePoint, p1: DoublePoint, p2: DoublePoint): Position {
        var a = DoublePoint(p2.x - p1.x, p2.y - p1.y)
        var b = DoublePoint(p.x - p1.x, p.y - p1.y)
        val sa =  a.x * b.y - b.x * a.y;

        if (sa > 0.0)
            return Position.Left
        if (sa < 0.0)
            return Position.Right
        return Position.Belongs
    }

    private fun getInvadingPoint(triangle: List<DoublePoint>, polygon: Polygon): Vertex? {
        var minD = 0.0;
        var result: Vertex? = null
        var curVertex = polygon.root!!
        do {
            if (!vertexInTriangle(curVertex, triangle))
                continue
            val dist = distFromPointToEdge(curVertex, triangle[2], triangle[0])
            if (dist > 0.0 && dist > minD) {
                minD = dist
                result = curVertex
            }
        } while (curVertex != polygon.root)
        return result
    }

    private fun vertexInTriangle(vertex: Vertex, triangle: List<DoublePoint>): Boolean {
        return  (classifyPoint(vertex.point, triangle[0], triangle[1]) != Position.Left) &&
                (classifyPoint(vertex.point, triangle[1], triangle[2]) != Position.Left) &&
                (classifyPoint(vertex.point, triangle[2], triangle[0]) != Position.Left)
    }

    private fun distFromPointToEdge(vertex: Vertex, a: DoublePoint, b: DoublePoint): Double {
        val A = b.y - a.y
        val B = a.x - b.x
        val C = -(A * b.x + B * b.y)
        return Math.abs(A * vertex.x + B * vertex.y + C) / Math.sqrt(A*A + B*B)
    }

    private fun drawPolygon(polygon: Polygon, ctx: GraphicsContext) {
        var vertex = polygon.root!!
        var prev = vertex.prev!!
        do {
            ctx.strokeLine(prev.x, prev.y, vertex.x, vertex.y)
            prev = vertex
            vertex = vertex.next!!
        } while (vertex != polygon.root)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(TriangulationInvading::class.java, *args)
        }
    }
}
