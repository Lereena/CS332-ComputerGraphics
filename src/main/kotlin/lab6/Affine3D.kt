package lab6

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage

class Affine3D : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Affine transformations 3D"
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val width = 800.0
        val height = 600.0
        val canvas = Canvas(width, height)
        val dotsGC = canvas.graphicsContext2D
        dotsGC.stroke = Color.BLACK

        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Affine3D::class.java, *args)
        }
    }
}