package ja.burhanrashid52.photoeditor.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import ja.burhanrashid52.photoeditor.R;

/**
 * <p>
 * This ViewGroup will have the {@link DrawingView} to draw paint on it with {@link ImageView}
 * which our source image
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 1/18/2018
 */

public class PhotoEditorView extends RelativeLayout {

    private static final String TAG = "PhotoEditorView";
    private static final int imgSrcId = 1, shapeSrcId = 2, glFilterId = 3;
    private SourceImageView mImgSource;
    private DrawingView mDrawingView;
    private boolean clipSourceImage;

    public PhotoEditorView(Context context) {
        super(context);
        init(null);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mDrawingView.setOnTouchListener(
                l == null ? null : this,
                l);
        super.setOnTouchListener(l);
    }

    private void init(@Nullable AttributeSet attrs) {
        //Setup image attributes
        mImgSource = new SourceImageView(getContext());
        RelativeLayout.LayoutParams sourceParam = setupImageSource(attrs);

        mImgSource.setOnImageChangedListener(sourceBitmap -> {
            Log.d(TAG, "onBitmapLoaded() called with: sourceBitmap = [" + sourceBitmap + "]");
        });

        //Setup drawing view
        mDrawingView = new DrawingView(getContext());
        RelativeLayout.LayoutParams brushParam = setupDrawingView();

        //Add image source
        addView(mImgSource, sourceParam);

        //Add brush view
        addView(mDrawingView, brushParam);
    }


    @SuppressLint("Recycle")
    private RelativeLayout.LayoutParams setupImageSource(@Nullable AttributeSet attrs) {
        mImgSource.setId(imgSrcId);
        mImgSource.setAdjustViewBounds(true);
        mImgSource.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PhotoEditorView);
            Drawable imgSrcDrawable = a.getDrawable(R.styleable.PhotoEditorView_photo_src);
            if (imgSrcDrawable != null) {
                mImgSource.setImageDrawable(imgSrcDrawable);
            }
        }

        int widthParam = ViewGroup.LayoutParams.MATCH_PARENT;
        if (clipSourceImage) {
            widthParam = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                widthParam, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        return params;
    }


    private RelativeLayout.LayoutParams setupDrawingView() {
        mDrawingView.setVisibility(GONE);
        mDrawingView.setId(shapeSrcId);

        // Align drawing view to the size of image view
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_TOP, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_LEFT, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_RIGHT, imgSrcId);
        return params;
    }

    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    public ImageView getSource() {
        return mImgSource;
    }

    public DrawingView getDrawingView() {
        return mDrawingView;
    }

    public void setClipSourceImage(boolean clip) {
        clipSourceImage = clip;
        RelativeLayout.LayoutParams param = setupImageSource(null);
        mImgSource.setLayoutParams(param);
    }
    // endregion
}
