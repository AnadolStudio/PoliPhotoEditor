package com.anadolstudio.mapper.implementation.curve

import android.graphics.Point
import com.anadolstudio.mapper.Function
import com.anadolstudio.mapper.FunctionDecorator
import com.anadolstudio.mapper.util.StringConst.SPACE

class CurveFunction(
    val rgb: List<Point>,
    val r: List<Point>,
    val g: List<Point>,
    val b: List<Point>,
    function: Function = Function.Empty
) : FunctionDecorator(function) {

    override fun type(): String = "@curve"

    override fun getFunctions(): String = super.getFunctions() + convertAllPointsToString()

    private fun convertAllPointsToString(): String {
        var result = ""

        if (rgb.isNotEmpty()) result += "RGB" + convertPointsToString(rgb)
        if (r.isNotEmpty()) result += "R" + convertPointsToString(r)
        if (g.isNotEmpty()) result += "G" + convertPointsToString(g)
        if (b.isNotEmpty()) result += "B" + convertPointsToString(b)

        return result
    }


    private fun convertPointsToString(points: List<Point>): String = points.joinToString(
        separator = SPACE,
        prefix = SPACE,
        postfix = SPACE,
        transform = this::pointValueToString
    )

    private fun pointValueToString(point: Point): String = "(${point.x}, ${point.y})"
}
