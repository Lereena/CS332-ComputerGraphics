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

        val redVals: Array<Array<Double>> = Array(image.height.toInt()) { Array(image.width.toInt()) {0.0}}
        val greenVals: Array<Array<Double>> = Array(image.height.toInt()) { Array(image.width.toInt()) {0.0}}
        val blueVals: Array<Array<Double>> = Array(image.height.toInt()) { Array(image.width.toInt()) {0.0}}

        val pixelReader = image.pixelReader
        for (y in (0 until image.height.toInt()))
            for (x in (0 until image.width.toInt())) {
                val color = pixelReader.getColor(x, y)
                redVals[y][x] = color.red
                greenVals[y][x] = color.green
                blueVals[y][x] = color.blue
                reds[Math.round(color.red * 255).toInt()]++
                greens[Math.round(color.green * 255).toInt()]++
                blues[Math.round(color.blue * 255).toInt()]++
            }

        val image1 = getImage(redVals, 'r')
        val image2 = getImage(greenVals, 'g')
        val image3 = getImage(blueVals, 'b')

        with(plotCtxs[0]) {
            drawImage(image1, 0.0, 0.0, 192.0, 128.5)
            drawImage(image2, 0.0, 138.5, 192.0, 128.5)
            drawImage(image3, 0.0, 277.0, 192.0, 128.5)
        }

        drawColorGist(
            plotCtxs[0], axesCtxs[0],
            Color.RED,
            reds,
            220.0, 195.0,
            560.0, 180.0
        )
        drawColorGist(
            plotCtxs[1], axesCtxs[1],
            Color.GREEN,
            greens,
            220.0, 390.0,
            560.0, 180.0
        )
        drawColorGist(
            plotCtxs[2], axesCtxs[2],
            Color.BLUE,
            blues,
            220.5, 585.0,
            560.0, 180.0
        )

        scene = Scene(root)
    }

    fun getImage(values: Array<Array<Double>>, comp: Char): Image {
        val image = WritableImage(values[0].size, values.size)
        val writer = image.pixelWriter
        for (y in (0 until values.size))
            for (x in (0 until values[0].size)) {
                val value = values[y][x]
                when (comp) {
                    'r' -> writer.setColor(x, y, Color(value, 0.0, 0.0, 1.0))
                    'g' -> writer.setColor(x, y, Color(0.0, value, 0.0, 1.0))
                    'b' -> writer.setColor(x, y, Color(0.0, 0.0, value, 1.0))
                }
            }
        return image
    }
}