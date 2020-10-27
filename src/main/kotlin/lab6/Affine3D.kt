package lab6

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths

enum class Projection { ORTHOGRAPHIC, PERSPECTIVE, AXONOMETRIC }

class Affine3D : Application() {
    var currentModel = Polyhedron("assets/3dmodels/hexahedron.obj")
    var currentProjectionMode = Projection.ORTHOGRAPHIC
    var currentAxis = Axis.Z

    override fun start(primaryStage: Stage) {
        val mainGroup = GridPane()
        mainGroup.alignment = Pos.BASELINE_CENTER
        mainGroup.hgap = 10.0;
        mainGroup.vgap = 10.0;

        val mainCanvas = Canvas(800.0, 600.0)
        val mainCanvasA = Canvas(800.0, 600.0)
        val mainGc = mainCanvas.graphicsContext2D
        val mainGcA = mainCanvasA.graphicsContext2D
        mainGcA.stroke = Color.RED
        mainGc.stroke = Color.BLACK

        val canvasGroup = StackPane(mainCanvas, mainCanvasA)
        canvasGroup.border = Border(
            BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT
            )
        )

        val shapePane = FlowPane(Orientation.HORIZONTAL, 10.0, 0.0)
        val transformationPane = FlowPane(Orientation.VERTICAL, 0.0, 30.0)

        mainGroup.add(canvasGroup, 0, 0)
        mainGroup.add(shapePane, 0, 1)
        mainGroup.add(transformationPane, 1, 0)

        // SHAPE PANE
        val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
        val resourcesPath = Paths.get(projectDirAbsolutePath, "/assets/3dmodels")
        val fileNames = Files.walk(resourcesPath).map { it.fileName.toString() }.skip(1).toArray()
        val fileItems = FXCollections.observableArrayList(*fileNames)

        val fileList = ComboBox(fileItems)
        fileList.setOnAction {
            val shapePath = "assets/3dmodels/" + fileList.value
            currentModel = Polyhedron(shapePath)
            redraw(mainCanvas, mainGcA, mainGc)
        }

        val axesItems = FXCollections.observableArrayList("XY", "XZ", "YZ")
        val axesList = ComboBox(axesItems)
        axesList.setOnAction {
            when (axesList.value) {
                "XY" -> {
                    currentAxis = Axis.Z; }
                "XZ" -> {
                    currentAxis = Axis.Y; }
                "YZ" -> {
                    currentAxis = Axis.X; }
            }
            redraw(mainCanvas, mainGcA, mainGc)
        }
        axesList.value = "XY"

        val ortModeButton = ToggleButton("Orthographic")
        ortModeButton.isSelected = true
        val perModeButton = ToggleButton("Perspective")
        val axModeButton = ToggleButton("Axonometric")

        shapePane.children.addAll(fileList, axesList, ortModeButton, perModeButton, axModeButton)
        ortModeButton.setOnAction {
            val state = ortModeButton.isSelected
            if (state) {
                perModeButton.isSelected = false
                axModeButton.isSelected = false
                currentProjectionMode = Projection.ORTHOGRAPHIC
                redraw(mainCanvas, mainGcA, mainGc)
            } else
                ortModeButton.isSelected = true
        }

        perModeButton.setOnAction {
            val state = perModeButton.isSelected
            if (state) {
                ortModeButton.isSelected = false
                axModeButton.isSelected = false
                currentProjectionMode = Projection.PERSPECTIVE
                redraw(mainCanvas, mainGcA, mainGc)
            } else
                perModeButton.isSelected = true
        }

        axModeButton.setOnAction {
            val state = axModeButton.isSelected
            if (state) {
                ortModeButton.isSelected = false
                perModeButton.isSelected = false
                currentProjectionMode = Projection.AXONOMETRIC
                redraw(mainCanvas, mainGcA, mainGc)
            } else
                perModeButton.isSelected = true
        }

        // TRANSFORMATION PANE
        // move pane
        val trMovePane = GridPane()
        val trMoveTitle = Label("Смещение")
        val trDxField = TextField("0.0")
        val trDyField = TextField("0.0")
        val trDzField = TextField("0.0")
        val trDxLabel = Label("dx: ")
        val trDyLabel = Label("dy: ")
        val trDzLabel = Label("dz: ")
        val trMoveButton = Button("Сдвинуть")
        trMovePane.add(trMoveTitle, 0, 0, 2, 1)
        trMovePane.add(trDxLabel, 0, 1)
        trMovePane.add(trDxField, 1, 1)
        trMovePane.add(trDyLabel, 0, 2)
        trMovePane.add(trDyField, 1, 2)
        trMovePane.add(trDzLabel, 0, 3)
        trMovePane.add(trDzField, 1, 3)
        trMovePane.add(trMoveButton, 0, 4, 2, 1)
        trMoveButton.setOnAction {
            move(
                currentModel,
                trDxField.text.toDouble(),
                trDyField.text.toDouble(),
                trDzField.text.toDouble()
            )
            redraw(mainCanvas, mainGcA, mainGc)
        }

        // scale pane
        val trScalePane = GridPane()
        val trScaleTitle = Label("Масштабирование")
        val trScaleKxField = TextField("1.0")
        val trScaleKyField = TextField("1.0")
        val trScaleKzField = TextField("1.0")
        val trScaleKxLabel = Label("kx: ")
        val trScaleKyLabel = Label("ky: ")
        val trScaleKzLabel = Label("kz: ")
        val trScaleCenterButton = Button("Масштабировать")
        trScalePane.add(trScaleTitle, 0, 0, 2, 1)
        trScalePane.add(trScaleKxLabel, 0, 1)
        trScalePane.add(trScaleKxField, 1, 1)
        trScalePane.add(trScaleKyLabel, 0, 2)
        trScalePane.add(trScaleKyField, 1, 2)
        trScalePane.add(trScaleKzLabel, 0, 3)
        trScalePane.add(trScaleKzField, 1, 3)
        trScalePane.add(trScaleCenterButton, 0, 4, 2, 1)

        trScaleCenterButton.setOnAction {
            scale(
                currentModel,
                trScaleKxField.text.toDouble(),
                trScaleKyField.text.toDouble(),
                trScaleKzField.text.toDouble()
            )
            redraw(mainCanvas, mainGcA, mainGc)
        }


        // rotate pane
        val trRotatePane = GridPane()
        val trRotateTitle = Label("Поворот")
        val trAngleField = TextField("0.0")
        val trAngleLabel = Label("Угол: ")

        var curRotateAxis = Axis.X
        val axesRotItems = FXCollections.observableArrayList("X", "Y", "Z")
        val axesRotList = ComboBox(axesRotItems)
        axesRotList.value = "X"
        val trRotateCenterButton = Button("Параллельно оси")

        trRotatePane.add(trRotateTitle, 0, 0, 2, 1)
        trRotatePane.add(trAngleLabel, 0, 1)
        trRotatePane.add(trAngleField, 1, 1)
        trRotatePane.add(axesRotList, 0, 3, 2, 1)
        trRotatePane.add(trRotateCenterButton, 0, 4, 2, 1)

        axesRotList.setOnAction {
            curRotateAxis = when (axesRotList.value) {
                "X" -> Axis.X
                "Y" -> Axis.Y
                "Z" -> Axis.Z
                else -> Axis.X
            }
        }

        trRotateCenterButton.setOnAction {
            rotateAroundCenter(currentModel, curRotateAxis, trAngleField.text.toDouble())
            redraw(mainCanvas, mainGcA, mainGc)
        }

        // reflext pane
        val trReflectPane = GridPane()
        val trReflectTitle = Label("Отражение")

        var curReflAxis = Axis.Z
        val axesReflItems = FXCollections.observableArrayList("XY", "YZ", "XZ")
        val axesReflList = ComboBox(axesReflItems)
        axesReflList.value = "XY"
        val trReflectButton = Button("Отразить")

        trReflectPane.add(trReflectTitle, 0, 0, 2, 1)
        trReflectPane.add(axesReflList,   0, 1)
        trReflectPane.add(trReflectButton, 0, 2, 2, 1)

        axesReflList.setOnAction {
            curReflAxis = when (axesReflList.value) {
                "XY" -> Axis.Z
                "YZ" -> Axis.X
                "XZ" -> Axis.Y
                else -> Axis.X
            }
        }

        trReflectButton.setOnAction {
            reflect(currentModel, curReflAxis)
            redraw(mainCanvas, mainGcA, mainGc)
        }

        // Rotate around line

        val trRotateLinePane = GridPane()
        val trRotateLineTitle = Label("Координаты точек")
        val trRotateP1XField = TextField("0.0")
        val trRotateP1YField = TextField("0.0")
        val trRotateP1ZField = TextField("0.0")
        val trRotateP2XField = TextField("0.0")
        val trRotateP2YField = TextField("0.0")
        val trRotateP2ZField = TextField("0.0")
        val trRotateP1XLabel = Label("X")
        val trRotateP1YLabel = Label("Y")
        val trRotateP1ZLabel = Label("Z")
        val trRotateP2XLabel = Label("X")
        val trRotateP2YLabel = Label("Y")
        val trRotateP2ZLabel = Label("Z")

        val trRotateLineButton = Button("Вокруг прямой")

        trRotateLinePane.add(trRotateLineTitle, 0, 0, 2, 1)
        trRotateLinePane.add(trRotateP1XLabel, 0, 1)
        trRotateLinePane.add(trRotateP1XField, 1, 1)
        trRotateLinePane.add(trRotateP1YLabel, 0, 2)
        trRotateLinePane.add(trRotateP1YField, 1, 2)
        trRotateLinePane.add(trRotateP1ZLabel, 0, 3)
        trRotateLinePane.add(trRotateP1ZField, 1, 3)

        trRotateLinePane.add(trRotateP2XLabel, 0, 4)
        trRotateLinePane.add(trRotateP2XField, 1, 4)
        trRotateLinePane.add(trRotateP2YLabel, 0, 5)
        trRotateLinePane.add(trRotateP2YField, 1, 5)
        trRotateLinePane.add(trRotateP2ZLabel, 0, 6)
        trRotateLinePane.add(trRotateP2ZField, 1, 6)
        trRotateLinePane.add(trRotateLineButton, 0, 7, 2, 1)

        trRotateLineButton.setOnAction {
            val point1 = Point3D(
                trRotateP1XField.text.toDouble(),
                trRotateP1YField.text.toDouble(),
                trRotateP1ZField.text.toDouble()
            )
            val point2 = Point3D(
                trRotateP2XField.text.toDouble(),
                trRotateP2YField.text.toDouble(),
                trRotateP2ZField.text.toDouble()
            )
            val line = Line(point1, point2)
            rotateAroundLine(currentModel, line, trAngleField.text.toDouble())
            redraw(mainCanvas, mainGcA, mainGc)
            val tempLine = getLinePolyhedron(line)
            when (currentProjectionMode) {
                Projection.ORTHOGRAPHIC -> orthographicProjection(mainCanvas, mainGcA, mainGc, tempLine, currentAxis)
                Projection.PERSPECTIVE -> perspectiveProjection(mainCanvas, mainGcA, mainGc, tempLine, currentAxis)
                Projection.AXONOMETRIC -> axonometricProjection(mainCanvas, mainGcA, mainGc, tempLine, currentAxis)
            }
        }

        val funcPlotPane = GridPane()

        val funcPlotTitle = Label("Построение графика")
        val funcPlotX0Field = TextField("-10.0")
        val funcPlotY0Field = TextField("-10.0")
        val funcPlotX1Field = TextField("10.0")
        val funcPlotY1Field = TextField("10.0")
        val funcPlotX0Label = Label("X0")
        val funcPlotY0Label = Label("Y0")
        val funcPlotX1Label = Label("X1")
        val funcPlotY1Label = Label("Y1")
        val funcPlotStepField = TextField("0.1")
        val funcPlotStepLabel = Label("Шаг")

        val funcPlotFunctionsItems = FXCollections.observableArrayList(
            "sin(x + y)",
            "sin(x + y) / (x + y)")
        val funcPlotFunctionsList = ComboBox(funcPlotFunctionsItems)

        val funcPlotButton = Button("Нарисовать график")

        funcPlotPane.add(funcPlotTitle, 0, 0, 2, 1)
        funcPlotPane.add(funcPlotX0Label, 0, 1)
        funcPlotPane.add(funcPlotX0Field, 1, 1)
        funcPlotPane.add(funcPlotY0Label, 0, 2)
        funcPlotPane.add(funcPlotY0Field, 1, 2)
        funcPlotPane.add(funcPlotX1Label, 0, 3)
        funcPlotPane.add(funcPlotX1Field, 1, 3)
        funcPlotPane.add(funcPlotY1Label, 0, 4)
        funcPlotPane.add(funcPlotY1Field, 1, 4)
        funcPlotPane.add(funcPlotStepLabel, 0, 5)
        funcPlotPane.add(funcPlotStepField, 1, 5)
        funcPlotPane.add(funcPlotFunctionsList, 0, 6, 2, 1)

        funcPlotPane.add(funcPlotButton, 0, 7, 2, 1)

        funcPlotButton.setOnAction {
            val x0 = funcPlotX0Field.text.toDouble()
            val y0 = funcPlotY0Field.text.toDouble()
            val x1 = funcPlotX1Field.text.toDouble()
            val y1 = funcPlotY1Field.text.toDouble()
            val step = funcPlotStepField.text.toDouble()
            val func = when (funcPlotFunctionsList.value) {
                "sin(x + y)" -> 0
                "sin(x + y) / (x + y)" -> 1
                else -> throw Exception()
            }
            currentModel = plot3D(x0, y0, x1, y1, step, functions[func])
            redraw(mainCanvas, mainGcA, mainGc)
        }

        transformationPane.children.addAll(
            trMovePane,
            trScalePane,
            trReflectPane,
            trRotatePane,
            trRotateLinePane,
            funcPlotPane
        )

        primaryStage.title = "Affine transformations 3D"

        primaryStage.scene = Scene(mainGroup)
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Affine3D::class.java, *args)
        }
    }

    fun redraw(canvas: Canvas, gcA: GraphicsContext, gc: GraphicsContext) {
        gc.clearRect(0.0, 0.0, 1000.0, 1000.0)
        gcA.clearRect(0.0, 0.0, 1000.0, 1000.0)
        gc.beginPath()
        when (currentProjectionMode) {
            Projection.ORTHOGRAPHIC -> orthographicProjection(canvas, gcA, gc, currentModel, currentAxis)
            Projection.PERSPECTIVE -> perspectiveProjection(canvas, gcA, gc, currentModel, currentAxis)
            Projection.AXONOMETRIC -> axonometricProjection(canvas, gcA, gc, currentModel, currentAxis)
        }
    }
}

fun orthographicProjection(canvas: Canvas, gcA: GraphicsContext, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    drawAxes(canvas, gcA, gc, ax)
    for (polygon in model.polygons) {
        for (i in polygon.points.indices) {
            when (ax) {
                Axis.Z -> {
                    gc.moveTo(polygon[i].x + canvas.width / 2, polygon[i].y * (-1) + canvas.height / 2)
                    gc.lineTo(polygon[i + 1].x + canvas.width / 2, polygon[i + 1].y * (-1) + canvas.height / 2)
                }
                Axis.Y -> {
                    gc.moveTo(polygon[i].x + canvas.width / 2, polygon[i].z * (-1) + canvas.height / 2)
                    gc.lineTo(polygon[i + 1].x + canvas.width / 2, polygon[i + 1].z * (-1) + canvas.height / 2)
                }
                Axis.X -> {
                    gc.moveTo(polygon[i].z + canvas.width / 2, polygon[i].y * (-1) + canvas.height / 2)
                    gc.lineTo(polygon[i + 1].z + canvas.width / 2, polygon[i + 1].y * (-1) + canvas.height / 2)
                }
            }
        }
    }
    gc.stroke()
}

fun perspectiveProjection(canvas: Canvas, gcA: GraphicsContext, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    drawAxes(canvas, gcA, gc, ax)
    val matrix = when (ax) {
        Axis.Z -> perspectiveZMatrix(300.0)
        Axis.Y -> perspectiveYMatrix(300.0)
        Axis.X -> perspectiveXMatrix(300.0)
    }
    for (polygon in model.polygons) {
        val projPoints = polygon.points.map { point ->
            multiplePointAndMatrix(point, matrix)
        }
        var prevPoint = projPoints.last()
        for (point in projPoints) {
            when (ax) {
                Axis.Z -> {
                    gc.moveTo(point.x + canvas.width / 2, -point.y + canvas.height / 2)
                    gc.lineTo(prevPoint.x + canvas.width / 2, -prevPoint.y + canvas.height / 2)
                }
                Axis.Y -> {
                    gc.moveTo(point.x + canvas.width / 2, -point.z + canvas.height / 2)
                    gc.lineTo(prevPoint.x + canvas.width / 2, -prevPoint.z + canvas.height / 2)
                }
                Axis.X -> {
                    gc.moveTo(point.z + canvas.width / 2, -point.y + canvas.height / 2)
                    gc.lineTo(prevPoint.z + canvas.width / 2, -prevPoint.y + canvas.height / 2)
                }
            }
            prevPoint = point
        }
    }
    gc.stroke()
}


fun axonometricProjection(canvas: Canvas, gcA: GraphicsContext, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    gcA.clearRect(0.0, 0.0, 800.0, 600.0)
    gcA.beginPath()
    gcA.moveTo(canvas.width / 2, 0.0)
    gcA.lineTo(canvas.width / 2, canvas.height / 2)
    gcA.moveTo(canvas.width / 2, canvas.height / 2)
    gcA.lineTo(0.0, 530.0)
    gcA.moveTo(canvas.width / 2, canvas.height / 2)
    gcA.lineTo(canvas.width, 530.0)
    gcA.stroke()
    gc.strokeText("Y", 410.0, 30.0)
    gc.strokeText("X", 23.0, 570.0)
    gc.strokeText("Z", 770.0, 570.0)
    for (polygon in model.polygons) {
        val projPoints = polygon.points.map { point ->
            multiplePointAndMatrix(point, axonometricMatrix(145.0 * 3.14 / 180, 45.0 * 3.14 / 180))
        }
        var prevPoint = projPoints.last()
        for (point in projPoints) {
            gc.moveTo(point.x + canvas.width / 2, -point.y + canvas.height / 2)
            gc.lineTo(prevPoint.x + canvas.width / 2, -prevPoint.y + canvas.height / 2)
            prevPoint = point
        }
    }
    gc.stroke()
}

fun drawAxes(canvas: Canvas, gcA: GraphicsContext, gc: GraphicsContext, ax: Axis) {
    gcA.beginPath()
    gcA.moveTo(canvas.width / 2, 0.0)
    gcA.lineTo(canvas.width / 2, canvas.height)
    gcA.moveTo(0.0, canvas.height / 2)
    gcA.lineTo(canvas.width, canvas.height / 2)
    gcA.stroke()
    when (ax) {
        Axis.Z -> {
            gc.strokeText("Y", 410.0, 30.0)
            gc.strokeText("X", 770.0, 290.0)
        }
        Axis.Y -> {
            gc.strokeText("Z", 410.0, 30.0)
            gc.strokeText("X", 770.0, 290.0)
        }
        Axis.X -> {
            gc.strokeText("Y", 410.0, 30.0)
            gc.strokeText("Z", 770.0, 290.0)
        }
    }
}
