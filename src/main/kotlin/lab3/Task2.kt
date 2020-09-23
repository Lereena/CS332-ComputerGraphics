package lab3

import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import java.io.FileInputStream
import java.util.*

class Task2(override val primaryStage: Stage): SceneWrapper(primaryStage, "Task 2") {
    private var mainCanvas = Canvas()
    private var mainCtx = mainCanvas.graphicsContext2D
    private var width = 0
    private var height = 0

    init {
        val root = Group()

        val image = Image(FileInputStream("assets/lines.png"))
        width = image.width.toInt()
        height = image.height.toInt()
        mainCanvas = Canvas(image.width, image.height)

        mainCtx = mainCanvas.graphicsContext2D
        mainCtx.drawImage(image, 0.0, 0.0, image.width, image.height)
        root.children.add(mainCanvas)

        scene = Scene(root)

        mainCanvas.setOnMouseClicked {
            val startPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
            highlightBorder(startPoint, image)
        }
    }

    private fun highlightBorder(startPoint: Point, image: Image) {
        val pixelWriter = mainCtx.pixelWriter

        val firstPixel = getFirstPixel(startPoint, image)
        if (firstPixel.x == -1)
            return

    }

    private fun getFirstPixel(startPoint: Point, image: Image): Point {
        val pixelReader = image.pixelReader
        for (y in (startPoint.y until height)) {
            val pixel = pixelReader.getArgb(startPoint.x, y)
            val test = pixel.toString()

            if (pixel.toLong() == 0xff000000) // If pixel is black
                return Point(startPoint.x, startPoint.y)
        }
        return Point(-1, -1)
    }

    private fun startPath(firstPoint: Point, image: Image): Vector<Point> {
        var result = Vector<Point>()

        var pixelReader = image.pixelReader

        var currentPoint = firstPoint
        var dir = 4 // Going left

        for (i in (0 until 8)) {
            val point = nextPoint(currentPoint, dir)
            val color = pixelReader.getArgb(point.x, point.y).toLong()
            if (color == 0xff000000) {
                dir -= 2
                if (dir < 0)
                    dir += 8
                currentPoint = point
            }
            if (++dir == 8)
                dir = 0
        }

        while (true) {
            break
        }

        return result
    }

    private fun nextPoint(currentPoint: Point, dir: Int): Point {
        /*  3 2 1
            4 X 0
            5 6 7 */
        
        val x = currentPoint.x
        val y = currentPoint.y
        return when(dir) {
            0 -> Point(x + 1, y)
            1 -> Point(x + 1, y - 1)
            2 -> Point(x,     y - 1)
            3 -> Point(x - 1, y - 1)
            4 -> Point(x - 1, y)
            5 -> Point(x - 1, y + 1)
            6 -> Point(x,     y + 1)
            7 -> Point(x + 1, y + 1)
            else -> throw Exception("Invalid direction")
        }
    }
}