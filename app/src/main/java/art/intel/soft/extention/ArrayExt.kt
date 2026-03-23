package art.intel.soft.extention

fun <T> Array<T>.nullIfEmpty(): Array<T>? = ifEmpty { null }
