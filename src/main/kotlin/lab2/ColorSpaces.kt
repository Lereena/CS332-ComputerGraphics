package lab2

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.FileInputStream


class ColorSpaces : Application() {
    override fun start(primaryStage: Stage) {
        val rootScene = DemoScene(primaryStage)
        val task1Scene = Task1(primaryStage)
        val task3Scene = Task3(primaryStage)

        primaryStage.show()

        rootScene.switch()

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED) { e ->
            when(e.text) {
                "0" -> rootScene.switch()
                "1" -> task1Scene.switch()
                "3" -> task3Scene.switch()
            }
        }
    }

    class DemoScene(override val primaryStage: Stage): SceneWrapper(primaryStage, "Graphics task") {
        init {
            val root = Group()
            val canvas = Canvas(800.0, 600.0)
            val gc = canvas.graphicsContext2D
            root.children.add(canvas)
            scene = Scene(root)

            gc.stroke = Color.BLUEVIOLET
            gc.lineWidth = 1.0
            gc.moveTo(0.0, 0.0)
            gc.lineTo(800.0, 600.0)
            gc.stroke()

            val imgFile = Image(FileInputStream("assets/fruits.jpg"))
            gc.drawImage(imgFile, 0.0, 0.0, 400.0, 300.0);
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ColorSpaces::class.java, *args)
        }
    }
}