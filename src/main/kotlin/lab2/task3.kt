package lab2

import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.FileInputStream
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Task3(override val primaryStage: Stage): SceneWrapper(primaryStage, "Task 3") {
    init {
        val root = Group()
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        scene = Scene(root)

        val image = Image(FileInputStream("assets/fruits.jpg"))
        val HSVImage = RGBImageToHSV(image)
        changeHSV(HSVImage, Component.H, 50)
        val transformedImage = HSVImageToRGB(HSVImage)
        gc.drawImage(transformedImage, 0.0, 0.0, 800.0, 600.0)
    }

    fun RGBImageToHSV(image: Image): Array<Array<HSVComponents>> {
        val pixelReader = image.pixelReader
        val width = image.width.toInt()
        val height = image.height.toInt()

        val hsvImage = Array(width) {
            Array(height) { HSVComponents(0.0, 0.0, 0.0) }
        }

        for (x in (0 until width))
            for (y in (0 until height)) {
                val pixel = pixelReader.getColor(x, y)
                val hsvPixel = pixelToHSV(pixel)
                hsvImage[x][y] = hsvPixel
            }

        return hsvImage
    }

    fun HSVImageToRGB(image: Array<Array<HSVComponents>>): Image {
        val width = image.size
        val height = image[0].size
        val newImage = WritableImage(width, height)
        val pixelWriter = newImage.pixelWriter

        for (x in (0 until width - 1))
            for (y in (0 until height - 1))
                pixelWriter.setColor(x, y, pixelToRGB(image[x][y]))

        return newImage
    }

    fun pixelToRGB(pixel: HSVComponents): Color {
        val hue = pixel.h
        val saturation = pixel.s * 100
        val value = pixel.v * 100

        val hi = (floor(hue / 60) % 6).toInt()
        val vMin = ((100 - saturation) * value) / 100
        val a = (value - vMin) * ((hue % 60) / 60)
        val vInc = vMin + a
        val vDec = value - a
        val coef = 2.55

        return when (hi) {
            0 -> Color(value * coef, vInc * coef, vMin * coef, 1.0)
            1 -> Color(vDec * coef, value * coef, vMin * coef, 1.0)
            2 -> Color(vMin * coef, value * coef, vInc * coef, 1.0)
            3 -> Color(vMin * coef, vDec * coef, value * coef, 1.0)
            4 -> Color(vInc * coef, vMin * coef, value * coef, 1.0)
            5 -> Color(value * coef, vMin * coef, vDec * coef, 1.0)
            else -> throw Exception("Какое-то странное взятие остатка от деления на 6")
        }
    }

    fun setHSVComponent(pixel: HSVComponents, component: Component, addition: Int) {
        if (addition !in 0..100)
            throw Exception("Изменение должно лежать в диапазоне от 0% до 100%")

        when (component) {
            Component.H -> pixel.h = abs((pixel.h + addition) % 360)
            Component.S -> pixel.s = min(max(pixel.s + (addition / 100.0), 0.0), 1.0)
            Component.V -> pixel.v = min(max(pixel.v + (addition / 100.0), 0.0), 1.0)
        }
    }

    fun changeHSV(image: Array<Array<HSVComponents>>, component: Component, addition: Int) {
        val width = image.size
        val height = image[0].size

        for (x in (0 until width))
            for (y in (0 until height)) {
                setHSVComponent(image[x][y], component, addition)
            }
    }

    fun pixelToHSV(pixel: Color): HSVComponents {
        val red = pixel.red / 255.0
        val green = pixel.green / 255.0
        val blue = pixel.blue / 255.0

        val max = max(red, max(green, blue))
        val min = min(red, min(green, blue))

        val h = if (max == min)
            0.0
        else if (max == red && green >= blue)
            (60 * (green - blue)) / (max - min)
        else if (max == red && green < blue)
            (60 * (green - blue)) / (max - min) + 360
        else if (max == green)
            (60 * (blue - red)) / (max - min) + 120
        else
            (60 * (red - green)) / (max - min) + 240

        val s = if (max == 0.0) 0.0 else 1.0 - (min / max)

        return HSVComponents(h, s, max)
    }
}

enum class Component { H, S, V }