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
import lab4.Mode
import lab4.scaleAroundCenter
import lab4.turnAroundCenter
import java.nio.file.Files
import java.nio.file.Paths

enum class Projection { ORTHOGRAPHIC, PERSPECTIVE }

class Affine3D : Application() {
    var currentModel = Polyhedron("assets/3dmodels/hexahedron.obj")
    var currentProjectionMode = Projection.ORTHOGRAPHIC
    var currentAxis = Axis.Z

    override fun start(primaryStage: Stage) {
        val mainGroup = GridPane()
        mainGroup.alignment = Pos.BASELINE_CENTER
        mainGroup.hgap = 10.0;
        mainGroup.vgap =10.0;

        val mainCanvas = Canvas(800.0, 600.0)
        val mainGc = mainCanvas.graphicsContext2D

        val canvasGroup = StackPane(mainCanvas)
        canvasGroup.border = Border(BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT))

        val shapePane = FlowPane(Orientation.HORIZONTAL, 10.0, 0.0)
        val transformationPane = FlowPane(Orientation.VERTICAL, 0.0, 30.0)

        mainGroup.add(canvasGroup,        0, 0)
        mainGroup.add(shapePane,          0, 1)
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
            redraw(mainCanvas, mainGc)
        }

        val axesItems = FXCollections.observableArrayList("XY", "XZ", "YZ")
        val axesList = ComboBox(axesItems)
        axesList.setOnAction {
            when (axesList.value) {
                "XY" -> { currentAxis = Axis.Z; }
                "XZ" -> { currentAxis = Axis.Y; }
                "YZ" -> { currentAxis = Axis.X; }
            }
            redraw(mainCanvas, mainGc)
        }
        axesList.value = "XY"

        val ortModeButton = ToggleButton("Orthographic")
        ortModeButton.isSelected = true
        val perModeButton =  ToggleButton("Perspective")

        shapePane.children.addAll(fileList, axesList, ortModeButton, perModeButton)
        ortModeButton.setOnAction {
            val state = ortModeButton.isSelected
            if (state) {
                perModeButton.isSelected = false
                currentProjectionMode = Projection.ORTHOGRAPHIC
                redraw(mainCanvas, mainGc)
            }
            else
                ortModeButton.isSelected = true
        }

        perModeButton.setOnAction {
            val state = perModeButton.isSelected
            if (state) {
                ortModeButton.isSelected = false
                currentProjectionMode = Projection.PERSPECTIVE
                redraw(mainCanvas, mainGc)
            }
            else
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
        trMovePane.add(trMoveTitle,  0, 0, 2, 1)
        trMovePane.add(trDxLabel,    0, 1)
        trMovePane.add(trDxField,    1, 1)
        trMovePane.add(trDyLabel,    0, 2)
        trMovePane.add(trDyField,    1, 2)
        trMovePane.add(trDzLabel,    0, 3)
        trMovePane.add(trDzField,    1, 3)
        trMovePane.add(trMoveButton, 0, 4, 2, 1)
        trMoveButton.setOnAction {
            move(currentModel,
                    trDxField.text.toDouble(),
                    trDyField.text.toDouble(),
                    trDzField.text.toDouble())
            redraw(mainCanvas, mainGc)
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
        val trScaleCenterButton =  Button("Масштабировать")
        trScalePane.add(trScaleTitle,   0, 0, 2, 1)
        trScalePane.add(trScaleKxLabel, 0, 1)
        trScalePane.add(trScaleKxField, 1, 1)
        trScalePane.add(trScaleKyLabel, 0, 2)
        trScalePane.add(trScaleKyField, 1, 2)
        trScalePane.add(trScaleKzLabel, 0, 3)
        trScalePane.add(trScaleKzField, 1, 3)
        trScalePane.add(trScaleCenterButton, 0, 4, 2, 1)

        trScaleCenterButton.setOnAction {
            scale(currentModel,
                    trScaleKxField.text.toDouble(),
                    trScaleKyField.text.toDouble(),
                    trScaleKzField.text.toDouble())
            redraw(mainCanvas, mainGc)
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
        trRotatePane.add(trAngleLabel,  0, 1)
        trRotatePane.add(trAngleField,  1, 1)
        trRotatePane.add(axesRotList,   0, 3, 2, 1)
        trRotatePane.add(trRotateCenterButton,   0, 4, 2, 1)

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
            redraw(mainCanvas, mainGc)
        }

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
                    trRotateP1ZField.text.toDouble())
            val point2 = Point3D(
                    trRotateP2XField.text.toDouble(),
                    trRotateP2YField.text.toDouble(),
                    trRotateP2ZField.text.toDouble())
            val line = Line(point1, point2)
            rotateAroundLine(currentModel, line, trAngleField.text.toDouble())
            redraw(mainCanvas, mainGc)
            val tempLine = getLinePolyhedron(line)
            if (currentProjectionMode == Projection.ORTHOGRAPHIC)
                orthographic_projection(mainCanvas, mainGc, tempLine, currentAxis)
            else
                perspective_projection(mainCanvas, mainGc, tempLine, currentAxis)
        }

        transformationPane.children.addAll(trMovePane,
                trScalePane,
                trRotatePane,
                trRotateLinePane)

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

    fun redraw(canvas: Canvas, gc: GraphicsContext) {
        gc.clearRect(0.0, 0.0, 1000.0, 1000.0)
        gc.beginPath()
        if (currentProjectionMode == Projection.ORTHOGRAPHIC)
            orthographic_projection(canvas, gc, currentModel, currentAxis)
        else
            perspective_projection(canvas, gc, currentModel, currentAxis)
    }
}

fun orthographic_projection(canvas: Canvas, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    gc.moveTo(canvas.width / 2, 0.0)
    gc.lineTo(canvas.width / 2, canvas.height)
    gc.stroke()
    gc.moveTo(0.0, canvas.height / 2)
    gc.lineTo(canvas.width, canvas.height / 2)
    gc.stroke()
    for (polygon in model.polygons) {
        for (i in polygon.points.indices) {
            when (ax) {
                Axis.Z -> {
                    gc.moveTo(polygon[i].x + canvas.width / 2, polygon[i].y * (-1) + canvas.height / 2)
                    gc.lineTo(polygon[i + 1].x + canvas.width / 2, polygon[i + 1].y * (-1) + canvas.height / 2)
                }
                Axis.X -> {
                    gc.moveTo(polygon[i].z + canvas.width / 2, polygon[i].y * (-1) + canvas.height / 2)
                    gc.lineTo(polygon[i + 1].z + canvas.width / 2, polygon[i + 1].y * (-1) + canvas.height / 2)
                }
                Axis.Y -> {
                    gc.moveTo(polygon[i].x + canvas.width / 2, polygon[i].z * (-1) + canvas.height / 2)
                    gc.lineTo(polygon[i + 1].x + canvas.width / 2, polygon[i + 1].z * (-1) + canvas.height / 2)
                }
            }
        }
    }
    gc.stroke()
}


fun perspective_projection(canvas: Canvas, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    gc.moveTo(canvas.width / 2, 0.0)
    gc.lineTo(canvas.width / 2, canvas.height)
    gc.stroke()
    gc.moveTo(0.0, canvas.height / 2)
    gc.lineTo(canvas.width, canvas.height / 2)
    gc.stroke()
    for (polygon in model.polygons) {
        for (i in polygon.points.indices) {
            when (ax) {
                Axis.Z -> {
                    var c = 150
                    gc.moveTo((polygon[i].x + (-1 / c)) *  (1 - (polygon[i].z + (-1 / c)) / c)  + canvas.width / 2 ,
                            ((polygon[i].y + (-1 / c)) * (1 - (polygon[i].z + (-1 / c)) / c) * (-1 ) + canvas.height / 2))
                    gc.lineTo(((polygon[i + 1].x + (-1 / c)) * (1 - (polygon[i + 1].z + (-1 / c)) / c) + canvas.width / 2),
                            ((polygon[i + 1].y + (-1 / c)) * (1 - (polygon[i + 1].z + (-1 / c)) / c) * (-1) + canvas.height / 2))
                }
                Axis.X -> {
                    var c = 150
                    gc.moveTo((polygon[i].z + (-1 / c)) *  (1 - (polygon[i].x + (-1 / c)) / c)  + canvas.width / 2 ,
                            ((polygon[i].y + (-1 / c)) * (1 - (polygon[i].x + (-1 / c)) / c) * (-1 ) + canvas.height / 2))
                    gc.lineTo(((polygon[i + 1].z + (-1 / c)) * (1 - (polygon[i + 1].x + (-1 / c)) / c) + canvas.width / 2),
                            ((polygon[i + 1].y + (-1 / c)) * (1 - (polygon[i + 1].x + (-1 / c)) / c) * (-1) + canvas.height / 2))
                }
                Axis.Y -> {
                    var c = 150
                    gc.moveTo((polygon[i].x + (-1 / c)) *  (1 - (polygon[i].y + (-1 / c)) / c)  + canvas.width / 2 ,
                            ((polygon[i].z + (-1 / c)) * (1 - (polygon[i].y + (-1 / c)) / c) * (-1 ) + canvas.height / 2))
                    gc.lineTo(((polygon[i + 1].x + (-1 / c)) * (1 - (polygon[i + 1].y + (-1 / c)) / c) + canvas.width / 2),
                            ((polygon[i + 1].z + (-1 / c)) * (1 - (polygon[i + 1].y + (-1 / c)) / c) * (-1) + canvas.height / 2))
                }
            }
        }
    }
    gc.stroke()
}

