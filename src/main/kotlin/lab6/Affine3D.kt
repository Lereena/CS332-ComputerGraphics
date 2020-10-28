@file:Suppress("NAME_SHADOWING")

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
import javafx.event.EventHandler
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.PI
import java.io.File as File

enum class Projection { ORTHOGRAPHIC, PERSPECTIVE, AXONOMETRIC }

class Affine3D : Application() {
    private var currentModel = Polyhedron("assets/3dmodels/hexahedron.obj")
    private var currentProjectionMode = Projection.ORTHOGRAPHIC
    private var currentAxis = Axis.Z
    private val mainCanvas = Canvas(800.0, 600.0)
    private val mainCanvasA = Canvas(800.0, 600.0)
    private val mainGc = mainCanvas.graphicsContext2D
    private val mainGcA = mainCanvasA.graphicsContext2D

    override fun start(primaryStage: Stage) {
        val mainGroup = GridPane()
        mainGroup.alignment = Pos.BASELINE_CENTER
        mainGroup.hgap = 10.0
        mainGroup.vgap = 10.0

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
            redraw()
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
            redraw()
        }
        axesList.value = "XY"

        val ortModeButton = ToggleButton("Orthographic")
        val perModeButton = ToggleButton("Perspective")
        val axModeButton = ToggleButton("Axonometric")
        ortModeButton.isSelected = true

        shapePane.children.addAll(fileList,
                axesList, 
                ortModeButton,
                perModeButton,
                axModeButton
        )

        val toggleButtons = arrayOf(
                ortModeButton,
                perModeButton,
                axModeButton,
        )
        setModeButton(ortModeButton, Projection.ORTHOGRAPHIC, toggleButtons)
        setModeButton(perModeButton, Projection.PERSPECTIVE,  toggleButtons)
        setModeButton(axModeButton,  Projection.AXONOMETRIC, toggleButtons)

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

        // scale pane
        val trScaleSection = InterfaceSection("Масштабирование")
        with(trScaleSection) {
            val kXInput = addInput("kX", "1.0")
            val kYInput = addInput("kY", "1.0")
            val kZInput = addInput("kZ", "1.0")
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
            val angleInput = addInput("Угол", "0.0")
            val axesList = addComboBox(arrayOf(Axis.X, Axis.Y, Axis.Z), Axis.X)

            addButton("Параллельно оси", EventHandler {
                val cP = currentModel.centerPoint
                val line = when(axesList.value) {
                    Axis.X -> Line(Point3D(cP.x-50.0, cP.y, cP.z), Point3D(cP.x+50.0, cP.y, cP.z))
                    Axis.Y -> Line(Point3D(cP.x, cP.y-50.0, cP.z), Point3D(cP.x, cP.y+50.0, cP.z))
                    Axis.Z -> Line(Point3D(cP.x, cP.y, cP.z-50.0), Point3D(cP.x, cP.y, cP.z+50.0))
                }
                rotateAroundLine(currentModel, line, angleInput.text.toDouble())
                redraw()
                val tempLine = getLinePolyhedron(line)
                draw(tempLine)
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
                val tempLine = getLinePolyhedron(line)
                draw(tempLine)
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
        with (funcPlotSection) {
            val x0Field = addInput("X0", "-10.0")
            val y0Field = addInput("Y0", "-10.0")
            val x1Field = addInput("X1", "10.0")
            val y1Field = addInput("Y1", "10.0")
            val stepField = addInput("Шаг", "0.1")
            val funcItems = FXCollections.observableArrayList(
                    "sin(x + y)",
                    "sin(x + y) / (x + y)",
                    "x + y^2",
            )
            val funcList = ComboBox(funcItems)
            addComboBox(funcList)
            val functions = arrayListOf<(Double, Double) -> Double>(
                    { x, y -> sin(x + y) },
                    { x, y -> sin(x + y) / (x + y) },
                    { x, y -> x + y.pow(2) },
            )
            addButton("Построить график", EventHandler {
                val x0 = x0Field.text.toDouble()
                val y0 = y0Field.text.toDouble()
                val x1 = x1Field.text.toDouble()
                val y1 = y1Field.text.toDouble()
                val step = stepField.text.toDouble()
                val func = funcItems.indexOf(funcList.value)
                currentModel = plot3D(x0, y0, x1, y1, step, functions[func])
                redraw()
            })
        }

        // Rotation shape
        val crRotationShapeSection = InterfaceSection("Фигура вращения")
        with (crRotationShapeSection) {
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
                currentModel = pointsToPolyhedron(generatrixPoints)
                redraw()
            })

            val stepsInput = addInput("Разбиения", "5")
            val axesList = addComboBox(arrayOf(Axis.X, Axis.Y, Axis.Z), Axis.Y)

            addButton("Создать фигуру", EventHandler {
                currentModel = rotationShape(generatrixPoints,
                        stepsInput.text.toInt(), axesList.value)
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
                trMoveSection.sectionPane,
                trScaleSection.sectionPane,
                trReflectSection.sectionPane,
                trRotateSection.sectionPane,
                funcPlotSection.sectionPane,
                crRotationShapeSection.sectionPane,
                saveSection.sectionPane
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

    fun redraw() {
        mainGc.clearRect(0.0, 0.0, 1000.0, 1000.0)
        mainGcA.clearRect(0.0, 0.0, 1000.0, 1000.0)
        mainGc.beginPath()
        draw(currentModel)
    }

    private fun draw(model: Polyhedron) {
        when (currentProjectionMode) {
            Projection.ORTHOGRAPHIC -> orthographicProjection(mainCanvas, mainGcA, mainGc, model, currentAxis)
            Projection.PERSPECTIVE -> perspectiveProjection(mainCanvas, mainGcA, mainGc, model, currentAxis)
            Projection.AXONOMETRIC -> axonometricProjection(mainCanvas, mainGcA, mainGc, model, currentAxis)
        }
    }

    private fun setModeButton(button: ToggleButton, mode: Projection, all_buttons: Array<ToggleButton>) {
        button.setOnAction {
            val state = button.isSelected
            if (state) {
                for (button in all_buttons)
                    button.isSelected = false
                currentProjectionMode = mode
                redraw()
            }
            button.isSelected = true
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
            multiplePointAndMatrix(point, axonometricMatrix(145.0 * PI / 180, 45.0 * PI / 180))
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

fun saveModel(model: Polyhedron, fileName: String) {
    val writer = File("assets/3dmodels/$fileName.obj").bufferedWriter()

    // добавляем вершины
    writer.write("v ")
    writer.write(model.vertices[0].x.toString())
    writer.write(" ")
    writer.write(model.vertices[0].y.toString())
    writer.write(" ")
    writer.write(model.vertices[0].z.toString())
    for (i in 1 until model.vertices.size) {
        writer.write("\nv ")
        writer.write(model.vertices[i].x.toString())
        writer.write(" ")
        writer.write(model.vertices[i].y.toString())
        writer.write(" ")
        writer.write(model.vertices[i].z.toString())
    }

    // добавляем нормали
    model.polygons.forEach {
        writer.write("\nvn")
        val polygon = it
        var normal = findNormal(polygon.points[0], polygon.points[1], polygon.points[2])
        writer.write(" " + normal.x + " " + normal.y + " " + normal.x)
    }

    // добавляем поверхности
    var p_i = 1
    model.polygons.forEach {
        writer.write("\nf")
        val polygon = it
        polygon.points.forEach {
            for (i in 0 until model.vertices.size) {
                if (it.x == model.vertices[i].x &&
                        it.y == model.vertices[i].y &&
                        it.z == model.vertices[i].z) {
                    writer.write(" " + (i + 1).toString())
                    writer.write("//" + p_i.toString())
                }
            }
        }
        p_i += 1
    }
    writer.close()
}

fun findNormal(p0: Point3D, p1: Point3D, p2: Point3D) : Point3D {
    val A = (p1.y - p0.y) * (p2.z - p0.z) - (p1.z - p0.z) * (p2.y - p0.y)
    val B = (p1.z - p0.z) * (p2.x - p0.x) - (p1.x - p0.x) * (p2.z - p0.z)
    val C = (p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x)
    return Point3D(A, B, C)
}