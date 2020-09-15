package lab2

import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO
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

        val hueSlider = Slider(-100.0, 100.0, 0.0)
        val saturationSlider = Slider(-100.0, 100.0, 0.0)
        val valueSlider = Slider(-100.0, 100.0, 0.0)
        configureSlider(hueSlider)
        configureSlider(saturationSlider)
        configureSlider(valueSlider)
        val saveButton = Button("Save")
        root.children.addAll(
            Label("Hue"),
            hueSlider,
            Label("Saturation"),
            saturationSlider,
            Label("Value"),
            valueSlider,
            saveButton
        )

        var hueValue = 50.0
        var saturationValue = 50.0
        var valueValue = 50.0

        val image = Image(FileInputStream("assets/fruits.jpg"))
        val HSVImage = RGBImageToHSV(image)
        var transformedImage: Image? = null
        val k = image.height / image.width
        gc.drawImage(image, 50.0, 50.0, 700.0, 700.0 * k)
        hueSlider.setOnMouseReleased {
            transformedImage = sliderChangeEvent(gc, HSVImage, Component.H, (hueSlider.value - hueValue).toInt())
            hueValue = hueSlider.value
        }
        saturationSlider.setOnMouseReleased {
            transformedImage =
                sliderChangeEvent(gc, HSVImage, Component.S, (saturationSlider.value - saturationValue).toInt())
            saturationValue = saturationSlider.value
        }
        valueSlider.setOnMouseReleased {
            transformedImage = sliderChangeEvent(gc, HSVImage, Component.V, (valueSlider.value - valueValue).toInt())
            valueValue = valueSlider.value
        }
        saveButton.setOnMouseClicked {
            if (transformedImage != null) {
                ImageIO.write(
                    SwingFXUtils.fromFXImage(transformedImage, null),
                    "png",
                    File("assets/transformedFruits.png")
                )
                val alert = Alert(Alert.AlertType.INFORMATION, "Image saved", ButtonType.OK)
                alert.show()
            } else {
                val alert = Alert(
                    Alert.AlertType.INFORMATION,
                    "You haven't done any changes. Image will not be saved",
                    ButtonType.OK
                )
                alert.show()
            }
        }
    }

    fun sliderChangeEvent(
        gc: GraphicsContext, image: Array<Array<HSVComponents>>,
        component: Component, addition: Int
    ): Image {

        val transformedImage = HSVImageToRGB(processImage(image, component, addition))
        val k = transformedImage.height / transformedImage.width
        gc.drawImage(transformedImage, 50.0, 50.0, 700.0, 700.0 * k)

        return transformedImage
    }

    fun processImage(
        image: Array<Array<HSVComponents>>,
        component: Component,
        addition: Int
    ): Array<Array<HSVComponents>> {
        val newImage = when (component) {
            Component.H -> changeHSV(image, Component.H, addition)
            Component.S -> changeHSV(image, Component.S, addition)
            Component.V -> changeHSV(image, Component.V, addition)
        }
//        return HSVImageToRGB(newImage)
        return newImage
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
                hsvImage[x][y] = pixelToHSV(pixel)
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
        val hue = pixel.h // 0-360
        val saturation = pixel.s * 100 // 0-100
        var value = pixel.v * 100 // 0-100

        // 0-5
        val hi = (floor(hue / 60) % 6).toInt()
        // 0-100
        var vMin = ((100 - saturation) * value) / 100.0
        // 0-100
        val a = (value - vMin) * ((hue % 60) / 60.0)
        //
        val vInc = (vMin + a) / 100.0
        val vDec = (value - a) / 100.0
        val coef = 1
        value /= 100
        vMin /= 100

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

    fun setHSVComponent(pixel: HSVComponents, component: Component, addition: Int): HSVComponents {
        return when (component) {
            Component.H -> {
                if (addition <= 0)
                    HSVComponents((pixel.h / 100) * (100 + addition), pixel.s, pixel.v)
                else
                    HSVComponents(pixel.h + ((360 - pixel.h) / 100) * addition, pixel.s, pixel.v)
            }
            //HSVComponents(abs((pixel.h + addition) % 360), pixel.s, pixel.v)
            Component.S -> {
                if (addition <= 0)
                    HSVComponents(pixel.h, (pixel.s / 100) * (100 + addition), pixel.v)
                else
                    HSVComponents(pixel.h, pixel.s + ((1.0 - pixel.s) / 100) * addition, pixel.v)
            }
            //HSVComponents(pixel.h, min(max(pixel.s + (addition / 100.0), 0.0), 1.0), pixel.v)
            Component.V -> {
                if (addition <= 0)
                    HSVComponents(pixel.h, pixel.s, (pixel.v / 100) * (100 + addition))
                else
                    HSVComponents(pixel.h, pixel.s, pixel.v + ((1.0 - pixel.v) / 100) * addition)
            }
            //HSVComponents(pixel.h, pixel.s, min(max(pixel.v + (addition / 100.0), 0.0), 1.0))
        }
    }

    fun changeHSV(
        image: Array<Array<HSVComponents>>,
        component: Component,
        addition: Int
    ): Array<Array<HSVComponents>> {
        val width = image.size
        val height = image[0].size
        val newImage = Array(width) {
            Array(height) { HSVComponents(0.0, 0.0, 0.0) }
        }

        for (x in (0 until width))
            for (y in (0 until height)) {
                newImage[x][y] = setHSVComponent(image[x][y], component, addition)
            }

        return newImage
    }

    fun pixelToHSV(pixel: Color): HSVComponents {
        val red = pixel.red
        val green = pixel.green
        val blue = pixel.blue

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