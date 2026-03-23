package art.intel.soft.extention

fun Float.correctInDiapason(min: Float, max: Float): Float = when {
    this > max -> max
    this < min -> min
    else -> this
}

fun Int.correctInDiapason(min: Int, max: Int): Int = when {
    this > max -> max
    this < min -> min
    else -> this
}
