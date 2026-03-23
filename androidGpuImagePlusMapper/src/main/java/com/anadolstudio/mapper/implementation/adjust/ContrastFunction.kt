package com.anadolstudio.mapper.implementation.adjust

import com.anadolstudio.mapper.Function

class ContrastFunction(
    private val contrast: Float,
    function: Function = Function.Empty
) : Adjust(function) {

    override val subType: String = "contrast"

    override fun getFunctions(): String = "${super.getFunctions()}$contrast"

}
