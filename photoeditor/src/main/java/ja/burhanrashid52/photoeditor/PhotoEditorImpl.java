package ja.burhanrashid52.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ja.burhanrashid52.photoeditor.brush.BrushDrawingStateListener;
import ja.burhanrashid52.photoeditor.gesture.MultiTouchListener;
import ja.burhanrashid52.photoeditor.graphic.Graphic;
import ja.burhanrashid52.photoeditor.graphic.GraphicBorderActions;
import ja.burhanrashid52.photoeditor.graphic.GraphicManager;
import ja.burhanrashid52.photoeditor.graphic.Image;
import ja.burhanrashid52.photoeditor.graphic.Text;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.text.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.util.BoxHelper;
import ja.burhanrashid52.photoeditor.view.DrawingView;
import ja.burhanrashid52.photoeditor.view.PhotoEditorImageViewListener;
import ja.burhanrashid52.photoeditor.view.PhotoEditorView;

/**
 * <p>
 * This class in initialize by {@link PhotoEditor.Builder} using a builder pattern with multiple
 * editing attributes
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 18/01/2017
 */
public class PhotoEditorImpl implements PhotoEditor {

    private static final String TAG = "PhotoEditor";
    private static final float DEFAULT_SCALE = 2F;
    private final PhotoEditorView parentView;
    private final PhotoEditorViewState viewState;
    private final ImageView imageView;
    private final View deleteView;
    private final DrawingView drawingView;
    private final BrushDrawingStateListener mBrushDrawingStateListener;
    private final BoxHelper mBoxHelper;
    private final boolean isTextPinchScalable;
    private final Typeface mDefaultTextTypeface;
    private final Typeface mDefaultEmojiTypeface;
    private final GraphicManager mGraphicManager;
    private final Context context;
    private OnPhotoEditorListener mOnPhotoEditorListener;

    @SuppressLint("ClickableViewAccessibility")
    protected PhotoEditorImpl(Builder builder) {
        this.context = builder.context;
        this.parentView = builder.parentView;
        this.imageView = builder.imageView;
        this.deleteView = builder.deleteView;
        this.drawingView = builder.drawingView;
        this.isTextPinchScalable = builder.isTextPinchScalable;
        this.mDefaultTextTypeface = builder.textTypeface;
        this.mDefaultEmojiTypeface = builder.emojiTypeface;

        this.viewState = new PhotoEditorViewState();
        this.mGraphicManager = new GraphicManager(builder.parentView, this.viewState);
        this.mBoxHelper = new BoxHelper(builder.parentView, this.viewState);

        mBrushDrawingStateListener = new BrushDrawingStateListener(builder.parentView, this.viewState);
        this.drawingView.setBrushViewChangeListener(mBrushDrawingStateListener);

        final GestureDetector mDetector = new GestureDetector(
                context,
                new PhotoEditorImageViewListener(
                        this.viewState,
                        this::clearHelperBox
                )
        );

        imageView.setOnTouchListener((v, event) -> {
            if (mOnPhotoEditorListener != null) {
                mOnPhotoEditorListener.onTouchSourceImage(event);
            }
            return mDetector.onTouchEvent(event);
        });

        this.parentView.setClipSourceImage(builder.clipSourceImage);
    }

    public PhotoEditorViewState getViewState() {
        return viewState;
    }

    public View getCurrentSelectedView() {
        return viewState.getCurrentSelectedView();
    }

    @Override
    public void addImage(Bitmap desiredImage, GraphicBorderActions borderData) {
        addImage(desiredImage, null, borderData);
    }

    @Override
    public void addImage(final Bitmap desiredImage, final String name, final GraphicBorderActions borderData) {
        MultiTouchListener multiTouchListener = getMultiTouchListener(true);
        Image image = new Image(parentView, multiTouchListener, viewState, mGraphicManager, borderData, name);
        image.buildView(desiredImage);
        addToEditor(image);
    }

    @Override
    public DrawingView getDrawingView() {
        return drawingView;
    }

    @Override
    public void addText(String text, final int colorCodeTextView, GraphicBorderActions borderData, int maxWidth) {
        addText(null, text, colorCodeTextView, borderData, maxWidth);
    }

    @Override
    public void addText(@Nullable Typeface textTypeface, String text, final int colorCodeTextView, GraphicBorderActions borderData, int maxWidth) {
        final TextStyleBuilder styleBuilder = new TextStyleBuilder();

        styleBuilder.withTextColor(colorCodeTextView);
        if (textTypeface != null) {
            styleBuilder.withTextFont(textTypeface);
        }

        addText(View.generateViewId(), text, styleBuilder, borderData, maxWidth);
    }

    @Override
    public void addText(int id, String text, @Nullable TextStyleBuilder styleBuilder, GraphicBorderActions borderData, int maxWidth) {
        drawingView.enableDrawing(false);
        MultiTouchListener multiTouchListener = getMultiTouchListener(isTextPinchScalable);

        Text textGraphic = new Text(id, parentView, multiTouchListener, viewState, mDefaultTextTypeface, mGraphicManager, borderData, maxWidth);
        textGraphic.buildView(text, styleBuilder);
        addToEditor(textGraphic);

        textGraphic.getRootView().post(() -> textGraphic.setScale(DEFAULT_SCALE, textGraphic.getRootView()));
    }

    @Override
    public void editText(@NonNull View view, String inputText, int colorCode) {
        editText(view, null, inputText, colorCode);
    }

    @Override
    public void editText(@NonNull View view, @Nullable Typeface textTypeface, String inputText, int colorCode) {
        final TextStyleBuilder styleBuilder = new TextStyleBuilder();
        styleBuilder.withTextColor(colorCode);
        if (textTypeface != null) {
            styleBuilder.withTextFont(textTypeface);
        }

        editText(view, inputText, styleBuilder);
    }

    @Override
    public void editText(@NonNull View view, String inputText, @Nullable TextStyleBuilder styleBuilder) {
        TextView inputTextView = view.findViewById(R.id.textView);
        if (inputTextView != null && viewState.containsAddedView(view) && !TextUtils.isEmpty(inputText)) {
            inputTextView.setText(inputText);
            if (styleBuilder != null) {
                styleBuilder.applyStyle(inputTextView);
            }
            Log.d("TextEditFragment", "updateCurrentTextView: editText2");
            mGraphicManager.updateView(view);
        }
    }

    private void addToEditor(Graphic graphic) {
        clearHelperBox();
        mGraphicManager.addView(graphic);
        // Change the in-focus view
        viewState.setCurrentSelectedView(graphic.getRootView());
        mGraphicManager.onChangeView(graphic.getRootView());
    }

    @Override
    public boolean childIsEmpty(){
        return mGraphicManager.childCount() == 0;
    }

    /**
     * Create a new instance and scalable touchview
     *
     * @param isPinchScalable true if make pinch-scalable, false otherwise.
     * @return scalable multitouch listener
     */
    @NonNull
    private MultiTouchListener getMultiTouchListener(final boolean isPinchScalable) {
        return new MultiTouchListener(
                deleteView,
                parentView,
                this.imageView,
                isPinchScalable,
                mOnPhotoEditorListener,
                this.viewState);
    }

    @Override
    public void setBrushDrawingMode(boolean brushDrawingMode) {
        if (drawingView != null) {
            drawingView.enableDrawing(brushDrawingMode);
        }
    }

    @Override
    public Boolean getBrushDrawableMode() {
        return drawingView != null && drawingView.isDrawingEnabled();
    }

    @Override
    public void setOpacity(@IntRange(from = 0, to = 100) int opacity) {
        if (drawingView != null && drawingView.getCurrentShapeBuilder() != null) {
            opacity = (int) ((opacity / 100.0) * 255.0);
            drawingView.getCurrentShapeBuilder().withShapeOpacity(opacity);
        }
    }

    @Override
    public float getBrushSize() {
        if (drawingView != null && drawingView.getCurrentShapeBuilder() != null) {
            return drawingView.getCurrentShapeBuilder().getShapeSize();
        }
        return 0;
    }

    @Override
    public void setBrushSize(float size) {
        if (drawingView != null && drawingView.getCurrentShapeBuilder() != null) {
            drawingView.getCurrentShapeBuilder().withShapeSize(size);
        }
    }

    @Override
    public int getBrushColor() {
        if (drawingView != null && drawingView.getCurrentShapeBuilder() != null) {
            return drawingView.getCurrentShapeBuilder().getShapeColor();
        }
        return 0;
    }

    @Override
    public void setBrushColor(@ColorInt int color) {
        if (drawingView != null && drawingView.getCurrentShapeBuilder() != null) {
            drawingView.getCurrentShapeBuilder().withShapeColor(color);
        }
    }

    @Override
    public void setBrushEraserSize(float brushEraserSize) {
        if (drawingView != null) {
            drawingView.setBrushEraserSize(brushEraserSize);
        }
    }

    @Override
    public float getEraserSize() {
        return drawingView != null ? drawingView.getEraserSize() : 0;
    }

    @Override
    public void brushEraser() {
        if (drawingView != null) {
            drawingView.brushEraser();
        }
    }

    @Override
    public boolean undo() {
        return mGraphicManager.undoView();
    }

    @Override
    public boolean redo() {
        return mGraphicManager.redoView();
    }

    @Override
    public void clearAllViews() {
        mBoxHelper.clearAllViews(drawingView);
        drawingView.setDefaultBitmap(null);
    }

    @Override
    public void clearHelperBox() {
        mBoxHelper.clearHelperBox();
    }

    @Override
    public void setOnPhotoEditorListener(@NonNull OnPhotoEditorListener onPhotoEditorListener) {
        this.mOnPhotoEditorListener = onPhotoEditorListener;
        mGraphicManager.setOnPhotoEditorListener(mOnPhotoEditorListener);
        mBrushDrawingStateListener.setOnPhotoEditorListener(mOnPhotoEditorListener);
    }

    @Override
    public boolean isCacheEmpty() {
        return viewState.getAddedViewsCount() == 0 && viewState.getRedoViewsCount() == 0;
    }

    // region Shape
    @Override
    public void setShape(ShapeBuilder shapeBuilder) {
        drawingView.setShapeBuilder(shapeBuilder);
    }
    // endregion

}
