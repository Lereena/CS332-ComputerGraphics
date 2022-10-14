package ind2kostikova

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.stage.Stage

class CornishRoom : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Cornish room"
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val width = 480.0
        val height = 480.0
        val canvas = Canvas(width, height)
        val gc = canvas.graphicsContext2D

//        val reflectionSection = GridPane()

        root.children.add(canvas)
        primaryStage.scene = Scene(root)

        val rayTracer = RayTracer(480, 480)
        rayTracer.showScene(gc)

        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(CornishRoom::class.java, *args)
        }
    }
}