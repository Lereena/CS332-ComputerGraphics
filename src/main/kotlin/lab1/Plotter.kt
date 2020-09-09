package lab1

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sin

class Plotter : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Plot constructor"
        val root = Group()
        val width = 800.0
        val height = 600.0
        val canvas = Canvas(width, height)
        val canvas2 = Canvas(width, height)
        val axisGC = canvas.graphicsContext2D
        val plotGC = canvas2.graphicsContext2D

//        drawGraph(axisGC, plotGC, -5.0, 3.0, 0.001) { x -> sin(x) }
//        drawGraph(axisGC, plotGC, -10.0, 10.0, 0.001) { x -> x.pow(2.0)}
        drawGraph(axisGC, plotGC, -5.0, 7.0, 0.001) { x -> sin(x) * x.pow(2) }

        root.children.add(canvas)
        root.children.add(canvas2)
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    private fun drawGraph(
        axisGC: GraphicsContext, plotGC: GraphicsContext,
        minX: Double, maxX: Double,
        step: Double, functio: (Double) -> Double
    ) {
        val (minY, maxY) = findExtremes(functio, minX, maxX, step)
        val width = axisGC.canvas.width
        val height = axisGC.canvas.height
        val coefX = width / (maxX - minX)
        val coefY = height / (maxY - minY)

        axisGC.stroke = Color.BLACK
        axisGC.lineWidth = 1.0

        val axisX = width * (-minX / (maxX - minX))
        val axisY = height * (1 - (-minY / (maxY - minY)));
        axisGC.moveTo(axisX, 0.0)
        axisGC.lineTo(axisX, height)
        axisGC.moveTo(0.0, axisY)
        axisGC.lineTo(width, axisY)
        axisGC.stroke()

        val axisStep = ceil((maxX - minX) / 20)
        var x = minX - (minX % axisStep)
        while (x < maxX) {
            axisGC.moveTo((x - minX) * coefX, axisY - 4)
            axisGC.lineTo((x - minX) * coefX, axisY + 4)
            x += axisStep
        }
        var y = minY - (minY % axisStep)
        while (y < maxY) {
            axisGC.moveTo(axisX - 4, (maxY - y) * coefY)
            axisGC.lineTo(axisX + 4, (maxY - y) * coefY)
            y += axisStep
        }
        axisGC.stroke()

        plotGC.stroke = Color.GREEN
        plotGC.lineWidth = 1.0
        x = minX
        y = functio(x)
        while (x <= maxX) {
            if (y.isNaN())
                continue
            val graphX = (x - minX) * coefX
            val graphY = (maxY - y) * coefY
            plotGC.moveTo(graphX, graphY)
            plotGC.lineTo(graphX, graphY)
            x += step
            y = functio(x)
        }
        plotGC.stroke()
    }

    private fun findExtremes(
        functio: (Double) -> Double, left: Double, right: Double, step: Double
    ): Pair<Double, Double> {
        val valuesCount = ceil((right - left) / step).toInt()

        var min = Double.MAX_VALUE
        var max = Double.MIN_VALUE
        var x = left
        for (index in 0 until valuesCount) {
            val y = functio(x)
            if (y.isNaN())
                continue
            if (y < min) min = y
            if (y > max) max = y
            x += step
        }

        return Pair(min, max)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Plotter::class.java, *args)
        }
    }
}