package lab2

import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.FileInputStream
import kotlin.math.roundToInt


class Task2(override val primaryStage: Stage): SceneWrapper(primaryStage, "Task 2") {
    init {
        val root = Group()
        val plotCanvases = Array<Canvas>(3) { Canvas(800.0, 600.0) }
        val axesCanvases = Array<Canvas>(3) { Canvas(800.0, 600.0) }
        val plotCtxs = plotCanvases.map { c -> c.graphicsContext2D }
        val axesCtxs = axesCanvases.map { c -> c.graphicsContext2D }

        root.children.addAll(plotCanvases)
        root.children.addAll(axesCanvases)

        val image = Image(FileInputStream("assets/fruits.jpg"))

        val reds: Array<Int> = Array(256) { 0 }
        val greens: Array<Int> = Array(256) { 0 }
        val blues: Array<Int> = Array(256) { 0 }

        val pixelReader = image.pixelReader
        for (y in (0 until image.height.toInt()))
            for (x in (0 until image.width.toInt())) {
                val color = pixelReader.getColor(x, y)
                reds[Math.round(color.red * 255).toInt()]++
                greens[Math.round(color.green * 255).toInt()]++
                blues[Math.round(color.blue * 255).toInt()]++
            }

        drawColorGist(
            plotCtxs[0], axesCtxs[0],
            Color.RED,
            reds,
            20.0, 195.0,
            760.0, 180.0
        )
        drawColorGist(
            plotCtxs[1], axesCtxs[1],
            Color.GREEN,
            greens,
            20.0, 390.0,
            760.0, 180.0
        )
        drawColorGist(
            plotCtxs[2], axesCtxs[2],
            Color.BLUE,
            blues,
            20.5, 585.0,
            760.0, 180.0
        )

        scene = Scene(root)
    }
}