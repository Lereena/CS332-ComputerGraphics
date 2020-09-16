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
import kotlin.math.*

class Task3(override val primaryStage: Stage) : SceneWrapper(primaryStage, "Task 3") {
    init {
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val canvas = Canvas(800.0, 600.0)
        val gc = canvas.graphicsContext2D
        root.children.add(canvas)
        scene = Scene(root)

        val hueSlider = Slider(-180.0, 180.0, 0.0)
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

        val image = Image(FileInputStream("assets/fruits.jpg"))
        val originalHSVImage = RGBImageToHSV(image)
        val HSVImage = RGBImageToHSV(image)

        val testRGB = image.pixelReader.getColor(0, 0)

        var transformedImage: Image? = null
        val k = image.height / image.width
        gc.drawImage(image, 50.0, 50.0, 700.0, 700.0 * k)
        hueSlider.setOnMouseReleased {
            transformedImage = sliderChangeEvent(gc, HSVImage, originalHSVImage, Component.H, (hueSlider.value).toInt())
        }
        saturationSlider.setOnMouseReleased {
            transformedImage = sliderChangeEvent(gc, HSVImage, originalHSVImage, Component.S, (saturationSlider.value).toInt())
        }
        valueSlider.setOnMouseReleased {
            transformedImage = sliderChangeEvent(gc, HSVImage, originalHSVImage, Component.V, (valueSlider.value).toInt())
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
        originalImage: Array<Array<HSVComponents>>,
        component: Component, addition: Int
    ): Image {

        val transformedImage = HSVImageToRGB(updateImage(image, originalImage, component, addition))
        val k = transformedImage.height / transformedImage.width
        gc.drawImage(transformedImage, 50.0, 50.0, 700.0, 700.0 * k)

        return transformedImage
    }

    fun updateImage(
        image: Array<Array<HSVComponents>>,
        originalImage: Array<Array<HSVComponents>>,
        component: Component,
        addition: Int
    ): Array<Array<HSVComponents>> {
        for (x in (0 until image.size))
            for (y in (0 until image[0].size))
                image[x][y] = setHSVComponent(image[x][y], originalImage[x][y], component, addition)
        return image
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
        val saturation = pixel.s // 0-100
        var value = pixel.v // 0-100

        val c = value * saturation
        val x = c * (1 - abs((hue / 60) % 2 - 1))
        val m = value - c
        val temp = when(hue) {
            in 0.0..60.0    -> Color(c, x, 0.0, 1.0)
            in 60.0..120.0  -> Color(x, c, 0.0, 1.0)
            in 120.0..180.0 -> Color(0.0, c, x, 1.0)
            in 180.0..240.0 -> Color(0.0, x, c, 1.0)
            in 240.0..300.0 -> Color(x, 0.0, c, 1.0)
            else            -> Color(c, 0.0, x, 1.0)
        }
        return Color(
            toInterval(temp.red + m),
            toInterval(temp.green + m),
            toInterval(temp.blue + m),
            1.0
        )

        // 0-5
//        val hi = (floor(hue / 60) % 6).toInt()
//        // 0-100
//        var vMin = ((100 - saturation) * value) / 100.0
//        // 0-100
//        val a = (value - vMin) * ((hue % 60) / 60.0)
//        //
//        val vInc = (vMin + a) / 100.0
//        val vDec = (value - a) / 100.0
//        val coef = 1.0
//        value /= 100
//        vMin /= 100
//
//        return when (hi) {
//            0 -> Color(value * coef, vInc * coef, vMin * coef, 1.0)
//            1 -> Color(vDec * coef, value * coef, vMin * coef, 1.0)
//            2 -> Color(vMin * coef, value * coef, vInc * coef, 1.0)
//            3 -> Color(vMin * coef, vDec * coef, value * coef, 1.0)
//            4 -> Color(vInc * coef, vMin * coef, value * coef, 1.0)
//            5 -> Color(value * coef, vMin * coef, vDec * coef, 1.0)
//            else -> throw Exception("Какое-то странное взятие остатка от деления на 6")
//        }
    }

    fun toInterval(x: Double): Double {
        if (x > 1)
            return 1.0
        if (x < 0)
            return 1.0
        return x
    }

    fun setHSVComponent(pixel: HSVComponents, originalPixel: HSVComponents, component: Component, addition: Int): HSVComponents {
        return when (component) {
            Component.H -> {
                var temp = originalPixel.h + addition
                if (temp < 0)
                    temp += 360.0
                if (temp > 360)
                    temp %= 360.0
                HSVComponents(temp, pixel.s, pixel.v)
            }
            Component.S -> {
//                var hslOg = HSVtoHSL(originalPixel)
//                var hsl = HSVtoHSL(pixel)
//                hsl.s = normalize(hslOg.s + addition / 100.0)
//                HSLtoHSV(hsl)


//                var temp = normalize(originalPixel.s + addition / 100.0)
//                HSVComponents(pixel.h, temp, pixel.v)
                val temp = scale(originalPixel.s, addition.toDouble(), 100.0)
                HSVComponents(pixel.h, temp, pixel.v)
            }
            //HSVComponents(pixel.h, min(max(pixel.s + (addition / 100.0), 0.0), 1.0), pixel.v)
            Component.V -> {
//                var hslOg = HSVtoHSL(originalPixel)
//                var hsl = HSVtoHSL(pixel)
//                hsl.l = normalize(hslOg.l + addition / 100.0)
//                HSLtoHSV(hsl)

                val temp = scale(originalPixel.v, addition.toDouble(), 100.0)
                HSVComponents(pixel.h, pixel.s, temp)
            }
        }
    }

    fun HSVtoHSL(hsv: HSVComponents): HSLComponents {
        val l = hsv.v * (1 - hsv.s / 2)
        val sL = when (l) {
            0.0, 1.0 -> 0.0
            else -> (hsv.v - l) / min(l, 1 - l)
        }
        return HSLComponents(hsv.h, sL, l)
    }

    fun HSLtoHSV(hsl: HSLComponents): HSVComponents {
        val v = hsl.l + hsl.s * min(hsl.l, 1 - hsl.l)
        val sV = when (v) {
            0.0 -> 0.0
            else -> 2 * (1 - hsl.l / v)
        }
        return HSVComponents(hsl.h, sV, v)
    }

    fun normalize(x: Double): Double {
        if (x < 0)
            return 0.0
        if (x > 1)
            return 1.0
        return x
    }

    fun scale(x: Double, addition: Double, addAbs: Double): Double {
        if (addition <= 0)
            return x * (1.0 + addition / addAbs)
        else
            return x + ((1.0 - x) / addAbs) * addition
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