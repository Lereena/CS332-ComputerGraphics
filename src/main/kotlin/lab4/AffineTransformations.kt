package lab4

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab3.Point
import java.util.*

class Shape(var points: LinkedList<Point>) {
    fun update(points: LinkedList<Point>) {
        this.points = points
    }
}

enum class Mode {
    NONE,

    DRAW_POINT,
    DRAW_LINE,
    DRAW_RECT,
    DRAW_POLY,

    SELECT_ROTATION_POINT
}

class AffineTransformations : Application() {
    var selectedMode: Mode = Mode.NONE
    var curPoints = LinkedList<Point>()
    val curShapes = Vector<Shape>()
    val mainCanvas = Canvas(800.0, 600.0)
    val mainGc = mainCanvas.graphicsContext2D

    override fun start(primaryStage: Stage) {
        val mainGroup = GridPane()
        mainGroup.setAlignment(Pos.BASELINE_CENTER)
        mainGroup.setHgap(10.0);
        mainGroup.setVgap(10.0);

        val canvasGroup = StackPane(mainCanvas)
        canvasGroup.border = Border(BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))

        val drawPane = FlowPane(Orientation.HORIZONTAL, 10.0, 0.0)
        val transformationPane = FlowPane(Orientation.VERTICAL, 0.0, 30.0)

        mainGroup.add(canvasGroup,        0, 0)
        mainGroup.add(drawPane,           0, 1)
        mainGroup.add(transformationPane, 1, 0)


        // DRAW PANE
        val drawPointButton =   ToggleButton("Точка")
        val drawLineButton =    ToggleButton("Линия")
        val drawRectButton =    ToggleButton("Треугольник")
        val drawPolygonButton = ToggleButton("Многоугольник")
        val clearButton =       Button("Очистить")

        drawPane.children.addAll(
                drawPointButton,
                drawLineButton,
                drawRectButton,
                drawPolygonButton,
                clearButton
        )

        // TRANSFORMATION PANE
        // move pane
        val trMovePane = GridPane()
        val trDxField = TextField("0")
        val trDyField = TextField("0")
        val trDxLabel = Label("dx: ")
        val trDyLabel = Label("dy: ")
        val trMoveButton = Button("Сдвинуть")
        trMovePane.add(trDxLabel,    0, 0)
        trMovePane.add(trDxField,    1, 0)
        trMovePane.add(trDyLabel,    0, 1)
        trMovePane.add(trDyField,    1, 1)
        trMovePane.add(trMoveButton, 0, 2)
        trMoveButton.setOnAction {
            for (shape in curShapes)
                shape.update(move(shape.points,
                        trDxField.text.toInt(), trDyField.text.toInt()))
            redrawShapes()
        }

        // rotate pane
        val trRotatePane = GridPane()
        val trAngleField = TextField("0")
        val trAngleLabel = Label("Угол: ")
        val trRotateButton = ToggleButton("Выбрать точку")
        trRotatePane.add(trAngleLabel, 0, 0)
        trRotatePane.add(trAngleField, 1, 0)
        trRotatePane.add(trRotateButton, 0, 2)

        transformationPane.children.addAll(
                trMovePane,
                trRotatePane
        )

        clearButton.setOnAction {
            mainGc.clearRect(0.0, 0.0, mainCanvas.getWidth(), mainCanvas.getHeight())
            mainGc.restore()
            curPoints.clear()
            curShapes.clear()
        }

        val toggleButtons = arrayOf(
                drawPointButton,
                drawLineButton,
                drawRectButton,
                drawPolygonButton,

                trRotateButton
        )
        setModeButton(drawPointButton,   Mode.DRAW_POINT, toggleButtons)
        setModeButton(drawLineButton,    Mode.DRAW_LINE,  toggleButtons)
        setModeButton(drawRectButton,    Mode.DRAW_RECT,  toggleButtons)
        setModeButton(drawPolygonButton, Mode.DRAW_POLY,  toggleButtons)

        setModeButton(trRotateButton,    Mode.SELECT_ROTATION_POINT, toggleButtons)

        mainCanvas.setOnMouseClicked {
            if (selectedMode != Mode.NONE) {
                val curPoint = Point(it.sceneX.toInt(), it.sceneY.toInt())
                curPoints.add(curPoint)

                when (selectedMode) {
                    Mode.DRAW_POINT -> { addShape() }
                    Mode.DRAW_LINE -> { if (curPoints.count() == 2) addShape() }
                    Mode.DRAW_RECT -> { if (curPoints.count() == 3) addShape() }
                    Mode.DRAW_POLY -> {}
                    Mode.SELECT_ROTATION_POINT -> {
                        for (shape in curShapes)
                            shape.update(turnAroundPoint(shape.points, curPoint, trAngleField.text.toDouble()))
                        redrawShapes()
                    }
                    else -> throw Exception("Invalid mode")
                }
            }
        }

        val scene = Scene(mainGroup)

        primaryStage.scene = scene
        primaryStage.show()
    }

    fun addShape() {
        val shape = Shape(curPoints)
        curShapes.add(shape)
        drawShape(shape)
        curPoints = LinkedList<Point>()
    }

    fun drawShape(shape: Shape) {
        if (shape.points.count() == 1) {
            val point = shape.points.first
            mainGc.strokeOval(
                    point.x - 3.0,
                    point.y - 3.0, 3.0, 3.0);
            return
        }

        var prevPoint = shape.points.last
        for (point in shape.points) {
            mainGc.strokeLine(point.x.toDouble(), point.y.toDouble(),
                prevPoint.x.toDouble(), prevPoint.y.toDouble())
            prevPoint = point
        }

        mainGc.stroke()
    }

    fun redrawShapes() {
        mainGc.clearRect(0.0, 0.0, mainCanvas.getWidth(), mainCanvas.getHeight())
        for (shape in curShapes)
            drawShape(shape)
    }

    fun setModeButton(button: ToggleButton, mode: Mode, all_buttons: Array<ToggleButton>) {
        button.setOnAction {
            val state = button.isSelected
            if (state) {
                disableButtons(all_buttons)
                button.isSelected = true
                selectedMode = mode
            }
            else {
                selectedMode = Mode.NONE
                addShape()
            }
        }
    }

    fun disableButtons(buttons: Array<ToggleButton>) {
        for (button in buttons)
            button.isSelected = false
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(AffineTransformations::class.java, *args)
        }
    }
}
