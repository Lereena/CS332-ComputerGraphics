@file:Suppress("NAME_SHADOWING")

package lab6

import ind1bostan.DoublePoint
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import lab4.Position
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.collections.ArrayList
import kotlin.math.*

enum class Projection { PERSPECTIVE, ORTHOGRAPHIC }

class Affine3D : Application() {
    private var axesModel = Polyhedron("assets/3dmodels/axes.obj")
    private var currentModel = Polyhedron("assets/3dmodels/hexahedron.obj")
    private val mainCanvas = Canvas(800.0, 600.0)
    private val mainGc = mainCanvas.graphicsContext2D

    private val camera = Camera(
        Point3D(0.0, 0.0, 300.0),
        0.0, 0.0,
        mainCanvas
    )

    override fun start(primaryStage: Stage) {
        val mainGroup = GridPane()
        mainGroup.alignment = Pos.BASELINE_CENTER
        mainGroup.hgap = 10.0
        mainGroup.vgap = 10.0

        mainGc.stroke = Color.BLACK

        val canvasGroup = StackPane(mainCanvas)
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
            redraw()
        }

        val perModeButton = ToggleButton("Perspective")
        val ortModeButton = ToggleButton("Orthographic")
        perModeButton.isSelected = true

        shapePane.children.addAll(
            fileList,
            perModeButton,
            ortModeButton
        )

        val toggleButtons = arrayOf(
            perModeButton,
            ortModeButton,
        )
        setModeButton(perModeButton, Projection.PERSPECTIVE, toggleButtons)
        setModeButton(ortModeButton, Projection.ORTHOGRAPHIC, toggleButtons)

        // TRANSFORMATION PANE
        // move pane
        val trMoveSection = InterfaceSection("Смещение")
        with(trMoveSection) {
            val dXInput = addInput("dX", "0.0")
            val dYInput = addInput("dY", "0.0")
            val dZInput = addInput("dZ", "0.0")
            addButton("Сдвинуть", EventHandler {
                move(
                    currentModel,
                    dXInput.text.toDouble(),
                    dYInput.text.toDouble(),
                    dZInput.text.toDouble(),
                )
                redraw()
            })
        }

        // Camera pane
        val cameraModeSection = FlowPane(Orientation.VERTICAL, 0.0, 5.0)
        val removeFacesBox = CheckBox("Удалить грани")
        removeFacesBox.setOnAction {
            camera.removeFaces = !camera.removeFaces
            redraw()
        }
        val cameraModeGroup = ToggleGroup()
        val byEdgesButton = RadioButton("По ребрам")
        byEdgesButton.toggleGroup = cameraModeGroup;
        byEdgesButton.isSelected = true
        byEdgesButton.setOnAction {
            camera.rasterMode = RasterModes.BY_EDGES
            redraw()
        }
        val zBufferButton = RadioButton("Z-буффер")
        zBufferButton.toggleGroup = cameraModeGroup;
        zBufferButton.setOnAction {
            camera.rasterMode = RasterModes.Z_BUFFER
            redraw()
        }
        val shaderButton = RadioButton("Тени")
        shaderButton.toggleGroup = cameraModeGroup;
        shaderButton.setOnAction {
            camera.rasterMode = RasterModes.SHADER
            redraw()
        }
        val textureButton = RadioButton("Текстура")
        textureButton.toggleGroup = cameraModeGroup;
        textureButton.setOnAction {
            camera.rasterMode = RasterModes.TEXTURE
            redraw()
        }
        val floatHorButton = RadioButton("Плав. гор-т")
        floatHorButton.toggleGroup = cameraModeGroup;
        floatHorButton.setOnAction {
            camera.rasterMode = RasterModes.FLOAT_HOR
            redraw()
        }
        cameraModeSection.children.addAll(
            removeFacesBox,
            byEdgesButton, zBufferButton,
            textureButton, floatHorButton
        )

        // scale pane
        val trScaleSection = InterfaceSection("Масштабирование")
        with(trScaleSection) {
            val kXInput = addInput("kX", "2.0")
            val kYInput = addInput("kY", "2.0")
            val kZInput = addInput("kZ", "2.0")
            addButton("Масштабировать", EventHandler {
                scale(
                    currentModel,
                    kXInput.text.toDouble(),
                    kYInput.text.toDouble(),
                    kZInput.text.toDouble()
                )
                redraw()
            })
        }

        // rotate pane
        val trRotateSection = InterfaceSection("Поворот")
        with(trRotateSection) {
            val angleInput = addInput("Угол", "0.2")
            val axesList = addComboBox(arrayOf(Axis.X, Axis.Y, Axis.Z), Axis.X)

            addButton("Параллельно оси", EventHandler {
                val cP = currentModel.centerPoint
                val line = when (axesList.value) {
                    Axis.X -> Line(Point3D(cP.x - 50.0, cP.y, cP.z), Point3D(cP.x + 50.0, cP.y, cP.z))
                    Axis.Y -> Line(Point3D(cP.x, cP.y - 50.0, cP.z), Point3D(cP.x, cP.y + 50.0, cP.z))
                    Axis.Z -> Line(Point3D(cP.x, cP.y, cP.z - 50.0), Point3D(cP.x, cP.y, cP.z + 50.0))
                }
                rotateAroundLine(currentModel, line, angleInput.text.toDouble())
                redraw()
            })

            addLabel("Координаты точек прямой")
            val p1XInput = addInput("aX", "0.0")
            val p1YInput = addInput("aY", "0.0")
            val p1ZInput = addInput("aZ", "0.0")
            val p2XInput = addInput("aX", "0.0")
            val p2YInput = addInput("aY", "0.0")
            val p2ZInput = addInput("aZ", "0.0")
            addButton("Вокруг прямой", EventHandler {
                val point1 = Point3D(
                    p1XInput.text.toDouble(),
                    p1YInput.text.toDouble(),
                    p1ZInput.text.toDouble()
                )
                val point2 = Point3D(
                    p2XInput.text.toDouble(),
                    p2YInput.text.toDouble(),
                    p2ZInput.text.toDouble()
                )
                val line = Line(point1, point2)
                rotateAroundLine(currentModel, line, angleInput.text.toDouble())
                redraw()
            })
        }

        // reflect pane
        val trReflectSection = InterfaceSection("Отражение")
        with(trReflectSection) {
            val axesItems = FXCollections.observableArrayList("XY", "YZ", "XZ")
            val axesList = ComboBox(axesItems)
            axesList.value = "XY"
            addComboBox(axesList)
            addButton("Отразить", EventHandler {
                val reflectAxis = when (axesList.value) {
                    "XY" -> Axis.Z
                    "YZ" -> Axis.X
                    "XZ" -> Axis.Y
                    else -> Axis.X
                }
                reflect(currentModel, reflectAxis)
                redraw()
            })
        }

        // plot pane
        val funcPlotSection = InterfaceSection("Построение графика")
        with(funcPlotSection) {
            val x0Field = addInput("X0", "-1.0")
            val y0Field = addInput("Y0", "-1.0")
            val x1Field = addInput("X1", "1.0")
            val y1Field = addInput("Y1", "1.0")
            val stepField = addInput("Шаг", "0.01")
            val funcItems = FXCollections.observableArrayList(
                "sin(x + y)",
                "sin(x + y) / (x + y)",
                "x + y^2",
                "sin(5x) * cos(5y) / 5",
                "sin(10(x^2 + y^2)) / 10"
            )
            val funcList = ComboBox(funcItems)
            addComboBox(funcList)
            val functions = arrayListOf<(Double, Double) -> Double>(
                { x, y -> sin(x + y) },
                { x, y -> sin(x + y) / (x + y) },
                { x, y -> x + y.pow(2) },
                { x, y -> sin(5 * x) * cos(5 * y) / 5.0 },
                { x, y -> sin(10 * (x.pow(2) + y.pow(2))) / 10 }
            )
            addButton("Построить график", EventHandler {
                val x0 = x0Field.text.toDouble()
                val y0 = y0Field.text.toDouble()
                val x1 = x1Field.text.toDouble()
                val y1 = y1Field.text.toDouble()
                val step = stepField.text.toDouble()
                val func = funcItems.indexOf(funcList.value)
                currentModel = floatingPlot(x0, y0, x1, y1, step, functions[func])
                redraw()
            })
        }

        // Rotation shape
        val crRotationShapeSection = InterfaceSection("Фигура вращения")
        with(crRotationShapeSection) {
            addLabel("Координаты точек")
            val xInput = addInput("X", "0.0")
            val yInput = addInput("Y", "0.0")
            val zInput = addInput("Z", "0.0")

            val generatrixPoints = ArrayList<Point3D>()
            addButton("Добавить точку", EventHandler {
                val point = Point3D(
                    xInput.text.toDouble(),
                    yInput.text.toDouble(),
                    zInput.text.toDouble()
                )
                generatrixPoints.add(point)
//                currentModel = pointsToPolyhedron(generatrixPoints)
                redraw()
            })

            val stepsInput = addInput("Разбиения", "5")
            val axesList = addComboBox(arrayOf(Axis.X, Axis.Y, Axis.Z), Axis.Y)

            addButton("Создать фигуру", EventHandler {
                currentModel = rotationShape(
                    generatrixPoints,
                    stepsInput.text.toInt(), axesList.value
                )
                redraw()
                generatrixPoints.clear()
            })
        }

        val saveSection = InterfaceSection("Сохранение")
        with(saveSection) {
            val nameInput = addInput("Имя:", "default")
            addButton("Сохранить", EventHandler {
                saveModel(currentModel, nameInput.text.toString())
            })
        }

        transformationPane.children.addAll(
            saveSection.sectionPane,
            trMoveSection.sectionPane,
            cameraModeSection,
            trScaleSection.sectionPane,
            trReflectSection.sectionPane,
            trRotateSection.sectionPane,
            funcPlotSection.sectionPane,
            crRotationShapeSection.sectionPane
        )

        primaryStage.title = "Affine transformations 3D"

        primaryStage.scene = Scene(mainGroup)

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED) { e ->
            when (e.text) {
                "w" -> camera.changePosition(0.0, 2.0)
                "a" -> camera.changePosition(-2.0, 0.0)
                "s" -> camera.changePosition(0.0, -2.0)
                "d" -> camera.changePosition(2.0, 0.0)
                "i" -> camera.changeAngleX(0.1)
                "k" -> camera.changeAngleX(-0.1)
                "l" -> camera.changeAngleY(0.1)
                "j" -> camera.changeAngleY(-0.1)
            }
            redraw()
        }
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Affine3D::class.java, *args)
        }
    }

    fun redraw() {
        mainGc.clearRect(0.0, 0.0, 1000.0, 1000.0)
        mainGc.beginPath()
//        camera.draw(axesModel)
        camera.draw(currentModel)
    }

    private fun setModeButton(button: ToggleButton, mode: Projection, all_buttons: Array<ToggleButton>) {
        button.setOnAction {
            val state = button.isSelected
            if (state) {
                for (button in all_buttons)
                    button.isSelected = false
                camera.projectionMode = mode
                redraw()
            }
            button.isSelected = true
        }
    }
}

fun saveModel(model: Polyhedron, fileName: String) {
//    val writer = File("assets/3dmodels/$fileName.obj").bufferedWriter()
//
//    // добавляем вершины
//    for (vertice in model.vertices) {
//        writer.write("v ${vertice.point.x} ${vertice.point.y} ${vertice.point.z}\n")
//    }
//
//    // добавляем нормали
//    for (polygon in model.polygons) {
//        val normal = findNormal(
//            polygon.textureVertices[0].first.point, polygon.textureVertices[1].first.point,
//            polygon.textureVertices[2].first.point
//        )
//        writer.write("vn ${normal.l} ${normal.m} ${normal.n}\n")
//    }
//
//    // добавляем поверхности
//    var faceIndex = 1
//    for (polygon in model.polygons) {
//        writer.write("f")
//        for (point in polygon.textureVertices) {
//            for (i in model.vertices.indices) {
//                if (point.first == model.vertices[i])
//                    writer.write(" ${i + 1}//${faceIndex}")
//            }
//        }
//        writer.write("\n")
//        faceIndex++
//    }
//    writer.close()
}

fun findNormal(p0: Point3D, p1: Point3D, p2: Point3D): DirectionVector {
    val A = (p1.y - p0.y) * (p2.z - p0.z) - (p1.z - p0.z) * (p2.y - p0.y)
    val B = (p1.z - p0.z) * (p2.x - p0.x) - (p1.x - p0.x) * (p2.z - p0.z)
    val C = (p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x)
    return DirectionVector(A, B, C)
}


fun findDepth(x: Int, y: Int, A: Double, B: Double, C: Double, F: Double): Double {
    return ((A * x) + (B * y) + F) / C
}

fun classifyPoint(p: Point3D, line: Line): Position {
    val p1 = line.point1
    val p2 = line.point2
    val a = DoublePoint(p2.x - p1.x, p2.y - p1.y)
    val b = DoublePoint(p.x - p1.x, p.y - p1.y)
    val sa = a.x * b.y - b.x * a.y;

    if (sa > 0.0)
        return Position.Right
    if (sa < 0.0)
        return Position.Left
    return Position.Belongs
}
