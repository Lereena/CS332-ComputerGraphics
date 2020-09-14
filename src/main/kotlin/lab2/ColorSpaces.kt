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
        primaryStage.title = "Graphics program"

        val task1 = Task1(primaryStage)
        val root = Group()
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        val rootScene = Scene(root)
        primaryStage.scene = rootScene
        primaryStage.show()

        gc.stroke = Color.BLUEVIOLET
        gc.lineWidth = 1.0
        gc.moveTo(0.0, 0.0)
        gc.lineTo(800.0, 600.0)
        gc.stroke()

        val imgFile = Image(FileInputStream("assets/fruits.jpg"))
        gc.drawImage(imgFile, 0.0, 0.0, 400.0, 300.0);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED) { e ->
            when(e.text) {
                "0" -> primaryStage.scene = rootScene
                "1" -> task1.switch()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ColorSpaces::class.java, *args)
        }
    }
}