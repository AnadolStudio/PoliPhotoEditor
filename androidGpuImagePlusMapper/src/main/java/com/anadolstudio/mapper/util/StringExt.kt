package com.anadolstudio.mapper.util

fun String.ifNotEmptyAdd(action: () -> String): String = if (isNotEmpty()) plus(action.invoke()) else this

fun String.ifNotEmptyAddPrefix(prefix: String): String = if (isNotEmpty()) "$prefix${this}" else this

fun String.nullIfEmpty(): String? = ifEmpty { null }

