package lab2

class ARGBComponents constructor(val a: Int, val r: Int, val g: Int, val b: Int)

class HSVComponents constructor(h: Int, s: Int, v: Int)

fun evalARGBComponents(argb: Int) : ARGBComponents {
    var _argb = argb
    val b = argb and 255
    _argb = _argb shr 8
    val g = argb and 255
    _argb = _argb shr 8
    val r = argb and 255
    _argb = _argb shr 8
    val a = argb
    return ARGBComponents(a, r, g, b)
}