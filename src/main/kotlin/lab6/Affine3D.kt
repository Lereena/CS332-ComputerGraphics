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

        var cube = Cube();
        orthographic_projection(canvas, dotsGC, cube, Axis.X)
        //orthographic_projection(canvas, dotsGC, cube, Axis.Y)
        //orthographic_projection(canvas, dotsGC, cube, Axis.Z)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Affine3D::class.java, *args)
        }
    }
}

fun Cube() : Polyhedron {
    var p1 = Point3D(0.0, 0.0, 0.0)
    var p2 = Point3D(0.0, 0.0, 200.0)
    var p3 = Point3D(0.0, 150.0, 0.0)
    var p4 = Point3D(0.0, 150.0, 200.0)
    var p5 = Point3D(150.0, 0.0, 0.0)
    var p6 = Point3D(150.0, 0.0, 200.0)
    var p7 = Point3D(150.0, 150.0, 0.0)
    var p8 = Point3D(150.0, 150.0, 200.0)

    var line1 = Line(p1, p2)
    var line2 = Line(p1, p3)
    var line3 = Line(p1, p5)
    var line4 = Line(p4, p2)
    var line5 = Line(p4, p3)
    var line6 = Line(p4, p8)
    var line7 = Line(p6, p2)
    var line8 = Line(p6, p5)
    var line9 = Line(p6, p8)
    var line10 = Line(p7, p3)
    var line11 = Line(p7, p5)
    var line12 = Line(p7, p8)

    var polygon1 = Polygon();
    polygon1.add(line1);
    polygon1.add(line2);
    polygon1.add(line4);
    polygon1.add(line5);

    var polygon2 = Polygon();
    polygon2.add(line1);
    polygon2.add(line3);
    polygon2.add(line7);
    polygon2.add(line8);

    var polygon3 = Polygon();
    polygon3.add(line2);
    polygon3.add(line3);
    polygon3.add(line10);
    polygon3.add(line12);

    var polygon4 = Polygon();
    polygon4.add(line4);
    polygon4.add(line6);
    polygon4.add(line7);
    polygon4.add(line9);

    var polygon5 = Polygon();
    polygon5.add(line5);
    polygon5.add(line6);
    polygon5.add(line10);
    polygon5.add(line11);

    var polygon6 = Polygon();
    polygon6.add(line8);
    polygon6.add(line9);
    polygon6.add(line11);
    polygon6.add(line12);

    var model = Polyhedron();
    model.add(polygon1);
    model.add(polygon2);
    model.add(polygon3);
    model.add(polygon4);
    model.add(polygon5);
    model.add(polygon6);

    return model;
}

fun orthographic_projection(canvas: Canvas, gc: GraphicsContext, model: Polyhedron, ax: Axis) {
    when (ax) {
        Axis.Z -> for (i_p in 0 until model.size) {
            for (i_l in 0 until model[i_p].size) {
                gc.moveTo(model[i_p][i_l].point1.x + canvas.width / 2,
                        model[i_p][i_l].point1.y * (-1) + canvas.height / 2)
                gc.lineTo(model[i_p][i_l].point2.x + canvas.width / 2,
                        model[i_p][i_l].point2.y * (-1) + canvas.height / 2)
                gc.stroke()
            }
        }
        Axis.X -> for (i_p in 0 until model.size) {
            for (i_l in 0 until model[i_p].size) {
                gc.moveTo(model[i_p][i_l].point1.z + canvas.width / 2,
                        model[i_p][i_l].point1.y * (-1) + canvas.height / 2)
                gc.lineTo(model[i_p][i_l].point2.z + canvas.width / 2,
                        model[i_p][i_l].point2.y * (-1) + canvas.height / 2)
                gc.stroke()
            }
        }
        Axis.Y -> for (i_p in 0 until model.size) {
            for (i_l in 0 until model[i_p].size) {
                gc.moveTo(model[i_p][i_l].point1.x + canvas.width / 2,
                        model[i_p][i_l].point1.z * (-1) + canvas.height / 2)
                gc.lineTo(model[i_p][i_l].point2.x + canvas.width / 2,
                        model[i_p][i_l].point2.z * (-1) + canvas.height / 2)
                gc.stroke()
            }
        }
    }
}