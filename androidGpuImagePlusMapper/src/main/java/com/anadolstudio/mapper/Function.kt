package com.anadolstudio.mapper

interface Function {

    fun getFunctions(): String

    object Empty : Function {

        override fun getFunctions(): String = ""
    }

}
