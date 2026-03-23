package com.anadolstudio.mapper

import android.graphics.Point
import com.anadolstudio.mapper.implementation.adjust.BrightnessFunction
import com.anadolstudio.mapper.implementation.adjust.ContrastFunction
import com.anadolstudio.mapper.implementation.adjust.SaturationFunction
import com.anadolstudio.mapper.implementation.adjust.WhiteBalanceFunction
import com.anadolstudio.mapper.implementation.curve.CurveFunction
import com.anadolstudio.mapper.implementation.selcolor.SelectiveColorFunction
import org.junit.Assert.assertEquals
import org.junit.Test

class FunctionStringConvertTest {
    @Test
    fun curveFunctionTest() {
        val func = CurveFunction(
            rgb = listOf(Point(1, 1)),
            r = listOf(Point(1, 1)),
            g = listOf(Point(1, 1)),
            b = listOf(Point(1, 1)),
            function = Function.Empty
        ).getFunctions()

        assertEquals("@curve RGB (0, 0) R (0, 0) G (0, 0) B (0, 0) ", func)
    }

    @Test
    fun whiteBalanceFunctionTest() {
        val func: String = WhiteBalanceFunction(
            1F,
            2.5F,
            Function.Empty
        ).getFunctions()

        assertEquals("@adjust whitebalance 1.0 2.5", func)
    }

    @Test
    fun contrastFunctionTest() {
        val func: String = ContrastFunction(
            1F,
            Function.Empty
        ).getFunctions()

        assertEquals("@adjust contrast 1.0", func)
    }

    @Test
    fun saturationFunctionTest() {
        val func: String = SaturationFunction(
            1F,
            Function.Empty
        ).getFunctions()

        assertEquals("@adjust saturation 1.0", func)
    }

    @Test
    fun brightnessFunctionTest() {
        val func: String = BrightnessFunction(
            1F,
            Function.Empty
        ).getFunctions()

        assertEquals("@adjust brightness 1.0", func)
    }

    @Test
    fun selColorFunctionTest() {
        assertEquals(
            "@selcolor red(0, 0, 0, 0) green(0, 0, 0, 0) blue(0, 0, 0, 0) cyan(0, 0, 0, 0) magenta(0, 0, 0, 0) yellow(0, 0, 0, 0) white(0, 0, 0, 0) gray(0, 0, 0, 0) black(0, 0, 0, 0)",
            SelectiveColorFunction(function = Function.Empty).getFunctions()
        )
    }

}
