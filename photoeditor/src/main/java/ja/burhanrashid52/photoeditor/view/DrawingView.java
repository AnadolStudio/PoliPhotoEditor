package ja.burhanrashid52.photoeditor.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;

import androidx.annotation.NonNull;
import ja.burhanrashid52.photoeditor.brush.BrushViewChangeListener;
import ja.burhanrashid52.photoeditor.shape.AbstractShape;
import ja.burhanrashid52.photoeditor.shape.BrushShape;
import ja.burhanrashid52.photoeditor.shape.LineShape;
import ja.burhanrashid52.photoeditor.shape.OvalShape;
import ja.burhanrashid52.photoeditor.shape.RectangleShape;
import ja.burhanrashid52.photoeditor.shape.ShapeAndPaint;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

/**
 * <p>
 * This is custom drawing view used to do painting on user touch events it it will paint on canvas
 * as per attributes provided to the paint
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 12/1/18
 */
public class DrawingView extends View {

    static final float DEFAULT_ERASER_SIZE = 50.0f;
    private final Stack<ShapeAndPaint> drawShapes = new Stack<>();
    private final Stack<ShapeAndPaint> redoShapes = new Stack<>();
    private ShapeAndPaint currentShape;
    private ShapeBuilder currentShapeBuilder;
    private boolean isEnabled;
    private BrushViewChangeListener viewChangeListener;
    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    // eraser parameters
    private boolean isErasing = false;
    private boolean hasDrawingPath = false;
    private float mBrushEraserSize = DEFAULT_ERASER_SIZE;
    private Bitmap defaultBitmap;
    private Bitmap drawableBitmap;
    private OnTouchListener touchListener;
    private View parent;
    private boolean isProcess = false;

    // region constructors
    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupBrushDrawing();
    }
    // endregion

    public void setPorterDuffXferMode(PorterDuff.Mode mode) {
        switch (mode) {
            case SRC:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    this.porterDuffXfermode = new PorterDuffXfermode(mode);
                }
                break;
            default:
                this.porterDuffXfermode = new PorterDuffXfermode(mode);
                break;
        }
    }

    private Paint createPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(porterDuffXfermode);

        // apply shape builder parameters
        paint.setStrokeWidth(currentShapeBuilder.getShapeSize());
        paint.setAlpha(currentShapeBuilder.getShapeOpacity());
        paint.setColor(currentShapeBuilder.getShapeColor());

        return paint;
    }

    private Paint createEraserPaint() {
        Paint paint = createPaint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        return paint;
    }

    private void setupBrushDrawing() {
        //Caution: This line is to disable hardware acceleration to make eraser feature work properly
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setVisibility(View.GONE);
        currentShapeBuilder = new ShapeBuilder();
    }

    public void clearAll() {
        drawableBitmap = null;
        hasDrawingPath = false;
        drawShapes.clear();
        redoShapes.clear();
        invalidate();
    }

    public void setBrushViewChangeListener(BrushViewChangeListener brushViewChangeListener) {
        viewChangeListener = brushViewChangeListener;
    }

    public void setDefaultBitmap(Bitmap bitmap) {
        this.defaultBitmap = bitmap;
    }

    public void setDrawableBitmap(Bitmap bitmap) {
        this.drawableBitmap = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (defaultBitmap != null) {
            canvas.drawBitmap(getDrawBitmap(defaultBitmap), 0, 0, null);
        }

        if (drawableBitmap != null) {
            canvas.drawBitmap(getDrawBitmap(drawableBitmap), 0, 0, null);
        }

        for (ShapeAndPaint shape : drawShapes) {
            shape.getShape().draw(canvas, shape.getPaint());
        }
//        invalidate();
    }

    private Bitmap getDrawBitmap(Bitmap bitmap) {
        if (bitmap.getWidth() == getWidth() && bitmap.getHeight() == getHeight()) {
            return bitmap;
        }
        return Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
    }

    public void setOnTouchListener(View parent, OnTouchListener l) {
        this.parent = parent;
        touchListener = l;
    }

    /**
     * Handle touch event to draw paint on canvas i.e brush drawing
     *
     * @param event points having touch info
     * @return true if handling touch events
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (event.getPointerCount() >= 2) {
            if (touchListener != null) touchListener.onTouch(parent, event);
            isProcess = false;

            return false;
        }
        if (isEnabled) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onTouchEventDown(touchX, touchY);
                    isProcess = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isProcess) {
                        onTouchEventMove(touchX, touchY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    onTouchEventUp(touchX, touchY);
                    isProcess = false;
                    break;
            }
            invalidate();

            return true;
        } else {
            return false;
        }
    }

    private void onTouchEventDown(float touchX, float touchY) {
        createShape();
        if (currentShape != null && currentShape.getShape() != null) {
            currentShape.getShape().startShape(touchX, touchY);
        }
    }

    private void onTouchEventMove(float touchX, float touchY) {
        if (currentShape != null && currentShape.getShape() != null) {
            currentShape.getShape().moveShape(touchX, touchY);
            hasDrawingPath = true;
        }
    }

    private void onTouchEventUp(float touchX, float touchY) {
        if (currentShape != null && currentShape.getShape() != null) {
            currentShape.getShape().stopShape();
            endShape(touchX, touchY);
        }
    }

    private void createShape() {
        final AbstractShape shape;
        Paint paint = createPaint();
        if (isErasing) {
            shape = new BrushShape();
            paint = createEraserPaint();
        } else if (currentShapeBuilder.getShapeType() == ShapeType.OVAL) {
            shape = new OvalShape();
        } else if (currentShapeBuilder.getShapeType() == ShapeType.RECTANGLE) {
            shape = new RectangleShape();
        } else if (currentShapeBuilder.getShapeType() == ShapeType.LINE) {
            shape = new LineShape();
        } else {
            shape = new BrushShape();
        }
        currentShape = new ShapeAndPaint(shape, paint);
        drawShapes.push(currentShape);

        if (viewChangeListener != null) {
            viewChangeListener.onStartDrawing();
        }

    }

    private void endShape(float touchX, float touchY) {
        boolean hasBeenTapped = currentShape.getShape().hasBeenTapped();
        if (hasBeenTapped) {
            // just a tap, this is not a shape, so remove it
            drawShapes.remove(currentShape);
        }

        if (viewChangeListener != null) {
            viewChangeListener.onStopDrawing();
            viewChangeListener.onViewAdd(this);
        }
    }

    public boolean undo() {
        if (!drawShapes.empty()) {
            redoShapes.push(drawShapes.pop());
            invalidate();
        }
        if (viewChangeListener != null) {
            viewChangeListener.onViewRemoved(this);
        }
        return !drawShapes.empty();
    }

    public boolean redo() {
        if (!redoShapes.empty()) {
            drawShapes.push(redoShapes.pop());
            invalidate();
        }

        if (viewChangeListener != null) {
            viewChangeListener.onViewAdd(this);
        }

        return !redoShapes.empty();
    }

    // region eraser
    public void brushEraser() {
        isEnabled = true;
        isErasing = true;
    }

    public void setBrushEraserSize(float brushEraserSize) {
        mBrushEraserSize = brushEraserSize;
    }

    public float getEraserSize() {
        return mBrushEraserSize;
    }
    // endregion

    // region Setters/Getters
    public void setShapeBuilder(ShapeBuilder shapeBuilder) {
        currentShapeBuilder = shapeBuilder;
    }

    public void enableDrawing(boolean brushDrawMode) {
        isEnabled = brushDrawMode;
        isErasing = !brushDrawMode;
        if (brushDrawMode) {
            setVisibility(View.VISIBLE);
        }
    }

    public boolean hasDrawingPath() {
        return hasDrawingPath;
    }

    public boolean isDrawingEnabled() {
        return isEnabled;
    }

    public ShapeAndPaint getCurrentShape() {
        return currentShape;
    }

    public ShapeBuilder getCurrentShapeBuilder() {
        return currentShapeBuilder;
    }

    Pair<Stack<ShapeAndPaint>, Stack<ShapeAndPaint>> getDrawingPath() {
        return new Pair<>(drawShapes, redoShapes);
    }
    // endregion
}

