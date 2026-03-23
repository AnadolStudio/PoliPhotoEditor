package ja.burhanrashid52.photoeditor.graphic;

import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditorViewState;
import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.gesture.MultiTouchListener;
import ja.burhanrashid52.photoeditor.text.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.util.ViewExtKt;
import ja.burhanrashid52.photoeditor.view.BorderView;
import ja.burhanrashid52.photoeditor.view.MaxSize;

public class Text extends Graphic {

    public static final float MIN_SCALE = 0.5F;

    private final MultiTouchListener mMultiTouchListener;
    private final Typeface mDefaultTextTypeface;
    private final GraphicManager mGraphicManager;
    private final ViewGroup mPhotoEditorView;
    private final PhotoEditorViewState mViewState;
    private final GraphicBorderActions graphicBorderActions;
    private final int maxWidth;
    private final int id;
    private TextView mTextView;

    public Text(int id,
                ViewGroup photoEditorView,
                MultiTouchListener multiTouchListener,
                PhotoEditorViewState viewState,
                Typeface defaultTextTypeface,
                GraphicManager graphicManager,
                GraphicBorderActions graphicBorderActions,
                int maxWidth
    ) {
        super(photoEditorView.getContext(), graphicManager);
        this.id = id;
        mPhotoEditorView = photoEditorView;
        mViewState = viewState;
        mMultiTouchListener = multiTouchListener;
        mDefaultTextTypeface = defaultTextTypeface;
        mGraphicManager = graphicManager;
        this.graphicBorderActions = graphicBorderActions;
        this.maxWidth = maxWidth;

        setupGesture();
    }

    public void buildView(String text, TextStyleBuilder styleBuilder) {
        mTextView.setText(text);
        if (styleBuilder != null) styleBuilder.applyStyle(mTextView);
        View rootView = getRootView();
        rootView.setId(id);

        BorderView borderView = (BorderView) rootView.findViewById(R.id.frmBorder);

        if (rootView instanceof MaxSize) {
            ((MaxSize) rootView).setMaxSize(maxWidth);
        }


        if (borderView != null) {
            borderView.needShowSupportCircle(false);
            borderView.setBorderData(graphicBorderActions.toBorderData(this));
            borderView.setActionClear(() -> {
                        lastButtonPoint = null;

                        return null;
                    }
            );
        }
    }

    private void setupGesture() {
        MultiTouchListener.OnGestureControl onGestureControl = buildGestureController(mPhotoEditorView, mViewState);
        mMultiTouchListener.setOnGestureControl(onGestureControl);
        View rootView = getRootView();
        rootView.setOnTouchListener(mMultiTouchListener);
    }

    @Override
    public  ViewType getViewType() {
        return ViewType.TEXT;
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_photo_editor_text;
    }

    @Override
    public void setupView(View rootView) {
        mTextView = rootView.findViewById(R.id.textView);

        if (mTextView != null && mDefaultTextTypeface != null) {
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setTypeface(mDefaultTextTypeface);
        }
    }

    @Override
    void updateView(View view) {
        mViewState.setCurrentSelectedView(view);
        toggleSelection();

        String textInput = mTextView.getText().toString();
        int currentTextColor = mTextView.getCurrentTextColor();
        OnPhotoEditorListener photoEditorListener = mGraphicManager.getOnPhotoEditorListener();

        if (photoEditorListener != null) {
            photoEditorListener.onEditTextChangeListener(view, textInput, currentTextColor);
        }
    }

    @Override
    protected float minScale() {
        return MIN_SCALE;
    }

    @Override
    public void scale(final float x, final float y) {
        super.scale(x, y);
    }

    @Override
    public void changeSize(final float x, final float y) {
        PointF lastPoint = lastButtonPoint;
        PointF currentPoint = correctCoordinate(x, y);

        if (lastPoint == null) {
            lastButtonPoint = currentPoint;

            return;
        }

        View rootView = getRootView();

        ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();

        int delta = getDelta(currentPoint, lastPoint);

        if (rootView.getWidth() + delta > maxWidth) {
            delta = maxWidth - layoutParams.width;
        }
        if (rootView.getWidth() + delta < minWidth()) {
            delta = layoutParams.width - minWidth();
        }

        layoutParams.width = rootView.getWidth() + delta;
        rootView.requestLayout();

        this.lastButtonPoint = currentPoint;
    }

    private int getDelta(PointF currentPoint, PointF lastPoint) {

        float currentRadius = (float) Math.sqrt(currentPoint.x * currentPoint.x + currentPoint.y * currentPoint.y);
        float previousRadius = (float) Math.sqrt(lastPoint.x * lastPoint.x + lastPoint.y * lastPoint.y);

        return (int) ((currentRadius - previousRadius) * 2);
    }

    private int minWidth() {
        float scale = getRootView().getScaleX();

        return (int) (ViewExtKt.dpToPx(TextStyleBuilder.DEFAULT_SIZE) + 2 * ViewExtKt.dpToPx(24) / scale);
    }

    private PointF correctCoordinate(final float x, final float y) {
        float mathCoordinateX = Math.max(0, x - getRootView().getWidth() / 2F);
        float mathCoordinateY = Math.max(0, y - getRootView().getHeight() / 2F);


        return new PointF(mathCoordinateX, mathCoordinateY);
    }
}
