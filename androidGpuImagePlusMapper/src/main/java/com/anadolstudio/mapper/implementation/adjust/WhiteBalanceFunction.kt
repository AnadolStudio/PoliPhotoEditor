package com.anadolstudio.mapper.implementation.adjust

import com.anadolstudio.mapper.Function
import com.anadolstudio.mapper.util.StringConst.SPACE

open class WhiteBalanceFunction(
    protected val temperature: Float,
    protected val tint: Float,
    function: Function = Function.Empty
) : Adjust(function) {

    override val subType: String = "whitebalance"

    override fun getFunctions(): String = "${super.getFunctions()}$temperature$SPACE$tint"

}
