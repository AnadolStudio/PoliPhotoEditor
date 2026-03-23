package art.intel.soft.ui.edit.improve

import com.anadolstudio.mapper.Function
import com.anadolstudio.mapper.util.StringConst
import java.util.TreeMap

class ImproveContainer : Function {

    enum class FunctionMode {
        NONE,
        CURVE,
        SELECT_COLOR,
        BRIGHTNESS,
        CONTRAST,
        SATURATION,
        WARMTH;
    }

    private var treeMap: TreeMap<Int, FunctionWrapper> = TreeMap()
    private var tempTreeMap: TreeMap<Int, FunctionWrapper> = TreeMap()

    fun clear() {
        treeMap.clear()
        clearTemp()
    }

    fun clearTemp() {
        tempTreeMap.clear()
    }

    fun apply() {
        tempTreeMap.forEach { (ordinal, function) -> treeMap[ordinal] = function }
        clearTemp()
    }

    fun putTemp(mode: FunctionMode, function: FunctionWrapper) {
        if (mode == FunctionMode.NONE) return

        val ordinal = mode.ordinal
        tempTreeMap[ordinal] = function
    }

    fun remove(mode: FunctionMode) {
        if (mode == FunctionMode.NONE) return

        val ordinal = mode.ordinal
        treeMap.remove(ordinal)
    }

    fun getValue(mode: FunctionMode): Float? = treeMap[mode.ordinal]?.value

    fun getFunction(mode: FunctionMode): Function? = treeMap[mode.ordinal]?.function

    override fun getFunctions(): String = mutableSetOf<Int>()
            .apply {
                addAll(treeMap.keys)
                addAll(tempTreeMap.keys)
            }
            .sorted()
            .mapNotNull { key -> tempTreeMap[key] ?: treeMap[key] }
            .map { functionWrapper -> functionWrapper.function }
            .joinToString(StringConst.SPACE) { function -> function.getFunctions() }

    data class FunctionWrapper(
            val value: Float? = null,
            val function: Function // TODO add apply (boolean) field
    )
}
