package ja.burhanrashid52.photoeditor;

import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Tracked state of user-added views (stickers, emoji, text, etc)
 */
public class PhotoEditorViewState {

    private View currentSelectedView;
    private List<View> addedViews;
    private Stack<View> redoViews;

    PhotoEditorViewState() {
        this.currentSelectedView = null;
        this.addedViews = new ArrayList<>();
        this.redoViews = new Stack<>();
    }

    public View getCurrentSelectedView() {
        return currentSelectedView;
    }

    public void setCurrentSelectedView(View currentSelectedView) {
        this.currentSelectedView = currentSelectedView;
    }

    public void clearCurrentSelectedView() {
        this.currentSelectedView = null;
    }

    public View getAddedView(int index) {
        return addedViews.get(index);
    }

    public int getAddedViewsCount() {
        return addedViews.size();
    }

    public void clearAddedViews() {
        addedViews.clear();
    }

    public void addAddedView(final View view) {
        addedViews.add(view);
    }

    public void removeAddedView(final View view) {
        addedViews.remove(view);
    }

    public View removeAddedView(final int index) {
        return addedViews.remove(index);
    }

    public boolean containsAddedView(final View view) {
        return addedViews.contains(view);
    }

    /**
     * Replaces a view in the current "added views" list.
     *
     * @param view The view to replace
     * @return true if the view was found and replaced, false if the view was not found
     */
    public boolean replaceAddedView(final View view) {
        final int i = addedViews.indexOf(view);
        if (i > -1) {
            addedViews.set(i, view);
            return true;
        }
        return false;
    }

    public void clearRedoViews() {
        redoViews.clear();
    }

    public void pushRedoView(final View view) {
        redoViews.push(view);
    }

    public View popRedoView() {
        return redoViews.pop();
    }

    public int getRedoViewsCount() {
        return redoViews.size();
    }

    public View getRedoView(int index) {
        return redoViews.get(index);
    }
}
