package art.intel.soft.utils.touchlisteners;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Created on 18/01/2017.
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * <p></p>
 */
public class BodyTouchListener implements OnTouchListener {
    public static final String TAG = BodyTouchListener.class.getName();

    private static final int INVALID_POINTER_ID = -1;

    private static void adjustTranslation(View view, float deltaX, float deltaY) {
        float[] deltaVector = {deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
        view.setTranslationY(view.getTranslationY() + deltaVector[1]);
    }

    private final GestureDetector mGestureListener;
    private final ScaleGestureDetector mScaleGestureDetector;
    private final boolean mIsPinchScalable;
    private final boolean isRotateEnabled = true;
    private final boolean isTranslateEnabled = true;
    private final boolean isScaleEnabled = true;
    private final float minimumScale = 0.5f;
    private final float maximumScale = 2f;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPrevX, mPrevY;

    public BodyTouchListener(boolean isPinchScalable) {
        mIsPinchScalable = isPinchScalable;

        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
        mGestureListener = new GestureDetector(new GestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(view, event);
        mGestureListener.onTouchEvent(event);
        int action = event.getAction();

        switch (action & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouch: ACTION_DOWN");
                mPrevX = event.getX();
                mPrevY = event.getY();
                float mPrevRawX = event.getRawX();
                float mPrevRawY = event.getRawY();
                mActivePointerId = event.getPointerId(0);
                view.bringToFront();
                break;
            case MotionEvent.ACTION_MOVE:
                // Only enable dragging on focused stickers.
                int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                if (pointerIndexMove != -1) {
                    float currX = event.getX(pointerIndexMove);
                    float currY = event.getY(pointerIndexMove);
                    if (!mScaleGestureDetector.isInProgress()) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int pointerIndexPointerUp = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndexPointerUp);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndexPointerUp == 0 ? 1 : 0;
                    mPrevX = event.getX(newPointerIndex);
                    mPrevY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }
        return true;
    }

    private static class TransformInfo {
        float deltaX;
        float deltaY;
        float deltaScale;
        float deltaAngle;
        float pivotX;
        float pivotY;
        float minimumScale;
        float maximumScale;
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private final Vector2D mPrevSpanVector = new Vector2D();
        private float mPivotX;
        private float mPivotY;

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            Log.d(TAG, "onTouch: onScaleBegin");
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            return mIsPinchScalable;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            Log.d(TAG, "onTouch: onScale");
            TransformInfo info = new TransformInfo();
            info.deltaScale = isScaleEnabled ? detector.getScaleFactor() : 1.0f;
            info.deltaAngle = isRotateEnabled ? Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector()) : 0.0f;
            info.deltaX = isTranslateEnabled ? detector.getFocusX() - mPivotX : 0.0f;
            info.deltaY = isTranslateEnabled ? detector.getFocusY() - mPivotY : 0.0f;
            info.pivotX = mPivotX;
            info.pivotY = mPivotY;
            info.minimumScale = minimumScale;
            info.maximumScale = maximumScale;

            return !mIsPinchScalable;
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

        }
    }
}
