package lab2

class ARGBComponents constructor(val a: Int, val r: Int, val g: Int, val b: Int)

class HSVComponents constructor(h: Int, s: Int, v: Int)

fun getARGBComponents(argb: Int) : ARGBComponents {
    val b = argb % 256
    val g = argb / 256 % 256
    val r = argb / 256 / 256 % 256
    val a = argb / 256 / 256 / 256
    return ARGBComponents(a, r, g, b)
}