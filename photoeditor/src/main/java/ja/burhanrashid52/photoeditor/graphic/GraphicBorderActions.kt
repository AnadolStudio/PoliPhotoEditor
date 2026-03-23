package ja.burhanrashid52.photoeditor.graphic

import ja.burhanrashid52.photoeditor.view.BorderView

data class GraphicBorderActions(
        val leftTopButton: GraphicBorderData? = null,
        val rightTopButton: GraphicBorderData? = null,
        val leftBottomButton: GraphicBorderData? = null,
        val rightBottomButton: GraphicBorderData? = null,
        val leftMiddleButton: GraphicBorderData? = null,
        val topButton: GraphicBorderData? = null,
        val rightMiddleButton: GraphicBorderData? = null,
        val bottomButton: GraphicBorderData? = null,
) {
    fun toBorderData(graphic: Graphic): BorderView.BorderData = BorderView.BorderData(
            leftTopButton = leftTopButton?.toButtonsData(graphic),
            rightTopButton = rightTopButton?.toButtonsData(graphic),
            leftBottomButton = leftBottomButton?.toButtonsData(graphic),
            rightBottomButton = rightBottomButton?.toButtonsData(graphic),
            leftMiddleButton = leftMiddleButton?.toButtonsData(graphic),
            topButton = topButton?.toButtonsData(graphic),
            rightMiddleButton = rightMiddleButton?.toButtonsData(graphic),
            bottomButton = bottomButton?.toButtonsData(graphic),
    )

    data class GraphicBorderData(val drawableId: Int, val isMovable: Boolean, val action: ((graphic: Graphic, x: Float, y: Float) -> Unit)) {

        fun toButtonsData(graphic: Graphic): BorderView.ButtonsData = BorderView.ButtonsData(
                drawableId = drawableId,
                isMovable = isMovable,
                action = { x, y -> action.invoke(graphic, x, y) }
        )
    }

}
