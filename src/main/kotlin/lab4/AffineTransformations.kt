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

    SELECT_ROTATION_POINT,
    SELECT_SCALE_POINT,
    CHECK_POLYGONS,
    CHECK_EDGE
}

class AffineTransformations : Application() {
    var selectedMode: Mode = Mode.NONE
    var curPoints = LinkedList<Point>()
    val curShapes = Vector<Shape>()
    val curEdges = Vector<Shape>()

    val mainCanvas = Canvas(800.0, 600.0)
    val mainGc = mainCanvas.graphicsContext2D

    override fun start(primaryStage: Stage) {
        val mainGroup = GridPane()
        mainGroup.alignment = Pos.BASELINE_CENTER
        mainGroup.hgap = 10.0;
        mainGroup.vgap =10.0;

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

        clearButton.setOnAction {
            mainGc.clearRect(0.0, 0.0, mainCanvas.getWidth(), mainCanvas.getHeight())
            mainGc.restore()
            curPoints.clear()
            curShapes.clear()
        }

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
        val trMoveTitle = Label("Смещение")
        val trDxField = TextField("0")
        val trDyField = TextField("0")
        val trDxLabel = Label("dx: ")
        val trDyLabel = Label("dy: ")
        val trMoveButton = Button("Сдвинуть")
        trMovePane.add(trMoveTitle,  0, 0, 2, 1)
        trMovePane.add(trDxLabel,    0, 1)
        trMovePane.add(trDxField,    1, 1)
        trMovePane.add(trDyLabel,    0, 2)
        trMovePane.add(trDyField,    1, 2)
        trMovePane.add(trMoveButton, 0, 3, 2, 1)
        trMoveButton.setOnAction {
            for (shape in curShapes)
                shape.update(move(shape.points,
                        trDxField.text.toInt(), trDyField.text.toInt()))
            redrawShapes()
        }

        // rotate pane
        val trRotatePane = GridPane()
        val trRotateTitle = Label("Поворот")
        val trAngleField = TextField("0")
        val trAngleLabel = Label("Угол: ")
        val trRotatePointButton =  ToggleButton("Выбрать точку")
        val trRotateCenterButton = Button("Относительно центров")
        trRotatePane.add(trRotateTitle, 0, 0, 2, 1)
        trRotatePane.add(trAngleLabel,  0, 1)
        trRotatePane.add(trAngleField,  1, 1)
        trRotatePane.add(trRotatePointButton,  0, 3, 2, 1)
        trRotatePane.add(trRotateCenterButton, 0, 4, 2, 1)

        trRotateCenterButton.setOnAction {
            for (shape in curShapes)
                shape.update(turnAroundCenter(shape.points, trAngleField.text.toDouble()))
            redrawShapes()
        }

        // rotate pane
        val trScalePane = GridPane()
        val trScaleTitle = Label("Масштабирование")
        val trScaleKxField = TextField("0")
        val trScaleKyField = TextField("0")
        val trScaleKxLabel = Label("kx: ")
        val trScaleKyLabel = Label("ky: ")
        val trScalePointButton =   ToggleButton("Выбрать точку")
        val trScaleCenterButton =  Button("Относительно центров")
        trScalePane.add(trScaleTitle,   0, 0, 2, 1)
        trScalePane.add(trScaleKxLabel, 0, 1)
        trScalePane.add(trScaleKxField, 1, 1)
        trScalePane.add(trScaleKyLabel, 0, 2)
        trScalePane.add(trScaleKyField, 1, 2)
        trScalePane.add(trScalePointButton,  0, 3, 2, 1)
        trScalePane.add(trScaleCenterButton, 0, 4, 2, 1)

        trScaleCenterButton.setOnAction {
            for (shape in curShapes)
                shape.update(scaleAroundCenter(shape.points,
                        trScaleKxField.text.toInt(), trScaleKyField.text.toInt()))
            redrawShapes()
        }

        // check edge intersection
        val checkEdgeIntersectionButton = Button("Точки пересечения ребер")
        checkEdgeIntersectionButton.setOnAction {
            val points = checkEdgesIntersection(curEdges)
            for (point in points)
                mainGc.strokeOval(
                    point.x - 4.0,
                    point.y - 4.0, 4.0, 4.0);
        }

        // check polygon
        val checkPolygonsPane = GridPane()
        val checkPolygonsTitle = Label("Проверка принадлежности")
        val convexCount =    Label("Не выбрана точка")
        val nonconvexCount = Label("")
        val checkPolygonsButton = ToggleButton("Выбрать точку")
        checkPolygonsPane.add(checkPolygonsTitle,  0, 0)
        checkPolygonsPane.add(convexCount,        0, 1)
        checkPolygonsPane.add(nonconvexCount,     0, 2)
        checkPolygonsPane.add(checkPolygonsButton, 0, 3)

        // check edge
        val checkPointEdgePane = GridPane()
        val checkPointEdgeTitle = Label("Отношение к ребру")
        val pointEdgeStatus = Label("Не выбрана точка")
        val checkPointEdgeButton = ToggleButton("Выбрать точку")
        checkPointEdgePane.add(checkPointEdgeTitle,  0, 0)
        checkPointEdgePane.add(pointEdgeStatus,      0, 1)
        checkPointEdgePane.add(checkPointEdgeButton, 0, 2)

        transformationPane.children.addAll(
                trMovePane,
                trRotatePane,
                trScalePane,
                checkEdgeIntersectionButton,
                checkPolygonsPane,
                checkPointEdgePane
        )


        val toggleButtons = arrayOf(
                drawPointButton,
                drawLineButton,
                drawRectButton,
                drawPolygonButton,

                trRotatePointButton,
                trScalePointButton,

                checkPolygonsButton,
                checkPointEdgeButton
        )
        setModeButton(drawPointButton,   Mode.DRAW_POINT, toggleButtons)
        setModeButton(drawLineButton,    Mode.DRAW_LINE,  toggleButtons)
        setModeButton(drawRectButton,    Mode.DRAW_RECT,  toggleButtons)
        setModeButton(drawPolygonButton, Mode.DRAW_POLY,  toggleButtons)

        setModeButton(trRotatePointButton,  Mode.SELECT_ROTATION_POINT, toggleButtons)
        setModeButton(trRotatePointButton,  Mode.SELECT_SCALE_POINT,    toggleButtons)
        setModeButton(checkPolygonsButton,  Mode.CHECK_POLYGONS,        toggleButtons)
        setModeButton(checkPointEdgeButton, Mode.CHECK_EDGE,            toggleButtons)

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
                        curPoints.clear()
                        for (shape in curShapes)
                            shape.update(turnAroundPoint(shape.points, curPoint, trAngleField.text.toDouble()))
                        redrawShapes()
                    }
                    Mode.SELECT_SCALE_POINT -> {
                        curPoints.clear()
                        for (shape in curShapes)
                            shape.update(scaleAroundPoint(shape.points, curPoint,
                                    trScaleKxField.text.toInt(), trScaleKyField.text.toInt()))
                        redrawShapes()
                    }

                    Mode.CHECK_POLYGONS -> {
                        curPoints.clear()
                        val result = checkPolygons(curShapes, curPoint)
                        convexCount.text =    "Выпуклых:   ${result.convex_count}"
                        nonconvexCount.text = "Невыпуклых: ${result.nonconvex_count}"
                    }
                    Mode.CHECK_EDGE -> {
                        curPoints.clear()
                        if (curEdges.count() != 0) {
                            val result = checkPointEdge(curEdges.firstElement(), curPoint)
                            pointEdgeStatus.text = when(result) {
                                Position.Left -> "Слева от отрезка"
                                Position.Belongs -> "Принадлежит отрезку"
                                Position.Right -> "Справа от отрезка"
                            }
                        }
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
        if (curPoints.count() == 2)
            curEdges.add(shape)
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
