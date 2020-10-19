package lab5

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import kotlin.math.pow
import kotlin.math.sqrt

class PointIntZ(val x: Int, val y: Int, var z: Double = 0.0) {
    override fun toString(): String {
        return "($x, $y)"
    }

    fun between(other: PointIntZ): PointIntZ {
        val newX = (other.x + x) / 2
        val newY = (other.y + y) / 2
        val newZ = (other.z + z) / 2
        return PointIntZ(newX, newY, newZ)
    }

    fun length(other: PointIntZ): Double {
        return sqrt(
                (other.y - y).toDouble().pow(2.0) +
                (other.x - x).toDouble().pow(2.0)
        )
    }
}

class Task2(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 2") {
    private var heightMap: List<List<PointIntZ>>
    private val R = 5.0

    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)
        root.vgap = 4.0; root.hgap = 8.0
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        scene = Scene(root)
        root.children.add(canvas)

        val stepsLabel = Label("Количество шагов:")
        val stepsCounter = Label("0")
        val actionButton = Button("Сделать один шаг")
        root.children.addAll(stepsLabel, stepsCounter, actionButton)

        heightMap = listOf(
                listOf(
                        PointIntZ(0, 0, randomHeight()),
                        PointIntZ(0, 511, randomHeight())),
                listOf(
                        PointIntZ(511, 0, randomHeight()),
                        PointIntZ(511, 511, randomHeight()))
        )

        drawMap(gc)

        actionButton.setOnMouseClicked {
            stepsCounter.text = (stepsCounter.text.toInt() + 1).toString()
            interpolate()
            drawMap(gc)
        }
    }

    private fun interpolate() {
        square()
        diamond()
    }

    private fun square() {
        val newMap = mutableListOf<List<PointIntZ>>()
        for (i in (1 until heightMap.size)) {
            val newLine1 = mutableListOf<PointIntZ>()
            val newLine2 = mutableListOf<PointIntZ>()

            for (j in (1 until heightMap.size)) {
                val point1 = heightMap[i - 1][j - 1]
                val point2 = heightMap[i - 1][j]
                val point3 = heightMap[i][j - 1]
                val point4 = heightMap[i][j]

                val newP1 = point1.between(point2)
                val newP2 = point1.between(point3)
                val newP3 = point1.between(point4)

                newLine1.add(point1); newLine1.add(newP1)
                newLine2.add(newP2); newLine2.add(newP3)

                val temp = point1.length(point4) * R
                newP3.z = normalizeHeight(average(point1, point2, point3, point4) +
                        Math.random() * (2 * temp) - temp)
            }
            newLine1.add(heightMap[i - 1].last())
            newLine2.add(heightMap[i - 1].last().between(heightMap[i].last()))
            newMap.add(newLine1); newMap.add(newLine2)
        }
        val newLine = mutableListOf<PointIntZ>()
        for (j in (1 until heightMap.size)) {
            val point1 = heightMap.last()[j - 1]
            val point2 = heightMap.last()[j]
            val newP = point1.between(point2)
            newLine.add(point1); newLine.add(newP)
        }
        newLine.add(heightMap.last().last())
        newMap.add(newLine)

        heightMap = newMap
    }

    private fun diamond() {
        val mult = 2.8 * (heightMap[0][1].x - heightMap[0][0].x)
        for (i in (0 until heightMap.size)) {
            val d = if (i % 2 == 0) 1 else 0

            for (j in (d until heightMap.size - d)) {
                val point = heightMap[i][j]
                val point1 = getSafe(i - 1, j)
                val point2 = getSafe(i + 1, j)
                val point3 = getSafe(i, j - 1)
                val point4 = getSafe(i, j + 1)

                val temp = mult * R
                point.z = normalizeHeight(average(point1, point2, point3, point4) +
                        Math.random() * (2 * temp) - temp)
            }
        }
    }

    private fun getSafe(y: Int, x: Int): PointIntZ? {
        if (y < 0 || y >= heightMap.size ||
            x < 0 || x >= heightMap.size)
            return null

        return heightMap[y][x]
    }

    private fun average(vararg points: PointIntZ?): Double {
        var result = 0.0
        for (point in points)
            if (point != null) {
                result += point.z
            }

        return result / points.size
    }

    private fun randomHeight(): Double {
        return Math.random() * 100 + 77
    }

    private fun drawMap(gc: GraphicsContext) {
        gc.clearRect(0.0, 0.0, 1000.0, 1000.0)
        for (i in (1 until heightMap.size)) {
            for (j in (1 until heightMap.size)) {
                val point1 = heightMap[i - 1][j - 1]
                val point2 = heightMap[i - 1][j]
                val point3 = heightMap[i][j - 1]
                val point4 = heightMap[i][j]
                val z = normalizeHeight(average(point1, point2, point3, point4))
                val color = heightToColor(z)
                gc.fill = color
                gc.fillRect(point1.x.toDouble(), point1.y.toDouble(),
                        (point4.x - point1.x).toDouble(), (point4.y - point1.y).toDouble())
            }
        }
    }

    private fun heightToColor(z: Double): Color {
        val temp = z / 255.0
        return Color(temp, temp, temp, 1.0)
    }

    private fun normalizeHeight(z: Double): Double {
        if (z < 0.0)
            return 0.0
        if (z > 255.0)
            return 255.0
        return z
    }
}