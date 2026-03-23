package com.anadolstudio.mapper.implementation.adjust

import com.anadolstudio.mapper.Function

class SaturationFunction(
    private val saturation: Float,
    function: Function = Function.Empty
) : Adjust(function) {

    override val subType: String = "saturation"

    override fun getFunctions(): String = "${super.getFunctions()}$saturation"

}
