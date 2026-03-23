package com.anadolstudio.mapper.implementation.adjust

import com.anadolstudio.mapper.Function

class BrightnessFunction(
    private val brightness: Float,
    function: Function = Function.Empty
) : Adjust(function) {

    override val subType: String = "brightness"

    override fun getFunctions(): String = "${super.getFunctions()}$brightness"

}
