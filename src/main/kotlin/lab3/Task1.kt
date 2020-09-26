package lab3

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import java.io.FileInputStream

class Task1(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 1") {
    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        scene = Scene(root)
        root.children.add(canvas)
        val fillButton = ToggleButton("Активировать заливку")
        root.children.add(fillButton)

        var startPoint: Point?
        var endPoint: Point? = null
        var image: WritableImage
        gc.drawImage(Image(FileInputStream("assets/blank.png")), 0.0, 0.0)

        root.setOnMouseClicked {
            startPoint = endPoint
            endPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
            if (startPoint != null && endPoint != null) {
                drawLine(gc, Color.BLACK, startPoint!!, endPoint!!)
            }
        }

        fillButton.setOnAction {
            if (fillButton.isSelected) {
                image = canvas.snapshot(null, null)
                root.setOnMouseClicked {
                    val start = Point(it.sceneX.toInt(), it.sceneY.toInt())
                    fill(gc, start, Color.GREEN, image)
                }
            }
            else {
                root.setOnMouseClicked {
                    startPoint = endPoint
                    endPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
                    if (startPoint != null && endPoint != null) {
                        drawLine(gc, Color.BLACK, startPoint!!, endPoint!!)
                    }
                }
            }
        }
    }

    fun fill(
        gc: GraphicsContext, startPoint: Point, targetColor: Color, image: WritableImage,
        first: Boolean = true, areaColor: Color? = null, direction: Direction? = null
    ) {
        val pixelReader = image.pixelReader
        val pixelColor = pixelReader.getColor(startPoint.x, startPoint.y)
        if (noNeedToHandle(areaColor, pixelColor, targetColor))
            return

        var xL = startPoint.x
        var xR = startPoint.x
        val y = startPoint.y

        while (pixelReader.getColor(xL, y) == pixelColor) xL--
        while (pixelReader.getColor(xR, y) == pixelColor) xR++
        val pixelWriter = image.pixelWriter
        for (x in (xL + 1) until xR)
            pixelWriter.setColor(x, y, targetColor)
        gc.drawImage(image, 0.0, 0.0)

        for (x in (xL + 1) until xR) {
            if (first || direction == Direction.UP)
                fill(gc, Point(x, y + 1), targetColor, image, false, pixelColor, Direction.UP)
            if (first || direction == Direction.DOWN)
                fill(gc, Point(x, y - 1), targetColor, image, false, pixelColor, Direction.DOWN)
        }
    }

    private fun noNeedToHandle(areaColor: Color?, currentColor: Color, targetColor: Color): Boolean {
        return (areaColor != null && currentColor != areaColor)
                || currentColor == targetColor
    }
}

enum class Direction { UP, DOWN }