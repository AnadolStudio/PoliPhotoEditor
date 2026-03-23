package com.anadolstudio.mapper

import com.anadolstudio.mapper.util.StringConst.SPACE
import com.anadolstudio.mapper.util.ifNotEmptyAdd

abstract class FunctionDecorator(private val function: Function) : Function {

    abstract fun type(): String

    override fun getFunctions(): String =
        function.getFunctions().ifNotEmptyAdd { SPACE } + type() + SPACE

}
