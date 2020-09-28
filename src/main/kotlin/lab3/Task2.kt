package lab3

import javafx.geometry.Orientation
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import java.io.FileInputStream
import java.util.*

class DirPoint(val x: Int, val y: Int, val dir: Int) {
    fun same(other: DirPoint): Boolean {
        return x == other.x && y == other.y && dir == other.dir
    }
}

class Task2(override val primaryStage: Stage): SceneWrapper(primaryStage, "Task 2") {
    private var mainCanvas = Canvas()
    private var mainCtx = mainCanvas.graphicsContext2D
    private var width = 0
    private var height = 0

    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)

        val image = Image(FileInputStream("assets/lines.png"))
        width = image.width.toInt()
        height = image.height.toInt()
        mainCanvas = Canvas(image.width, image.height)

        mainCtx = mainCanvas.graphicsContext2D
        mainCtx.drawImage(image, 0.0, 0.0, image.width, image.height)
        root.children.add(mainCanvas)

        val clearButton = Button("Clear")
        root.children.add(clearButton)

        scene = Scene(root)

        clearButton.setOnMouseClicked {
            mainCtx.drawImage(image, 0.0, 0.0, image.width, image.height)
        }

        mainCanvas.setOnMouseClicked {
            val startPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
            highlightBorder(startPoint, image)
        }
    }

    private fun highlightBorder(startPoint: Point, image: Image) {
        val pixelWriter = mainCtx.pixelWriter

        val firstPoint = getFirstPoint(startPoint, image) ?: return
//        pixelWriter.setColor(startPoint.x, startPoint.y, Color.RED)

        val path = startPath(firstPoint, image)
        for (point in path)
            pixelWriter.setColor(point.x, point.y, Color.RED)
    }

    private fun getFirstPoint(startPoint: Point, image: Image): DirPoint? {
        val pixelReader = image.pixelReader
        // Going down
        for (y in (startPoint.y until height)) {
            val color = pixelReader.getColor(startPoint.x, y)

            if (color == Color.BLACK) // If pixel is black
                return DirPoint(startPoint.x, y, 6)
        }
        return null
    }

    private fun startPath(firstPoint: DirPoint, image: Image): Vector<DirPoint> {
        val result = Vector<DirPoint>()
        val pixelReader = image.pixelReader

        result.add(firstPoint)
        val beginPoint = nextPoint(firstPoint, pixelReader) ?: return result
        result.add(beginPoint)

        var currentPoint = beginPoint

        while (true) {
            currentPoint = nextPoint(currentPoint, pixelReader) ?: return result

            if (currentPoint.same(beginPoint))
                break

            result.add(currentPoint)
        }

        return result
    }

    private fun nextPoint(currentPoint: DirPoint, pixelReader: PixelReader): DirPoint? {
        var dir = currentPoint.dir - 3
        dir = normalizeDir(dir)

        for (i in (0 until 8)) {
            val point = pointInDir(currentPoint, dir)
            val color = pixelReader.getColor(point.x, point.y)

            if (color == Color.BLACK) {
                return point
            }
            dir += 1
            dir = normalizeDir(dir)
        }
        return null
    }

    private fun normalizeDir(dir: Int): Int {
        if (dir < 0)
            return dir + 8
        if (dir >= 8)
            return dir - 8
        return dir
    }

    private fun pointInDir(currentPoint: DirPoint, dir: Int): DirPoint {
        /*  3 2 1
            4 X 0
            5 6 7 */
        
        val x = currentPoint.x
        val y = currentPoint.y
        return when(dir) {
            0 -> DirPoint(x + 1, y, dir)
            1 -> DirPoint(x + 1, y - 1, dir)
            2 -> DirPoint(x,     y - 1, dir)
            3 -> DirPoint(x - 1, y - 1, dir)
            4 -> DirPoint(x - 1, y, dir)
            5 -> DirPoint(x - 1, y + 1, dir)
            6 -> DirPoint(x,     y + 1, dir)
            7 -> DirPoint(x + 1, y + 1, dir)
            else -> throw Exception("Invalid direction")
        }
    }
}