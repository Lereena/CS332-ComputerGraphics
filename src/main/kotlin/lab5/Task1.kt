package lab5

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import lab2.SceneWrapper
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Point(val x: Double, val y: Double) {
    override fun toString(): String {
        return "($x, $y)"
    }
}

class Task1(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 1") {
    init {
        val root = FlowPane(Orientation.HORIZONTAL, 0.0, 30.0)
        root.padding = Insets(5.0, 5.0, 5.0, 5.0)
        root.vgap = 4.0; root.hgap = 8.0
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        scene = Scene(root)
        root.children.add(canvas)
        gc.stroke = Color.BLUEVIOLET

        val items = FXCollections.observableArrayList(
            "Кривая Коха", "Квадратный остров Коха",
            "Кривая Гильберта", "Шестиугольная мозаика",
            "Куст", "Из файла"
        )
        val list = ComboBox(items)
        val stepsLabel = Label("Количество шагов: ")
        val stepsInputField = TextField("1")
        val drawButton = Button("Нарисовать кривую")
        root.children.addAll(list, stepsLabel, stepsInputField, drawButton)

        val fileChooser = FileChooser()
        fileChooser.title = "Выберите файл с описанием L-системы"
        fileChooser.initialDirectory = File("LSystems")

        drawButton.setOnMouseClicked {
            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            gc.beginPath()
            val steps = stepsInputField.text.toInt()
            when (list.value) {
                null -> Alert(Alert.AlertType.ERROR, "Не выбран тип кривой")
                "Из файла" -> {
                    val selectedFile = fileChooser.showOpenDialog(primaryStage)
                    if (selectedFile != null) {
                        val state = getState(selectedFile.readLines(Charsets.UTF_8), steps)
                        drawState(gc, state.first, state.second, state.third)
                    }
                }
                "Кривая Коха" -> {
                    val state = getState(getSystemDescription("LSystems/koch-curve"), steps)
                    drawState(gc, state.first, state.second, state.third)
                }
                "Квадратный остров Коха" -> {
                    val state = getState(getSystemDescription("LSystems/koch-square-island"), steps)
                    drawState(gc, state.first, state.second, state.third)
                }
                "Кривая Гильберта" -> {
                    val state = getState(getSystemDescription("LSystems/hilbert-curve"), steps)
                    drawState(gc, state.first, state.second, state.third)
                }
                "Шестиугольная мозаика" -> {
                    val state = getState(getSystemDescription("LSystems/hexagonal-mosaic"), steps)
                    drawState(gc, state.first, state.second, state.third)
                }
                "Куст" -> {
                    val state = getState(getSystemDescription("LSystems/bush"), steps)
                    drawState(gc, state.first, state.second, state.third)
                }
            }
        }
    }
}

fun getSystemDescription(path: String): List<String> {
    return File(path).readLines(Charsets.UTF_8)
}

fun getState(systemDescription: List<String>, stepsCount: Int): Triple<String, Double, String> {
    val rules = HashMap<Char, String>()

    val firstLine = systemDescription[0].split(' ')
    var currentState = firstLine[0]
    val angle = firstLine[1].toDouble()
    val direction = firstLine[2]
    for (i in 1 until systemDescription.size)
        rules[systemDescription[i][0]] = systemDescription[i].substring(2)

    for (i in 0..stepsCount) {
        val nextState = StringBuilder()
        for (symbol in currentState)
            if (rules.containsKey(symbol))
                nextState.append(rules[symbol])
            else
                nextState.append(symbol)

        currentState = nextState.toString()
    }

    return Triple(currentState, angle, direction)
}

fun drawState(gc: GraphicsContext, state: String, angle: Double, direction: String) {
    val points = LinkedList<Point>()
    var currentAngle = 0.0
    when (direction) {
        "down" -> currentAngle = 90.0
        "left" -> currentAngle = 180.0
        "up" -> currentAngle = 270.0
    }

    var currentPoint = Point(0.0, 0.0)
    points.addLast(currentPoint)

    val branches = LinkedList<Stack<Point>>()
    branches.addLast(Stack<Point>())
    val angles = LinkedList<Double>()
    angles.addLast(currentAngle)

    for (symbol in state)
        when (symbol) {
            '[' -> {
                val st = Stack<Point>()
                st.push(currentPoint)
                branches.addLast(st)
                angles.addLast(currentAngle)
            }
            ']' -> {
                val size = branches.size
                while (branches[size - 1].size != 0) {
                    val point = branches[size - 1].pop()
                    points.addLast(point)
                    if (branches[size - 1].size == 0) {
                        currentAngle = angles[size - 1]
                        currentPoint = point
                    }
                }
                branches.removeAt(size - 1)
                angles.removeAt(size - 1)
            }
            'F' -> {
                val newX = currentPoint.x + cos(currentAngle / 180 * PI)
                val newY = currentPoint.y + sin(currentAngle / 180 * PI)
                val nextPoint = Point(newX, newY)
                currentPoint = nextPoint
                points.addLast(currentPoint)
                branches[branches.size - 1].push(currentPoint)
            }
            '-' -> currentAngle -= angle
            '+' -> currentAngle += angle
        }

    val scaledPoints = scalePoints(points)

    gc.moveTo(scaledPoints[0].x, scaledPoints[0].y)
    for (i in 1 until scaledPoints.size) {
        val point = scaledPoints[i]
        gc.lineTo(point.x, point.y)
        gc.moveTo(point.x, point.y)
    }

    gc.stroke()
}

fun scalePoints(points: LinkedList<Point>): LinkedList<Point> {
    var minX = Int.MAX_VALUE; var maxX = Int.MIN_VALUE
    var minY = Int.MAX_VALUE; var maxY = Int.MIN_VALUE

    for (i in points.indices) {
        if (points[i].x < minX)
            minX = points[i].x.toInt()
        if (points[i].x > maxX)
            maxX = points[i].x.toInt()
        if (points[i].y < minY)
            minY = points[i].y.toInt()
        if (points[i].y > maxY)
            maxY = points[i].y.toInt()
    }

    val middlePoint = Point(((minX + maxX) / 2).toDouble(), ((minY + maxY) / 2).toDouble())
    val windowMiddle = Point(400.0, 300.0)

    val coefX = 666.0 / (maxX - minX)
    val coefY = 500.0 / (maxY - minY)
    val coef = min(coefX, coefY)

    val scaledPoints = LinkedList<Point>()
    for (i in points.indices) {
        val distanceX = (points[i].x - middlePoint.x) * coef
        val distanceY = (points[i].y - middlePoint.y) * coef
        scaledPoints.addLast(Point(windowMiddle.x + distanceX, windowMiddle.y + distanceY))
    }

    return scaledPoints
}