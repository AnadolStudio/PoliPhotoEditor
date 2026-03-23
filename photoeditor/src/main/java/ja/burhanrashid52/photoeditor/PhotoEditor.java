package ja.burhanrashid52.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import ja.burhanrashid52.photoeditor.graphic.GraphicBorderActions;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.text.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.view.DrawingView;
import ja.burhanrashid52.photoeditor.view.PhotoEditorView;

public interface PhotoEditor {

    void addImage(Bitmap desiredImage, GraphicBorderActions borderData);

    void addImage(Bitmap desiredImage, String name, GraphicBorderActions borderData);

    DrawingView getDrawingView();

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text              text to display
     * @param colorCodeTextView text color to be displayed
     */
    @SuppressLint("ClickableViewAccessibility")
    void addText(String text, int colorCodeTextView, GraphicBorderActions borderData, int maxWidth);

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param textTypeface      typeface for custom font in the text
     * @param text              text to display
     * @param colorCodeTextView text color to be displayed
     */
    @SuppressLint("ClickableViewAccessibility")
    void addText(@Nullable Typeface textTypeface, String text, int colorCodeTextView, GraphicBorderActions borderData, int maxWidth);

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text         text to display
     * @param styleBuilder text style builder with your style
     */
    @SuppressLint("ClickableViewAccessibility")
    void addText(int id,String text, @Nullable TextStyleBuilder styleBuilder, GraphicBorderActions borderData, int maxWidth);

    /**
     * This will update text and color on provided view
     *
     * @param view      view on which you want update
     * @param inputText text to update {@link TextView}
     * @param colorCode color to update on {@link TextView}
     */
    void editText(@NonNull View view, String inputText, int colorCode);

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param textTypeface update typeface for custom font in the text
     * @param inputText    text to update {@link TextView}
     * @param colorCode    color to update on {@link TextView}
     */
    void editText(@NonNull View view, @Nullable Typeface textTypeface, String inputText, int colorCode);

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param inputText    text to update {@link TextView}
     * @param styleBuilder style to apply on {@link TextView}
     */
    void editText(@NonNull View view, String inputText, @Nullable TextStyleBuilder styleBuilder);

    boolean childIsEmpty();

    /**
     * Enable/Disable drawing mode to draw on {@link PhotoEditorView}
     *
     * @param brushDrawingMode true if mode is enabled
     */
    void setBrushDrawingMode(boolean brushDrawingMode);

    /**
     * @return true is brush mode is enabled
     */
    Boolean getBrushDrawableMode();

    /**
     * Set the size of brush user want to paint on canvas i.e {@link DrawingView}
     * @deprecated use {@code setShape} of a ShapeBuilder
     *
     * @param size size of brush
     */
    @Deprecated
    void setBrushSize(float size);

    /**
     * set opacity/transparency of brush while painting on {@link DrawingView}
     * @deprecated use {@code setShape} of a ShapeBuilder
     *
     * @param opacity opacity is in form of percentage
     */
    @Deprecated
    void setOpacity(@IntRange(from = 0, to = 100) int opacity);

    /**
     * set brush color which user want to paint
     * @deprecated use {@code setShape} of a ShapeBuilder
     *
     * @param color color value for paint
     */
    @Deprecated
    void setBrushColor(@ColorInt int color);

    /**
     * set the eraser size
     * <b>Note :</b> Eraser size is different from the normal brush size
     *
     * @param brushEraserSize size of eraser
     */
    void setBrushEraserSize(float brushEraserSize);

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushEraserSize(float)
     */
    float getEraserSize();

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushSize(float)
     */
    float getBrushSize();

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushColor(int)
     */
    int getBrushColor();

    /**
     * <p>
     * Its enables eraser mode after that whenever user drags on screen this will erase the existing
     * paint
     * <br>
     * <b>Note</b> : This eraser will work on paint views only
     * <p>
     */
    void brushEraser();

    /**
     * Undo the last operation perform on the {@link PhotoEditor}
     *
     * @return true if there nothing more to undo
     */
    boolean undo();

    /**
     * Redo the last operation perform on the {@link PhotoEditor}
     *
     * @return true if there nothing more to redo
     */
    boolean redo();

    /**
     * Removes all the edited operations performed {@link PhotoEditorView}
     * This will also clear the undo and redo stack
     */
    void clearAllViews();

    /**
     * Remove all helper boxes from views
     */
    @UiThread
    void clearHelperBox();

    /**
     * Callback on editing operation perform on {@link PhotoEditorView}
     *
     * @param onPhotoEditorListener {@link OnPhotoEditorListener}
     */
    void setOnPhotoEditorListener(@NonNull OnPhotoEditorListener onPhotoEditorListener);

    /**
     * Check if any changes made need to save
     *
     * @return true if nothing is there to change
     */
    boolean isCacheEmpty();

    /**
     * Builder pattern to define {@link PhotoEditor} Instance
     */
    class Builder {

        Context context;
        PhotoEditorView parentView;
        ImageView imageView;
        View deleteView;
        DrawingView drawingView;
        Typeface textTypeface;
        Typeface emojiTypeface;
        // By default, pinch-to-scale is enabled for text
        boolean isTextPinchScalable = true;
        boolean clipSourceImage = false;

        /**
         * Building a PhotoEditor which requires a Context and PhotoEditorView
         * which we have setup in our xml layout
         *
         * @param context         context
         * @param photoEditorView {@link PhotoEditorView}
         */
        public Builder(Context context, PhotoEditorView photoEditorView) {
            this.context = context;
            parentView = photoEditorView;
            imageView = photoEditorView.getSource();
            drawingView = photoEditorView.getDrawingView();
        }

        Builder setDeleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }

        /**
         * set default text font to be added on image
         *
         * @param textTypeface typeface for custom font
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setDefaultTextTypeface(Typeface textTypeface) {
            this.textTypeface = textTypeface;
            return this;
        }

        /**
         * set default font specific to add emojis
         *
         * @param emojiTypeface typeface for custom font
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setDefaultEmojiTypeface(Typeface emojiTypeface) {
            this.emojiTypeface = emojiTypeface;
            return this;
        }

        /**
         * Set false to disable pinch-to-scale for text inserts.
         * Set to "true" by default.
         *
         * @param isTextPinchScalable flag to make pinch to zoom for text inserts.
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setPinchTextScalable(boolean isTextPinchScalable) {
            this.isTextPinchScalable = isTextPinchScalable;
            return this;
        }

        /**
         * @return build PhotoEditor instance
         */
        public PhotoEditor build() {
            return new PhotoEditorImpl(this);
        }

        /**
         * Set true true to clip the drawing brush to the source image.
         *
         * @param clip a boolean to indicate if brush drawing is clipped or not.
         */
        public Builder setClipSourceImage(boolean clip) {
            this.clipSourceImage = clip;
            return this;
        }
    }


    /**
     * A callback to save the edited image asynchronously
     */
    interface OnSaveListener {

        /**
         * Call when edited image is saved successfully on given path
         *
         * @param imagePath path on which image is saved
         */
        void onSuccess(@NonNull String imagePath);

        /**
         * Call when failed to saved image on given path
         *
         * @param exception exception thrown while saving image
         */
        void onFailure(@NonNull Exception exception);
    }


    // region Shape
    /**
     * Update the current shape to be drawn,
     * through the use of a ShapeBuilder.
     */
    void setShape(ShapeBuilder shapebuilder);
    // endregion

}
