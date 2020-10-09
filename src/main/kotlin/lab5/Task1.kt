package lab5

import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import lab2.SceneWrapper
import java.io.File
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
        val drawButton = Button("Нарисовать кривую")
        root.children.addAll(list, drawButton)

        val fileChooser = FileChooser()
        fileChooser.title = "Выберите файл с описанием L-системы"
        fileChooser.initialDirectory = File("LSystems")
        
        drawButton.setOnMouseClicked {
            when (list.value) {
                null -> Alert(Alert.AlertType.ERROR, "Не выбран тип кривой")
                "Из файла" -> {
                    val selectedFile = fileChooser.showOpenDialog(primaryStage)
                    if (selectedFile != null) {
                        val system = selectedFile.readLines(Charsets.UTF_8)
                        getSystem(gc, system, 1)
                    }
                }
                "Кривая Коха" -> getSystem(gc, getSystemDescription("LSystems/koch-curve"), 1)
                "Квадратный остров Коха" -> getSystem(gc, getSystemDescription("LSystems/koch-square-island"), 1)
                "Кривая Гильберта" -> getSystem(gc, getSystemDescription("LSystems/hilbert-curve"), 1)
                "Шестиугольная мозаика" -> getSystem(gc, getSystemDescription("LSystems/hexagonal-mosaic"), 1)
                "Куст" -> getSystem(gc, getSystemDescription("LSystems/bush"), 1)
            }
        }
    }
}

fun getSystemDescription(path: String): List<String> {
    return File(path).readLines(Charsets.UTF_8)
}

fun getSystem(gc: GraphicsContext, systemDescription: List<String>, stepsCount: Int) {
    val rules = HashMap<Char, String>()

    val firstLine = systemDescription[0].split(' ')
    var currentState = firstLine[0]
    val angle = firstLine[1].toDouble()
    val direction = firstLine[2]
    for (i in 1 until systemDescription.size)
        rules[systemDescription[i][0]] = systemDescription[i].substring(2)

    for (i in 0..stepsCount) {
        var nextState = ""
        for (symbol in currentState)
            if (rules.containsKey(symbol))
                nextState += rules[symbol]
            else
                nextState += symbol

        currentState = nextState
    }

    drawState(gc, currentState, angle, direction)
    gc.stroke()
}

fun drawState(gc: GraphicsContext, state: String, angle: Double, direction: String) {
    val points = LinkedList<Point>()
    val length = 100
    var currentAngle = 0.0
    when (direction) {
        "left" -> currentAngle = 180.0
        "up" -> currentAngle = 270.0
    }

    var currentPoint = Point(0.0, 0.0)
    points.addLast(currentPoint)

    val branches = LinkedList<Stack<Point>>()
    branches.addLast(Stack<Point>())
    val angles = LinkedList<Double>()
    angles.addLast(currentAngle)

    for (symbol in state) {
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
                val newX = currentPoint.x + (length * cos(currentAngle / 180 * PI))
                val newY = currentPoint.y + (length * sin(currentAngle / 180 * PI))
                val nextPoint = Point(newX, newY)
                currentPoint = nextPoint
                points.addLast(currentPoint)
                branches[branches.size - 1].push(currentPoint)
            }
            '-' -> currentAngle -= angle
            '+' -> currentAngle += angle
        }

        var minX = Int.MAX_VALUE;
        var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE;
        var maxY = Int.MIN_VALUE

        for (i in points.indices) {
            if (points[i].x < minX)
                minX = points[i].x.toInt()
            if (points[i].x < maxX)
                maxX = points[i].x.toInt()
            if (points[i].y < minY)
                minY = points[i].y.toInt()
            if (points[i].y < maxY)
                maxY = points[i].y.toInt()
        }

        val middlePoint = Point(((minX + maxX) / 2).toDouble(), ((minY + maxY) / 2).toDouble())
        val windowMiddle = Point(400.0, 300.0)

        val coefX = 400.0 / (maxX - minX + 1)
        val coefY = 300.0 / (maxY - minY)
        val coef = min(coefX, coefY)

        val actualPoints = LinkedList<Point>()
        for (i in points.indices) {
            val distanceX = (points[i].x - middlePoint.x) * coef
            val distanceY = (points[i].y - middlePoint.y) * coef
            actualPoints.addLast(Point(windowMiddle.x + distanceX, windowMiddle.y + distanceY))
        }

        gc.moveTo(actualPoints[0].x, actualPoints[0].y)
        for (i in 1 until actualPoints.size) {
            val point = actualPoints[i]
            gc.lineTo(point.x, point.y)
            gc.moveTo(point.x, point.y)
        }
    }
}