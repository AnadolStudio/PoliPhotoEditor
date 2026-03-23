package art.intel.soft.extention

fun Boolean.onTrue(action: () -> Unit) = this.also { isTrue -> if (isTrue) action.invoke() }

fun Boolean?.onTrueOrNull(action: () -> Unit) = this.also { isTrue -> if (isTrue != false) action.invoke() }

fun Boolean.onFalse(action: () -> Unit) = this.also { isTrue -> if (!isTrue) action.invoke() }
