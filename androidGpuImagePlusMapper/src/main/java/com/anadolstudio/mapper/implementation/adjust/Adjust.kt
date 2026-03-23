package com.anadolstudio.mapper.implementation.adjust

import com.anadolstudio.mapper.Function
import com.anadolstudio.mapper.FunctionDecorator

abstract class Adjust(function: Function = Function.Empty) :FunctionDecorator(function) {

    abstract val subType: String

    override fun type(): String = "@adjust $subType"
}
