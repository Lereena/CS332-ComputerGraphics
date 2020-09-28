package lab4

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab2.SceneWrapper
import java.io.FileInputStream

class AffineTransformations : Application() {
    override fun start(primaryStage: Stage) {
        val root = Group()
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        val scene = Scene(root)

        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(AffineTransformations::class.java, *args)
        }
    }
}
