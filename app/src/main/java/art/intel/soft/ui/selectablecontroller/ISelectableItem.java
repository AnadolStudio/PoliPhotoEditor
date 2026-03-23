package art.intel.soft.ui.selectablecontroller;

public interface ISelectableItem<T> {

    void setCurrentSelectedItem(T t);

    void updateView(T t, boolean isSelected);

    int saveState(T t);

    int getState();
}
