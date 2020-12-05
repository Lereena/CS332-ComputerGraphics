package ind2kostikova

import javafx.scene.paint.Color
import lab6.Point3D

enum class LightType { Ambient, Point }

class Light(val type: LightType, intensity: Double, val position: Point3D) {
    var rIntensity = intensity
    var gIntensity = intensity
    var bIntensity = intensity
    var color = Color(intensity, intensity, intensity, 1.0)
}
