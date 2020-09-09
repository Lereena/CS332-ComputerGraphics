package lab2

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage

class ColorSpaces : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Graphics program"
        val root = Group()
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        primaryStage.show()

        gc.stroke = Color.BLUEVIOLET
        gc.lineWidth = 1.0
        gc.moveTo(0.0, 0.0)
        gc.lineTo(800.0, 600.0)
        gc.stroke()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ColorSpaces::class.java, *args)
        }
    }
}