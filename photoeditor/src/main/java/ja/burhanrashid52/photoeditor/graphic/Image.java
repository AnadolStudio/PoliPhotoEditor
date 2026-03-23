package ja.burhanrashid52.photoeditor.graphic;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ja.burhanrashid52.photoeditor.PhotoEditorViewState;
import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.gesture.MultiTouchListener;
import ja.burhanrashid52.photoeditor.view.BorderView;

public class Image extends Graphic {

    private final MultiTouchListener mMultiTouchListener;
    private final ViewGroup mPhotoEditorView;
    private final PhotoEditorViewState mViewState;
    private final GraphicBorderActions graphicBorderActions;
    private ImageView imageView;
    private Bitmap bitmap;
    private String name;

    public Image(ViewGroup photoEditorView,
                 MultiTouchListener multiTouchListener,
                 PhotoEditorViewState viewState,
                 GraphicManager graphicManager,
                 GraphicBorderActions graphicBorderActions
    ) {
        this(photoEditorView, multiTouchListener, viewState,graphicManager,graphicBorderActions, null);
    }

    public Image(ViewGroup photoEditorView,
                 MultiTouchListener multiTouchListener,
                 PhotoEditorViewState viewState,
                 GraphicManager graphicManager,
                 GraphicBorderActions graphicBorderActions,
                 String name
    ) {
        super(photoEditorView.getContext(), graphicManager);
        mPhotoEditorView = photoEditorView;
        mViewState = viewState;
        mMultiTouchListener = multiTouchListener;
        this.graphicBorderActions = graphicBorderActions;
        setupGesture();
        this.name = name;
    }

    public void buildView(Bitmap desiredImage) {
        this.bitmap = desiredImage;
        imageView.setImageBitmap(desiredImage);
        BorderView borderView = getRootView().findViewById(R.id.frmBorder);

        if (borderView != null) {
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
    public void flip() {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(bitmap);
    }

    public Bitmap getImage() {
        return bitmap;
    }

    public String getName() {
        return name;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.IMAGE;
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_photo_editor_image;
    }

    @Override
    public void setupView(View rootView) {
        imageView = rootView.findViewById(R.id.imgPhotoEditorImage);
    }
}
