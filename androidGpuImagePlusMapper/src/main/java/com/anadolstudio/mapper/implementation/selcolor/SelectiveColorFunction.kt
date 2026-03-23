package com.anadolstudio.mapper.implementation.selcolor

import com.anadolstudio.mapper.Function
import com.anadolstudio.mapper.FunctionDecorator
import com.anadolstudio.mapper.util.StringConst.SPACE

class SelectiveColorFunction(
    val red: SelectiveColorItem = SelectiveColorItem(),
    val green: SelectiveColorItem = SelectiveColorItem(),
    val blue: SelectiveColorItem = SelectiveColorItem(),
    val cyan: SelectiveColorItem = SelectiveColorItem(),
    val magenta: SelectiveColorItem = SelectiveColorItem(),
    val yellow: SelectiveColorItem = SelectiveColorItem(),
    val white: SelectiveColorItem = SelectiveColorItem(),
    val gray: SelectiveColorItem = SelectiveColorItem(),
    val black: SelectiveColorItem = SelectiveColorItem(),
    function: Function = Function.Empty
) : FunctionDecorator(function) {

    override fun type(): String = "@selcolor"

    override fun getFunctions(): String = super.getFunctions() + convertAllDataToString()

    private fun convertAllDataToString(): String  =
        red.let { "red$it$SPACE" } +
        green.let { "green$it$SPACE" } +
        blue.let { "blue$it$SPACE" } +
        cyan.let { "cyan$it$SPACE" } +
        magenta.let { "magenta$it$SPACE" } +
        yellow.let { "yellow$it$SPACE" } +
        white.let { "white$it$SPACE" } +
        gray.let { "gray$it$SPACE" } +
        black.let { "black$it" }


    data class SelectiveColorItem(
        var cyan: Int = 0,
        var magenta: Int = 0,
        var yellow: Int = 0,
        var black: Int = 0
    ) {
        override fun toString(): String = "($cyan, $magenta, $yellow, $black)"
    }

}
