package art.intel.soft.ui.edit;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import kotlin.Deprecated;

@Deprecated(message = "Переписать на Kotlin либо удалить")
public abstract class BrushBaseEditFragment extends BaseEditFragment {
    protected static final String BRUSH_MODE = "brush_mode";
    protected static final float XSMALL = 5.0f;
    protected static final float XLARGE = 100.0f;
    protected static final float NORMAL = (XLARGE - XSMALL) / 2;
    protected float currentSize;
    protected boolean isBrush;
    protected int color;

    protected void setupBrush() {
        setupBrush(currentSize);
    }

    protected void setupBrush(float size) {
        currentSize = size;

        PhotoEditor photoEditor = editor().getPhotoEditor();
        photoEditor.setBrushDrawingMode(true);
        ShapeBuilder builder = new ShapeBuilder()
                .withShapeColor(color)
                .withShapeSize(currentSize);
        photoEditor.setShape(builder);
        if (!isBrush) {
            photoEditor.brushEraser();
        }
    }

    protected void setBrushMode(boolean isBrush) {
        this.isBrush = isBrush;
    }
}
