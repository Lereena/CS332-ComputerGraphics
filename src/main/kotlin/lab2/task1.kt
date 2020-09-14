package lab2

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.FileInputStream


class Task1 : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Graphics program"
        val root = Group()
        val mainCanvas = Canvas(800.0, 600.0)
        val mainCtx = mainCanvas.graphicsContext2D
        val plotCanvas = Canvas(800.0, 600.0)
        val plotCtx = plotCanvas.graphicsContext2D
        root.children.add(plotCanvas)
        root.children.add(mainCanvas)
        primaryStage.scene = Scene(root)
        primaryStage.show()

        val image = Image(FileInputStream("assets/fruits.jpg"))
        val matrix1 = IntensityMatrix(image) { x -> 0.299 * x.red + 0.587 * x.green + 0.114 * x.blue }
        val matrix2 = IntensityMatrix(image) { x -> 0.2126 * x.red + 0.7152 * x.green + 0.0722 * x.blue }
        val matrixDif = matrix1 - matrix2
        val image1 = matrix1.getImage()
        val image2 = matrix2.getImage()
        val imageD = matrixDif.getImage()
        mainCtx.drawImage(image,  0.0, 0.0,   192.0, 128.5);
        mainCtx.drawImage(image1, 0.0, 138.5, 192.0, 128.5);
        mainCtx.drawImage(image2, 0.0, 277.0, 192.0, 128.5);
        mainCtx.drawImage(imageD, 0.0, 415.5, 192.0, 128.5);

        drawColorGist(
            plotCtx, mainCtx,
            Color.BLACK,
            matrix1.evalGist(),
            225.0, 290.0,
            550.0, 280.0
        )
        drawColorGist(
            plotCtx, mainCtx,
            Color.BLACK,
            matrix2.evalGist(),
            225.0, 590.0,
            550.0, 280.0
        )
    }

    fun drawColorGist(
        plotCtx: GraphicsContext, axesCtx: GraphicsContext,
        color: Color,
        values: Array<Int>,
        x0: Double, y0: Double,
        width: Double, height: Double
    ) {
        val max = values.max()!!

        plotCtx.stroke = color
        val kX = width / 256.0
        val kY = height / max
        plotCtx.lineWidth = kX
        for (x in (0 until 256)) {
            if (values[x] == 0)
                continue
            val xCord = x0 + x * kX
            plotCtx.moveTo(xCord, y0)
            plotCtx.lineTo(xCord, y0 - values[x] * kY)
        }
        plotCtx.stroke()

        axesCtx.stroke = Color.PURPLE
        axesCtx.lineWidth = 1.0
        axesCtx.moveTo(x0, y0)
        axesCtx.lineTo(x0, y0 - height)
        axesCtx.moveTo(x0, y0)
        axesCtx.lineTo(x0 + width, y0)
        axesCtx.stroke()
    }

    class IntensityMatrix {
        private var _matrix: Array<Array<Double>> = Array<Array<Double>>(0) { Array<Double>(0) { 0.0 } }
        private var _height: Int = 0
        private var _width: Int = 0

        constructor(image: Image, func: (Color) -> Double) {
            _height = image.height.toInt();
            _width = image.width.toInt();
            val matrix = Array<Array<Double>>(_height) {
                Array<Double>(_width) { 0.0 }
            }
            val pixelReader = image.pixelReader;
            for (y in (0 until _height))
                for (x in (0 until _width)) {
                    val color = pixelReader.getColor(x, y)
                    val value = func(color)
                    matrix[y][x] = value
                }
            _matrix = matrix;
        }

        constructor(matrix: Array<Array<Double>>) {
            _matrix = matrix;
            _height = matrix.size
            _width = matrix[0].size
        }

        operator fun minus(other: IntensityMatrix): IntensityMatrix {
            if (this._height != other._height ||
                this._width != other._width
            )
                throw Exception("Matrix sizes don't match")

            val matrix = Array<Array<Double>>(_height) {
                Array<Double>(_width) { 0.0 }
            }
            var min = 1.0
            for (y in (0 until this._height))
                for (x in (0 until this._width)) {
                    val diff = this._matrix[y][x] - other._matrix[y][x]
                    matrix[y][x] = diff
                    if (diff < min)
                        min = diff
                }

            for (y in (0 until this._height))
                for (x in (0 until this._width))
                    matrix[y][x] -= min

            return IntensityMatrix(matrix)
        }

        fun getImage(): Image {
            val image = WritableImage(_width, _height)
            val writer = image.pixelWriter
            for (y in (0 until this._height))
                for (x in (0 until this._width)) {
                    val value = _matrix[y][x]
                    writer.setColor(x, y, Color(value, value, value, 1.0))
                }
            return image
        }

        fun evalGist(): Array<Int> {
            val result = Array<Int>(256) { 0 }
            for (y in (0 until this._height))
                for (x in (0 until this._width)) {
                    val value = Math.round(_matrix[y][x] * 255).toInt()
                    result[value]++
                }
            return result
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Task1::class.java, *args)
        }
    }
}