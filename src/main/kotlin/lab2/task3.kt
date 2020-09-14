package lab2

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.FileInputStream
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Task3(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 3") {
    init {
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        scene = Scene(root)

        val hueSlider = Slider(0.0, 100.0, 50.0)
        val saturationSlider = Slider(0.0, 100.0, 50.0)
        val valueSlider = Slider(0.0, 100.0, 50.0)
        configureSlider(hueSlider)
        configureSlider(saturationSlider)
        configureSlider(valueSlider)
        root.children.addAll(
            Label("Hue"),
            hueSlider,
            Label("Saturation"),
            saturationSlider,
            Label("Value"),
            valueSlider
        )

        var hueValue = 50.0
        var saturationValue = 50.0
        var valueValue = 50.0

        val image = Image(FileInputStream("assets/fruits.jpg"))
        val HSVImage = RGBImageToHSV(image)
        val k = image.height / image.width
        gc.drawImage(image, 50.0, 50.0, 700.0, 700.0 * k)
        hueSlider.setOnDragDetected {
            sliderChangeEvent(gc, HSVImage, Component.H, (hueSlider.value - hueValue).toInt())
            hueValue = hueSlider.value
        }
        saturationSlider.setOnDragDetected {
            sliderChangeEvent(gc, HSVImage, Component.S, (saturationSlider.value - saturationValue).toInt())
            saturationValue = saturationSlider.value
        }
        valueSlider.setOnDragDetected {
            sliderChangeEvent(gc, HSVImage, Component.V, (valueSlider.value - valueValue).toInt())
            valueValue = valueSlider.value
        }
    }

    fun sliderChangeEvent(
        gc: GraphicsContext, image: Array<Array<HSVComponents>>,
        component: Component, addition: Int
    ) {
        when (component) {
            Component.H -> changeHSV(image, Component.H, addition)
            Component.S -> changeHSV(image, Component.S, addition)
            Component.V -> changeHSV(image, Component.V, addition)
        }
        val transformedImage = HSVImageToRGB(image)

        val k = transformedImage.height / transformedImage.width
        gc.drawImage(transformedImage, 50.0, 50.0, 700.0, 700.0 * k)
    }

    fun configureSlider(slider: Slider) {
        slider.isSnapToTicks = true
        slider.orientation = Orientation.HORIZONTAL
        slider.isShowTickMarks = true
        slider.isShowTickLabels = true
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