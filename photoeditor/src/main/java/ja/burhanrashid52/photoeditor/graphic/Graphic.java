package ja.burhanrashid52.photoeditor.graphic;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ja.burhanrashid52.photoeditor.PhotoEditorViewState;
import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.gesture.MultiTouchListener;
import ja.burhanrashid52.photoeditor.util.BoxHelper;
import ja.burhanrashid52.photoeditor.view.Scalable;

public abstract class Graphic {

    protected PointF lastButtonPoint = null;

    private final View mRootView;

    private final GraphicManager mGraphicManager;

    public abstract ViewType getViewType();

    public abstract int getLayoutId();

    public abstract void setupView(View rootView);

    void updateView(View view) {
        //Optional for subclass to override
    }

    public Graphic(Context context, GraphicManager graphicManager) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (getLayoutId() == 0) {
            throw new UnsupportedOperationException("Layout id cannot be zero. Please define a layout");
        }
        mRootView = layoutInflater.inflate(getLayoutId(), null);
        mGraphicManager = graphicManager;
        setupView(mRootView);
        setupRemoveView(mRootView);
    }

    public Graphic(View rootView, GraphicManager graphicManager) {
        mRootView = rootView;
        mGraphicManager = graphicManager;
        setupView(mRootView);
        setupRemoveView(mRootView);
    }

    private void setupRemoveView(final View rootView) {
        //We are setting tag as ViewType to identify what type of the view it is
        //when we remove the view from stack i.e onRemoveViewListener(ViewType viewType, int numberOfAddedViews);
        final ViewType viewType = getViewType();
        rootView.setTag(viewType);
    }

    public void remove() {
        mGraphicManager.removeView(Graphic.this);
    }

    protected void toggleSelection() {
        mGraphicManager.onChangeView(mRootView);

        View frmBorder = mRootView.findViewById(R.id.frmBorder);

        if (frmBorder != null) {
            frmBorder.setVisibility(View.VISIBLE);
            frmBorder.setTag(true);
        }
    }

    protected MultiTouchListener.OnGestureControl buildGestureController(final ViewGroup viewGroup, final PhotoEditorViewState viewState) {
        final BoxHelper boxHelper = new BoxHelper(viewGroup, viewState);
        return new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                boxHelper.clearHelperBox();
                toggleSelection();
                // Change the in-focus view
                viewState.setCurrentSelectedView(mRootView);

            }

            @Override
            public void onLongClick() {
                updateView(mRootView);
            }
        };
    }

    public View getRootView() {
        return mRootView;
    }

    private PointF correctCoordinate(float x, float y) {
        float mathCoordinateX = getRootView().getScaleY() * (x - getRootView().getWidth() / 2f);
        float mathCoordinateY = getRootView().getScaleY() * (y - getRootView().getHeight() / 2f);

        return new PointF(mathCoordinateX, mathCoordinateY);
    }

    public void flip(){

    }

    public void changeSize(float x, float y){

    }

    public void scale(float x, float y) {
        PointF lastPoint = lastButtonPoint;
        PointF currentPoint = correctCoordinate(x, y);

        if (lastPoint == null) {
            this.lastButtonPoint = currentPoint;

            return;
        }

        float currentRadius = (float) Math.sqrt(currentPoint.x * currentPoint.x + currentPoint.y * currentPoint.y);
        float previousRadius = (float) Math.sqrt(lastPoint.x * lastPoint.x + lastPoint.y * lastPoint.y);
        float delta = (currentRadius / previousRadius);

        View root = getRootView();

        if (root.getScaleX() * delta > maxScale()) {
            setScale(maxScale(), root);

            return;
        }

        if (root.getScaleX() * delta < minScale()) {
            setScale(minScale(), root);

            return;
        }

        this.lastButtonPoint = currentPoint;
        setScale(root.getScaleX() * delta, root);
    }

    public void setScale(final float scale, final View root) {
        if (root instanceof Scalable && ((Scalable) root).getScalableViews().size() > 0) {
            ((Scalable) root).setSupportScale(scale);
        }
    }

    protected float maxScale() {
        return MultiTouchListener.MAX_SCALE;
    }

    protected float minScale() {
        return MultiTouchListener.MIN_SCALE;
    }
}
