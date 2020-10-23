package lab6

import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.stage.Stage

class Affine3D : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Affine transformations 3D"
        val root = FlowPane(Orientation.VERTICAL, 0.0, 30.0)
        val width = 800.0
        val height = 600.0
        val canvas = Canvas(width, height)
        val dotsGC = canvas.graphicsContext2D
        dotsGC.stroke = Color.BLACK

        root.children.add(canvas)
        primaryStage.scene = Scene(root)
        primaryStage.show()

        var model = Polyhedron("assets/3dmodels/rooster.obj")
        scale(model, 50.0, 50.0, 50.0)
        orthographic_projection(canvas, dotsGC, model, Axis.Z)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Affine3D::class.java, *args)
        }
    }
}

//fun orthographic_projection(canvas: Canvas, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
//    when (ax) {
//        Axis.Z -> for (i_p in 0 until model.size) {
//            for (i_l in 0 until model[i_p].size) {
//                gc.moveTo(model[i_p][i_l].point1.x + canvas.width / 2,
//                        model[i_p][i_l].point1.y * (-1) + canvas.height / 2)
//                gc.lineTo(model[i_p][i_l].point2.x + canvas.width / 2,
//                        model[i_p][i_l].point2.y * (-1) + canvas.height / 2)
//                gc.stroke()
//            }
//        }
//        Axis.X -> for (i_p in 0 until model.size) {
//            for (i_l in 0 until model[i_p].size) {
//                gc.moveTo(model[i_p][i_l].point1.z + canvas.width / 2,
//                        model[i_p][i_l].point1.y * (-1) + canvas.height / 2)
//                gc.lineTo(model[i_p][i_l].point2.z + canvas.width / 2,
//                        model[i_p][i_l].point2.y * (-1) + canvas.height / 2)
//                gc.stroke()
//            }
//        }
//        Axis.Y -> for (i_p in 0 until model.size) {
//            for (i_l in 0 until model[i_p].size) {
//                gc.moveTo(model[i_p][i_l].point1.x + canvas.width / 2,
//                        model[i_p][i_l].point1.z * (-1) + canvas.height / 2)
//                gc.lineTo(model[i_p][i_l].point2.x + canvas.width / 2,
//                        model[i_p][i_l].point2.z * (-1) + canvas.height / 2)
//                gc.stroke()
//            }
//        }
//    }
//}

fun orthographic_projection(canvas: Canvas, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    for (polygon in model.polygons) {
        for (i in polygon.points.indices) {
            when (ax) {
                Axis.Z -> {
                    gc.moveTo(polygon[i].x + canvas.width / 2, polygon[i].y * (-1) + canvas.width / 2)
                    gc.lineTo(polygon[i + 1].x + canvas.width / 2, polygon[i + 1].y * (-1) + canvas.width / 2)
                    gc.stroke()
                }
                Axis.X -> {
                    gc.moveTo(polygon[i].z + canvas.width / 2, polygon[i].y * (-1) + canvas.width / 2)
                    gc.lineTo(polygon[i + 1].z + canvas.width / 2, polygon[i + 1].y * (-1) + canvas.width / 2)
                    gc.stroke()
                }
                Axis.Y -> {
                    gc.moveTo(polygon[i].x + canvas.width / 2, polygon[i].z * (-1) + canvas.width / 2)
                    gc.lineTo(polygon[i + 1].x + canvas.width / 2, polygon[i + 1].z * (-1) + canvas.width / 2)
                    gc.stroke()
                }
            }
        }
    }
}
