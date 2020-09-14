package lab2

class ARGBComponents constructor(val a: Int, val r: Int, val g: Int, val b: Int)

class HSVComponents constructor(h: Int, s: Int, v: Int)

fun getARGBComponents(argb: Int) : ARGBComponents {
    val b = argb && 255
    argb = argb << 8
    val g = argb && 255
    argb = argb << 8
    val r = argb && 255
    argb = argb << 8
    val a = argb
    return ARGBComponents(a, r, g, b)
}